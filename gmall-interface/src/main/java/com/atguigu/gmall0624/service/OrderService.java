package com.atguigu.gmall0624.service;

import com.atguigu.gmall0624.bean.OrderInfo;

public interface OrderService {
    String saveOrder(OrderInfo orderInfo);

    String getTradeNo(String userId);

    boolean checkTradeCode(String userId,String tradeCodeNo);

    void deleteTradeCode(String userId);

    boolean checkStock(String skuId, Integer skuNum);

    OrderInfo getOrderInfo(String orderId);
}
