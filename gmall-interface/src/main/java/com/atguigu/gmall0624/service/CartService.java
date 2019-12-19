package com.atguigu.gmall0624.service;

import com.atguigu.gmall0624.bean.CartInfo;

import java.util.List;

public interface CartService {

    //添加购物车数据
    void addToCart(String skuId,String userId,Integer skuNum);

    List<CartInfo> getCartList(String userTempId);

    List<CartInfo> mergeToCartList(List<CartInfo> cartInfoArrayList, String userId);

    void deleteCartList(String userTempId);

    void checkCart(String isChecked, String skuId, String userId);

    List<CartInfo> getCartCheckedList(String userId);
}
