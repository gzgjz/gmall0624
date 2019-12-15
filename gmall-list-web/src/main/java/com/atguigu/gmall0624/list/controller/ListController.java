package com.atguigu.gmall0624.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0624.bean.*;
import com.atguigu.gmall0624.service.ListService;
import com.atguigu.gmall0624.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
    //@ResponseBody
    public String getLitst(SkuLsParams skuLsParams, HttpServletRequest request){
        //分页设置
        skuLsParams.setPageSize(3);

        SkuLsResult skuLsResult = listService.search(skuLsParams);
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();

        // 获取到平台属性值Id 集合 (171,81,120,167,82,83)
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        // 调用方法将Id 集合传入
        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrList(attrValueIdList);
        // 保存到作用域
        // 如何保存用户查询的条件
        String urlParam = makeUrlParam(skuLsParams);

        // 声明一个面包屑集合
        ArrayList<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();

        // 点击平台属性值过滤时，平台属性消失 ---- itar，iter，itco ?
        if (baseAttrInfoList!=null && baseAttrInfoList.size()>0){

            for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo = iterator.next();
                // 得到平台属性值集合
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

                if (skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
                    for (String valueId : skuLsParams.getValueId()) {
                        for (BaseAttrValue baseAttrValue : attrValueList) {
                            if (valueId.equals(baseAttrValue.getId())){
                                iterator.remove();
                                // 声明一个平台属性值对象
                                BaseAttrValue baseAttrValueed = new BaseAttrValue();

                                // 组成面包屑： 平台属性名称：平台属性值名称
                                // baseAttrValueed.valueName 做成了面包屑
                                baseAttrValueed.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());

                                // 制作新的urlParam
                                String newUrlParam = makeUrlParam(skuLsParams, valueId);
                                // 将最新的参数付给当前变量
                                baseAttrValueed.setUrlParam(newUrlParam);

                                baseAttrValueArrayList.add(baseAttrValueed);

                            }
                        }
                    }
                }
            }
        }

        request.setAttribute("totalPages",skuLsResult.getTotalPages());
        request.setAttribute("pageNo",skuLsParams.getPageNo());
        request.setAttribute("baseAttrValueArrayList",baseAttrValueArrayList);
        request.setAttribute("keyword",skuLsParams.getKeyword());
        request.setAttribute("urlParam",urlParam);
        request.setAttribute("baseAttrInfoList",baseAttrInfoList);
        request.setAttribute("skuLsInfoList",skuLsInfoList);
        return "list";
    }

    private String makeUrlParam(SkuLsParams skuLsParams,String... excludeValueIds) {
        String urlParam = "";
        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }

        // 用户走的是三级分类Id
        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }

        // 判断是否有平台属性值Id
        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            for (String valueId : skuLsParams.getValueId()) {
                // 用户点击时的平台属性值Id
                if (excludeValueIds!=null && excludeValueIds.length>0){
                    String excludeValueId = excludeValueIds[0];
                    if (valueId.equals(excludeValueId)){
                        continue;
                    }
                }

                // 有平台属性值Id是要拼接 &
                urlParam+="&valueId="+valueId;
            }
        }

        return urlParam;
    }
}
