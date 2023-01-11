package com.hzc.mymall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hzc.common.constant.OrderConstant;
import com.hzc.common.exception.NoStockException;
import com.hzc.common.to.mq.OrderTo;
import com.hzc.common.utils.R;
import com.hzc.common.vo.MemberRespVo;
import com.hzc.common.vo.OrderSubmitVo;
import com.hzc.mymall.order.dao.OrderItemDao;
import com.hzc.mymall.order.entity.OrderItemEntity;
import com.hzc.mymall.order.entity.PaymentInfoEntity;
import com.hzc.mymall.order.enume.OrderStatusEnum;
import com.hzc.mymall.order.feign.CartFeignService;
import com.hzc.mymall.order.feign.MemberFeignService;
import com.hzc.mymall.order.feign.ProductFeignService;
import com.hzc.mymall.order.feign.WmsFeignService;
import com.hzc.mymall.order.interceptor.LonginUserInterceptor;
import com.hzc.mymall.order.service.OrderItemService;
import com.hzc.mymall.order.service.PaymentInfoService;
import com.hzc.mymall.order.to.OrderCreateTo;
import com.hzc.mymall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.Query;

import com.hzc.mymall.order.dao.OrderDao;
import com.hzc.mymall.order.entity.OrderEntity;
import com.hzc.mymall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WmsFeignService wmsFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentInfoService paymentInfoService;

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

        // 5. 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(),
                token, 30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, cartFuture).get();

        return confirmVo;
    }

    /**
     * 下订单操作
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        submitVoThreadLocal.set(vo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);
        MemberRespVo memberRespVo = LonginUserInterceptor.loginUser.get();

        // 1. 验证令牌【令牌的对比和删除必须保证原子性】
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()),
                orderToken);
        if(result == 0) {
            // 令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        }else {
            // 令牌验证成功
            // 去创建订单，验令牌，验价格，锁库存
            // 1, 创建订单，订单项等信息
            OrderCreateTo order = createOrder();
            // 2, 验价
            BigDecimal payAmount = order.getOrderEntity().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue())<0.01) {
                // 金额对比成功
                // 3, 保存订单
                saveOrder(order);
                // 4, 库存锁定。只要有异常就回滚订单数据
                // 订单号，所有订单项（skuId, skuName, num）
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrderEntity().getOrderSn());

                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);

                // 远程锁库存
                // 如果库存锁定成功，但是因为网络原因超时了，那么订单可以回滚，库存无法回滚
                // 为了保证高并发，库存服务自己回滚（如果发生异常）
                // 库存本身也可以使用自动解锁模式
                R r = wmsFeignService.orderLockStock(lockVo);
                if(r.getCode() == 0) {
                    // 锁成功了
                    responseVo.setOrderEntity(order.getOrderEntity());

                    // todo 5, 远程扣减积分
//                    int i = 10/0;
                    // 订单创建成功，发送消息给 MQ
                    rabbitTemplate.convertAndSend("order.event.exchange",
                            "order.create.order",
                            order.getOrderEntity());
                    return responseVo;
                }else {
                    // 锁失败了
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
//                    responseVo.setCode(3);
//                    return responseVo;
                }
            }else {
                // 金额对比失败
                responseVo.setCode(2);
                return responseVo;
            }
        }

//        String redisOrderToken =
//                redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());
//        if(orderToken != null && orderToken.equals(redisOrderToken)) {
//            // 令牌验证通过
//            redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());
//            return responseVo;
//        }else {
//            // 令牌验证不通过
//            return responseVo;
//        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));

        return orderEntity;
    }

    /**
     * 关闭订单
     * @param orderEntity
     */
    @Override
    public void closeOrder(OrderEntity orderEntity) {
        // 查询当前这个订单的最新状态
        OrderEntity entity = this.getById(orderEntity.getId());
        // 关单
        if (entity.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            entity.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(entity);
            // 通知库存队列订单已关闭
            OrderTo order = new OrderTo();
            BeanUtils.copyProperties(entity, order);
            try {
                // TODO 保证消息一定会发送出去（给数据库保存每一个消息的详细信息）
                // TODO 定期扫描数据库，将发送失败的消息重新发送
                rabbitTemplate.convertAndSend("order.event.exchange",
                        "order.release.other",
                        order);
            }catch (Exception e) {
                // TODO 将没发送成功的消息进行重试发送
            }
        }
    }

    /**
     * 获取当前订单的支付信息
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();

        OrderEntity orderEntity = this.getOrderByOrderSn(orderSn);
        payVo.setTotal_amount(orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP).toString());
        payVo.setOut_trade_no(orderEntity.getOrderSn());
        payVo.setSubject("付款测试");

        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo memberRespVo = LonginUserInterceptor.loginUser.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
                        .eq("member_id", memberRespVo.getId())
                        .orderByDesc("id")
        );

        List<OrderEntity> collect = page.getRecords().stream().map(item -> {
            List<OrderItemEntity> entities =
                    orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", item.getOrderSn()));
            item.setItemEntities(entities);
            return item;
        }).collect(Collectors.toList());
        page.setRecords(collect);

        return new PageUtils(page);
    }

    /**
     * 根据支付宝支付结果，处理订单状态
     * @param payAsyncVo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo payAsyncVo) {
        // 1. 保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(payAsyncVo.getTrade_no());
        infoEntity.setOrderSn(payAsyncVo.getOut_trade_no());
        infoEntity.setPaymentStatus(payAsyncVo.getTrade_status());
        infoEntity.setCallbackTime(payAsyncVo.getNotify_time());
        paymentInfoService.save(infoEntity);

        // 2. 修改订单的状态信息
        String trade_status = payAsyncVo.getTrade_status();
        if(trade_status.equals("TRADE_SUCCESS") || trade_status.equals("TRADE_FINISHED")) {
            // 支付成功
            String outTradeNo = payAsyncVo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(outTradeNo, OrderStatusEnum.PAYED.getCode());
        }

        return "success";
    }

    /**
     * 保存订单数据
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrderEntity();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 1. 生成订单号
        String orderSn = IdWorker.getTimeId();

        // 创建订单
        OrderEntity order = buildOrder(orderSn);

        // 获取所有订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);

        // 验价
        computePrice(order, itemEntities);
        orderCreateTo.setOrderEntity(order);
        orderCreateTo.setOrderItems(itemEntities);

        return orderCreateTo;
    }

    private void computePrice(OrderEntity order, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        Integer gift = 0;
        Integer growth = 0;

        // 订单的总额，叠加每一个项的总额
        for (OrderItemEntity itemEntity : itemEntities) {
            total = total.add(itemEntity.getRealAmount());
            coupon = coupon.add(itemEntity.getCouponAmount());
            integration = integration.add(itemEntity.getIntegrationAmount());
            promotion = promotion.add(itemEntity.getPromotionAmount());
            gift += itemEntity.getGiftIntegration();
            growth += itemEntity.getGiftGrowth();
        }
        // 1. 订单价格相关
        order.setTotalAmount(total);
        // 设置应付总额
        order.setPayAmount(total.add(order.getFreightAmount()));
        order.setPromotionAmount(promotion);
        order.setIntegrationAmount(integration);
        order.setCouponAmount(coupon);
        // 设置积分和成长值
        order.setIntegration(gift);
        order.setGrowth(growth);
    }

    /**
     * 构建订单
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        MemberRespVo memberRespVo = LonginUserInterceptor.loginUser.get();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(memberRespVo.getId());

        // 获取收货地址信息
        OrderSubmitVo orderSubmitVo = submitVoThreadLocal.get();
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareVo = fare.getData(new TypeReference<FareVo>() {});
        // 设置运费信息
        entity.setFreightAmount(fareVo.getFare());
        // 设置收货人信息
        entity.setReceiverCity(fareVo.getAddress().getCity());
        entity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
        entity.setReceiverName(fareVo.getAddress().getName());
        entity.setReceiverPhone(fareVo.getAddress().getPhone());
        entity.setReceiverPostCode(fareVo.getAddress().getPostCode());
        entity.setReceiverProvince(fareVo.getAddress().getProvince());
        entity.setReceiverRegion(fareVo.getAddress().getRegion());

        // 设置订单的相关状态信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);
        // 设置删除状态
        entity.setDeleteStatus(0);

        return entity;
    }

    /**
     * 构建所有订单项
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // 获取所有订单项
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if(currentUserCartItems != null && !currentUserCartItems.isEmpty()) {
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItem(item);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());

            return itemEntities;
        }

        return null;
    }

    /**
     * 构建单个订单项
     * @param item
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // 1.订单信息：订单号

        // 2.商品的SPU信息
        Long skuId = item.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoVo = r.getData(new TypeReference<SpuInfoVo>() {});
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());

        // 3.商品的SKU信息
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";"));
        orderItemEntity.setSkuQuantity(item.getCount());
        // 4.优惠信息【忽略】
        // 5.积分信息
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());

        // 6.订单项价格信息
        orderItemEntity.setPromotionAmount(new BigDecimal("0.0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0.0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        // 当前订单项的实际金额
        BigDecimal origin = orderItemEntity.getSkuPrice()
                .multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = origin.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;
    }

}