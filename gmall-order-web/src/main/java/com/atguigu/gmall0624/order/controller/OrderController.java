package com.atguigu.gmall0624.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0624.bean.*;
import com.atguigu.gmall0624.config.LoginRequire;
import com.atguigu.gmall0624.service.CartService;
import com.atguigu.gmall0624.service.ManageService;
import com.atguigu.gmall0624.service.OrderService;
import com.atguigu.gmall0624.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
public class OrderController {

    @Reference
    private UserInfoService userInfoService;

    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;

    @Reference
    private ManageService manageService;

    @RequestMapping("trade")
    //@ResponseBody // 第一个作用：返回json 字符串。第二个作用：将控制器中的数据直接输入到一个空白页！
    @LoginRequire
    public String trade(HttpServletRequest request){
        // 获取用户Id
        String userId = (String) request.getAttribute("userId");
        // 得到用户的收货地址列表
        // return userInfoService.findUserAddressListByUserId(userId);
        List<UserAddress> userAddressList = userInfoService.findUserAddressListByUserId(userId);

        // 获取购物车中的数据：
        List<CartInfo> cartInfoList = cartService.getCartCheckedList(userId);

        // 保存订单明细数据，订单明细数据来自于购物车的!
        ArrayList<OrderDetail> detailsList = new ArrayList<>();
        if (cartInfoList!=null && cartInfoList.size()>0){
            for (CartInfo cartInfo : cartInfoList) {
                OrderDetail orderDetail = new OrderDetail();

                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                // 添加到订单明细集合中
                detailsList.add(orderDetail);

            }
        }
        // 计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailsList);
        // 调用方法
        orderInfo.sumTotalAmount();

        // 保存作用域，给页面渲染
        request.setAttribute("detailsList",detailsList);

        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        request.setAttribute("userAddressList",userAddressList);

        //将流水号保存到后台
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);

        return "trade";
    }
    // http://trade.gmall.com/submitOrder
    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        String tradeNo = request.getParameter("tradeNo");

        // 获取用户Id
        String userId = (String) request.getAttribute("userId");
        orderInfo.setUserId(userId);
        boolean result = orderService.checkTradeCode(userId, tradeNo);
        if(!result){
            request.setAttribute("errMsg","不能重复提交订单！");

            return "tradeFail";
        }

        orderService.deleteTradeCode(userId);
        //验证库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if(orderDetailList != null && orderDetailList.size() > 0){
            for (OrderDetail orderDetail : orderDetailList) {
                boolean flag = orderService.checkStock(orderDetail.getSkuId(),orderDetail.getSkuNum());

                if(!flag){
                    request.setAttribute("errMsg",orderDetail.getSkuName()+"库存不足，请重新下单订单！");

                    return "tradeFail";
                }

                //验证价格
                SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
                if(skuInfo.getPrice().compareTo(orderDetail.getOrderPrice())!=0){
                    cartService.loadCartCache(userId);
                }
            }
        }

        // 保存订单
        String orderId = orderService.saveOrder(orderInfo);
        //  支付页面
        return  "redirect://payment.gmall.com/index?orderId="+orderId;
    }
}
