package com.atguigu.gmall0624.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0624.bean.SkuInfo;
import com.atguigu.gmall0624.bean.SkuLsInfo;
import com.atguigu.gmall0624.bean.SpuImage;
import com.atguigu.gmall0624.bean.SpuSaleAttr;
import com.atguigu.gmall0624.service.ListService;
import com.atguigu.gmall0624.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class SkuManageController {

    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;

    //http://localhost:8082/spuImageList?spuId=61
    @RequestMapping("spuImageList")
    public List<SpuImage> spuImageList(SpuImage spuImage){
        return manageService.spuImageList(spuImage);
    }

    //http://localhost:8082/spuSaleAttrList?spuId=61
    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> spuSaleAttrList(String spuId){
        return manageService.spuSaleAttrList(spuId);
    }

    //http://localhost:8082/saveSkuInfo
    @RequestMapping("saveSkuInfo")
    public void saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);

        //提交审核流程，商品上架的申请

    }

    @RequestMapping("onSale")
    public void onSale(String skuId){
        SkuLsInfo skuLsInfo = new SkuLsInfo();

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        //给skulsinfo赋值
        BeanUtils.copyProperties(skuInfo,skuLsInfo);
        listService.saveSkuLsInfo(skuLsInfo);
    }
}
