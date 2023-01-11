package com.hzc.mymall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzc.common.exception.NoStockException;
import com.hzc.common.to.mq.OrderTo;
import com.hzc.common.to.mq.StockDetailTo;
import com.hzc.common.to.mq.StockLockedTo;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.Query;
import com.hzc.common.utils.R;
import com.hzc.mymall.ware.dao.WareSkuDao;
import com.hzc.mymall.ware.entity.WareOrderTaskDetailEntity;
import com.hzc.mymall.ware.entity.WareOrderTaskEntity;
import com.hzc.mymall.ware.entity.WareSkuEntity;
import com.hzc.mymall.ware.feign.OrderFeignService;
import com.hzc.mymall.ware.feign.ProductFeignService;
import com.hzc.mymall.ware.service.WareOrderTaskDetailService;
import com.hzc.mymall.ware.service.WareOrderTaskService;
import com.hzc.mymall.ware.service.WareSkuService;
import com.hzc.mymall.ware.vo.OrderItemVo;
import com.hzc.mymall.ware.vo.OrderVo;
import com.hzc.mymall.ware.vo.SkuHasStockVo;
import com.hzc.mymall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
@Slf4j
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 判断是否有库存记录
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(
                new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities == null || wareSkuEntities.isEmpty()) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            // 远程查询 sku 的名字，如果失败，流程继续，无需回滚异常
            try {
                R info = productFeignService.info(skuId);
                if(info.getCode() == 0) {
                    Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e) {}
            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> hasStockVos = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            // 查询当前 sku 的总库存量
            Long count = this.baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count == null ? false : count > 0);

            return skuHasStockVo;
        }).collect(Collectors.toList());
        return hasStockVos;
    }

    /**
     * 为某个订单锁定库存
     *
     * 库存解锁的场景：
     *      1)，下订单成功，订单过期没有支付被系统自动取消，被用户手动取消。都要解锁库存
     *      2)，下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚，之前锁定的库存需要自动解锁
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = {NoStockException.class})
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        // 保存库存工作单的详情
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        // 1, 按照下单的收货地址，找到一个就近仓库，锁定库存

        // 1, 找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            // 查询这个商品在哪个仓库有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        // 2, 锁定库存
        for (SkuWareHasStock skuWareHasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareId();
            if(wareIds == null || wareIds.isEmpty()) {
                // 没有仓库有这个商品的库存
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, skuWareHasStock.getNum());
                if(count == 1) {
                    skuStocked = true;
                    // TODO：告诉库存锁定成功
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null,
                            skuId,
                            "",
                            skuWareHasStock.getNum(),
                            wareOrderTaskEntity.getId(),
                            wareId,
                            1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo detail = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, detail);
                    stockLockedTo.setDetail(detail);
                    rabbitTemplate.convertAndSend("stock.event.exchange",
                            "stock.locked",
                            stockLockedTo);
                    break;
                }else {
                    // 当前仓库锁定库存失败，继续下一个仓库
                }
            }
            if(!skuStocked) {
                // 当前商品所有仓库都没有锁定库存
                throw new NoStockException(skuId);
            }
        }

        // 所有商品库存锁定成功
        return true;
    }

    /**
     * 解锁库存
     * @param to
     */
    @Override
    public void unLockStock(StockLockedTo to) {
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        // 解锁
        // 1，查询数据库关于这个订单的锁定库存消息
        //      有：证明库存锁定成功，
        //          解锁：订单情况
        //              1，没有这个订单，必须解锁
        //              2，有这个订单，
        //                  1)，已取消，需要解锁库存
        //                  2)，没取消，不需要解锁
        //      没有：库存锁定失败，库存自动回滚，无需解锁
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if(byId != null) {
            // 解锁
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            // 根据订单号查询订单状态
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if(r.getCode() == 0) {
                // 订单数据返回成功
                OrderVo orderVo = r.getData(new TypeReference<OrderVo>() {});
                if(orderVo == null || orderVo.getStatus() == 4) {
                    // 当前库存工作单状态为已锁定，才可以解锁
                    if(byId.getLockStatus() == 1) {
                        // 订单已被取消或者订单不存在（因为异常订单服务事务回滚），解锁库存
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            }else {
                // 消息拒绝以后重新放到队列里面，让别人继续消费解锁
                throw new RuntimeException("远程订单服务失败");
            }
        }else {
            // 无需解锁
        }
    }

    /**
     * 防止网络卡顿情况下，库存信息在订单状态还未修改的情况下先到期，导致订单状态修改时，没有解锁库存
     * @param to
     */
    @Transactional
    @Override
    public void unLockStock(OrderTo to) {
        String orderSn = to.getOrderSn();
        // 查一下库存最新的状态，防止重复解锁库存
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = taskEntity.getId();
        // 按照工作单找到所有没有解锁的库存，进行解锁
        List<WareOrderTaskDetailEntity> detailEntities = wareOrderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", id)
                        .eq("lock_status", 1));
        for (WareOrderTaskDetailEntity detailEntity : detailEntities) {
            unLockStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum(),
                    detailEntity.getId());
        }
    }

    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        // 库存解锁
        wareSkuDao.unLockStock(skuId, wareId, num);
        // 更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(entity);
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}