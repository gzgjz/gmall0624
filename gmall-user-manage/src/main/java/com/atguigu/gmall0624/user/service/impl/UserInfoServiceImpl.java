package com.atguigu.gmall0624.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0624.bean.UserAddress;
import com.atguigu.gmall0624.bean.UserInfo;
import com.atguigu.gmall0624.service.UserInfoService;
import com.atguigu.gmall0624.user.mapper.UserAddressMapper;
import com.atguigu.gmall0624.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService{

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserInfo> getAllUserInfo() {
        return null;
    }

    @Override
    public List<UserInfo> selectUser(UserInfo userInfo) {
        return null;
    }

    @Override
    public List<UserInfo> getaddUser(String name) {
        return null;
    }

    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {

        UserAddress userAddress = new UserAddress();
        userAddress.setId(userId);

        return userAddressMapper.select(userAddress);
    }

    @Override
    public List<UserAddress> findUserAddressListByUserId(UserAddress userAddress) {
        return userAddressMapper.select(userAddress);
    }
}
