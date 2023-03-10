package com.hzc.mymall.order.web;

import com.hzc.common.exception.NoStockException;
import com.hzc.common.vo.OrderSubmitVo;
import com.hzc.mymall.order.service.OrderService;
import com.hzc.mymall.order.vo.OrderConfirmVo;
import com.hzc.mymall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-08 17:43
 */
@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();

        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {

        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
            if(responseVo.getCode() == 0) {
                // 下单成功，前往支付选择页
                model.addAttribute("submitOrderResp", responseVo);
                return "pay";
            }else {
                // 下单失败回到订单确认页重新确认订单信息
                String msg = "下单失败";
                switch (responseVo.getCode()) {
                    case 1: msg += "订单信息过期，请刷新再次提交"; break;
                    case 2: msg += "订单商品价格发生变化，请确认后再次提交"; break;
                    case 3: msg += "库存锁定失败，商品库存不足"; break;
                }
                redirectAttributes.addFlashAttribute("msg", msg);
                return "redirect:http://order.mymall.com/toTrade";
            }
        }catch (Exception e) {
            if (e instanceof NoStockException) {
                String message = e.getMessage();
                redirectAttributes.addFlashAttribute("msg",message);
            }
            return "redirect:http://order.mymall.com/toTrade";
        }
    }
}
