package com.atguigu.gmall0624.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0624.bean.OrderDetail;
import com.atguigu.gmall0624.bean.OrderInfo;
import com.atguigu.gmall0624.bean.enums.OrderStatus;
import com.atguigu.gmall0624.bean.enums.ProcessStatus;
import com.atguigu.gmall0624.config.RedistUtil;
import com.atguigu.gmall0624.order.mapper.OrderDetailMapper;
import com.atguigu.gmall0624.order.mapper.OrderInfoMapper;
import com.atguigu.gmall0624.service.OrderService;
import com.atguigu.gmall0624.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    // 调用mapper
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedistUtil redistUtil;

    @Override
    @Transactional
    public String saveOrder(OrderInfo orderInfo) {
        // 两张表 orderInfo ,orderDetail
        // 总金额，订单状态，[用户Id]，第三方交易变化，创建时间，过期时间，进程状态
        orderInfo.sumTotalAmount();
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        // 支付使用
        String outTradeNo = "ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setCreateTime(new Date());
        // 过期时间：+1天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);

        orderInfo.setExpireTime(calendar.getTime());

        orderInfo.setProcessStatus(ProcessStatus.UNPAID);

        orderInfoMapper.insertSelective(orderInfo);

        // 保存订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (orderDetailList!=null && orderDetailList.size()>0){
            for (OrderDetail orderDetail : orderDetailList) {
                orderDetail.setId(null);
                orderDetail.setOrderId(orderInfo.getId());
                orderDetailMapper.insertSelective(orderDetail);
            }
        }
        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {
        //流水号
        String outTradeNo = UUID.randomUUID().toString().replace("-","");
        Jedis jedis = redistUtil.getJedis();
        //key
        String tradeNoKey = "user:"+userId+":tradeCode";
        jedis.set(tradeNoKey,outTradeNo);
        jedis.close();
        return outTradeNo;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {

        Jedis jedis = redistUtil.getJedis();
        //key
        String tradeNoKey = "user:"+userId+":tradeCode";
        String redisTradeNo = jedis.get(tradeNoKey);

        jedis.close();
        return tradeCodeNo.equals(redisTradeNo);
    }

    @Override
    public void deleteTradeCode(String userId) {
        Jedis jedis = redistUtil.getJedis();
        //key
        String tradeNoKey = "user:"+userId+":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        //http://www.gware.com /hasStock?skuId=10221&num=2
        String res = HttpClientUtil.doGet("http://www.gware.com /hasStock?skuId=" + skuId + "&num=" + skuNum);
        if("1".equals(res)){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        return orderInfoMapper.selectByPrimaryKey(orderId);
    }
}
