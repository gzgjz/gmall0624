package com.atguigu.gmall0624.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0624.bean.UserInfo;
import com.atguigu.gmall0624.passport.config.JwtUtil;
import com.atguigu.gmall0624.service.UserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    private UserInfoService userInfoService;

    @Value("${token.key}")
    private String key;

    @RequestMapping("index")
    public String index(HttpServletRequest request){

        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);

        return "index";
    }

    //登陆
    @RequestMapping("login")
    @ResponseBody
    public String login(HttpServletRequest request, UserInfo userInfo){
        //调用服务层
        UserInfo info = userInfoService.login(userInfo);

        if(info != null ){
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId",info.getId());
            map.put("nickName",info.getNickName());
            String salt = request.getHeader("X-forwarded-for");
            System.out.println(salt);
            String token = JwtUtil.encode(key, map, salt);
            return token;
        }

        //登陆失败
        return "fail";
    }

    //用户认证
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        //得到token
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");
        Map<String, Object> map = JwtUtil.decode(token, key, salt);
        if(map != null && map.size()>0){
            String userId = (String)(map.get("userId"));

            UserInfo userInfo = userInfoService.verify(userId);

            if(userInfo != null){
                //缓存中有数据
                return "success";
            }
        }
        return "fail";
    }

}
