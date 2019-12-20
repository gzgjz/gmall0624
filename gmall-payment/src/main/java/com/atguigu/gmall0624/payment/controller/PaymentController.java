package com.atguigu.gmall0624.payment.controller;

import com.atguigu.gmall0624.bean.OrderInfo;
import com.atguigu.gmall0624.config.LoginRequire;
import com.atguigu.gmall0624.service.OrderService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PaymentController {

    @Reference
    private OrderService orderService;

    @RequestMapping("index")
    @LoginRequire
    public String index(HttpServletRequest request){
        String orderId = request.getParameter("orderId");

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        request.setAttribute("orderId",orderId);
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());


        return "index";
    }
}
