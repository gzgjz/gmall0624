package com.atguigu.gmall0624.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0624.bean.SkuLsParams;
import com.atguigu.gmall0624.bean.SkuLsResult;
import com.atguigu.gmall0624.service.ListService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @RequestMapping("list.html")
    @ResponseBody
    public String getLitst(SkuLsParams skuLsParams){
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        return JSON.toJSONString(skuLsResult);
    }
}
