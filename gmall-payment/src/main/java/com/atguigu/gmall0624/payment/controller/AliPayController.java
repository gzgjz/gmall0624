package com.atguigu.gmall0624.payment.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall0624.bean.OrderInfo;
import com.atguigu.gmall0624.bean.PaymentInfo;
import com.atguigu.gmall0624.bean.enums.PaymentStatus;
import com.atguigu.gmall0624.service.OrderService;
import com.atguigu.gmall0624.service.PaymentService;
import com.atguigu.gmall0624.payment.config.AlipayConfig;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.netty.handler.codec.rtsp.RtspHeaders.Values.CHARSET;

@Controller
public class AliPayController {

    @Autowired
    private AlipayClient alipayClient;

    @Reference
    private OrderService orderService;

    @Reference
    private PaymentService paymentService;

    @RequestMapping("alipay/submit")
    @ResponseBody
    public String aliPaySbumit(HttpServletRequest request, HttpServletResponse response){
        String orderId = request.getParameter("orderId");
        PaymentInfo paymentInfo = new PaymentInfo();
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("大衣，帽子");
        paymentInfo.setCreateTime(new Date());

        paymentService.savePaymentInfo(paymentInfo);


        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //获得初始化的AlipayClient
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        HashMap<String,Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",orderInfo.getTotalAmount());
        map.put("subject","帽子");

        alipayRequest.setBizContent(JSON.toJSONString(map));//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=" + CHARSET);
        //httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        //httpResponse.getWriter().flush();
        //httpResponse.getWriter().close();
        return form;
    }

    @RequestMapping("alipay/callback/return")
    public String callBack(){
        return "redirect:"+AlipayConfig.return_order_url;
    }

    @RequestMapping("alipay/callback/notify")
    @ResponseBody
    public String callbackNotify(@RequestParam Map<String,String> paramMap,
                                 HttpServletRequest request) throws AlipayApiException {
        System.out.println("callbackNotify");
        String trade_status = paramMap.get("trade_status");
        String out_trade_no = paramMap.get("out_trade_no");
        boolean signVerified = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, "utf-8",AlipayConfig.sign_type);
        if(signVerified){
            if("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){

                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOutTradeNo(out_trade_no);
                PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);
                if (paymentInfoQuery.getPaymentStatus()==PaymentStatus.PAID ||paymentInfoQuery.getPaymentStatus()==PaymentStatus.ClOSED){
                    return "failure";
                }

                PaymentInfo paymentInfoUPD = new PaymentInfo();
                paymentInfoUPD.setCallbackTime(new Date());
                paymentInfoUPD.setPaymentStatus(PaymentStatus.PAID);
                paymentInfoUPD.setCallbackContent(paramMap.toString());
                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUPD);
                return "success";
            }
        }else {
            return "failure";
        }
        return "failure";
    }

    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId){
        boolean flag = paymentService.refund(orderId);

        return ""+flag;
    }
}
