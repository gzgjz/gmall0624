package com.atguigu.gmall0624.service;

import com.atguigu.gmall0624.bean.UserAddress;
import com.atguigu.gmall0624.bean.UserInfo;

import java.util.List;

public interface UserInfoService {

    List<UserInfo> getAllUserInfo();

    List<UserInfo> selectUser(UserInfo userInfo);

    List<UserInfo> getaddUser(String name);

    List<UserInfo> findAll();

    List<UserAddress> findUserAddressListByUserId(String userId);

    List<UserAddress> findUserAddressListByUserId(UserAddress userAddress);

    UserInfo login(UserInfo userInfo);

    UserInfo verify(String userId);
}
