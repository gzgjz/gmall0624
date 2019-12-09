package com.atguigu.gmall0624.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0624.bean.BaseSaleAttr;
import com.atguigu.gmall0624.bean.SpuInfo;
import com.atguigu.gmall0624.service.ManageService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class SpuManageController {

    @Reference
    private ManageService manageService;

    //'http://localhost:8082/spuList?catalog3Id=61
    @RequestMapping("spuList")
    public List<SpuInfo> spuList(SpuInfo spuInfo){
        return manageService.spuList(spuInfo);
    }

    //http://localhost:8082/baseSaleAttrList
    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> baseSaleAttrList(){
        return manageService.baseSaleAttrList();
    }

    //http://localhost:8082/saveSpuInfo
    @RequestMapping("saveSpuInfo")
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
    }
}
