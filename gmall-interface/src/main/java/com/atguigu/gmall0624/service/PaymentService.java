package com.atguigu.gmall0624.service;

import com.atguigu.gmall0624.bean.PaymentInfo;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    boolean refund(String orderId);
}
