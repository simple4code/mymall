package com.hzc.mymall.order.web;

import com.alipay.api.AlipayApiException;
import com.hzc.mymall.order.config.AlipayTemplate;
import com.hzc.mymall.order.service.OrderService;
import com.hzc.mymall.order.vo.PayVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-11 13:38
 */
@Controller
@Slf4j
public class PayWebController {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;

    /**
     * 1, 将支付页让浏览器展示
     * 2, 支付成功以后，跳转到用户的订单列表页
     * @param orderSn
     * @return
     * @throws AlipayApiException
     */
    @GetMapping(value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);

        // AlipayTemplate#pay 方法返回的是一个页面，将此页面直接交给浏览器即可
        String pay = alipayTemplate.pay(payVo);

        return pay;
    }
}
