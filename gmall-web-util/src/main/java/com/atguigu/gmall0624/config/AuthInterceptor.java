package com.atguigu.gmall0624.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0624.passport.config.CookieUtil;
import com.atguigu.gmall0624.passport.config.WebConst;
import com.atguigu.gmall0624.utils.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Map;

// 拦截器
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{

    // 进入控制器之前
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("--------------preHandle-------------");

        String token = request.getParameter("newToken");
        // 判断token
        if (token!=null){
            // 放入cookie
            CookieUtil.setCookie(request, response,"token",token, WebConst.COOKIE_MAXAGE,false);
        }

        if (token==null){
            token = CookieUtil.getCookieValue(request,"token",false);
        }
        // 获取用户的昵称
        if (token!=null){
            Map map = makeUserInfo(token);

            String nickName = (String) map.get("nickName");
            // 保存到作用域
            request.setAttribute("nickName",nickName);
        }

        HandlerMethod handlerMethod = (HandlerMethod)handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if(methodAnnotation != null){
            String salt = request.getHeader("X-forwarded-for");
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);
            if("success".equals(result)){
                //登陆成功，记录userId
                Map map = makeUserInfo(token);

                String userId = (String) map.get("userId");
                // 保存到作用域
                request.setAttribute("nickName",userId);

                return true;
            }else {
                if(methodAnnotation.autoRedirect()){
                    String requestURL = request.getRequestURL().toString();
                    System.out.println(requestURL);
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    System.out.println(encodeURL);
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    return false;

                }
            }
        }

        // 放行拦截器：
        return true;
    }

    // 获取用户信息
    private Map makeUserInfo(String token) {
        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        // 创建base64 对象
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] bytes = base64UrlCodec.decode(tokenUserInfo);

        String userJson = new String(bytes);

        Map map = JSON.parseObject(userJson, Map.class);
        System.out.println(map);
        return map;


    }

    // 进入控制器之后，返回视图之前
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }
    // 视图渲染之后
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

}
