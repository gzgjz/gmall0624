package com.atguigu.gmall0624.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0624.bean.UserAddress;
import com.atguigu.gmall0624.bean.UserInfo;
import com.atguigu.gmall0624.config.RedistUtil;
import com.atguigu.gmall0624.service.UserInfoService;
import com.atguigu.gmall0624.user.mapper.UserAddressMapper;
import com.atguigu.gmall0624.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService{

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedistUtil redistUtil;

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24*7;

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

    //登陆
    @Override
    public UserInfo login(UserInfo userInfo) {

        //密码加密
        String passwd = userInfo.getPasswd();
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(newPasswd);

        UserInfo info = userInfoMapper.selectOne(userInfo);
        if(info != null){
            Jedis jedis = redistUtil.getJedis();
            String userKey = userKey_prefix+info.getId()+userinfoKey_suffix;

            jedis.setex(userKey,userKey_timeOut, JSON.toJSONString(info));
            jedis.close();

            return info;
        }
        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        Jedis jedis = redistUtil.getJedis();
        String userKey = userKey_prefix+userId+userinfoKey_suffix;
        String userJson = jedis.get(userKey);
        if(!StringUtils.isEmpty(userId)){
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }
        jedis.close();
        return null;
    }
}
