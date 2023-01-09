package com.hzc.mymall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.hzc.common.utils.R;
import com.hzc.common.vo.MemberRespVo;
import com.hzc.mymall.order.feign.CartFeignService;
import com.hzc.mymall.order.feign.MemberFeignService;
import com.hzc.mymall.order.feign.WmsFeignService;
import com.hzc.mymall.order.interceptor.LonginUserInterceptor;
import com.hzc.mymall.order.vo.MemberAddressVo;
import com.hzc.mymall.order.vo.OrderConfirmVo;
import com.hzc.mymall.order.vo.OrderItemVo;
import com.hzc.mymall.order.vo.SkuStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.Query;

import com.hzc.mymall.order.dao.OrderDao;
import com.hzc.mymall.order.entity.OrderEntity;
import com.hzc.mymall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WmsFeignService wmsFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 返回订单确认页需要的数据
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LonginUserInterceptor.loginUser.get();

        // 先取出原请求数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            // 因为异步线程获取不到原请求数据，所以这里手动设置下原请求数据到当前异步线程的 ThreadLocal
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1. 远程查询所有收货地址列表
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);


        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            // 因为异步线程获取不到原请求数据，所以这里手动设置下原请求数据到当前异步线程的 ThreadLocal
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2. 远程查询购物车所有选中的购物项
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 查询库存信息
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R r = wmsFeignService.getSkusHasStock(collect);
            List<SkuStockVo> skuStockVos = r.getData(new TypeReference<List<SkuStockVo>>() {});
            if(skuStockVos != null) {
                Map<Long, Boolean> map = skuStockVos.stream()
                        .collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(map);
            }
        }, executor);

        // 3. 查询用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        // 4. 其他数据自动计算

        // todo 5. 仿重令牌

        CompletableFuture.allOf(getAddressFuture, cartFuture).get();

        return confirmVo;
    }

}