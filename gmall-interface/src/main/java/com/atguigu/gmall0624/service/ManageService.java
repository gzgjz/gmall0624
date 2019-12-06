package com.atguigu.gmall0624.service;

import com.atguigu.gmall0624.bean.*;

import java.util.List;

public interface ManageService {

    //一级分类
    List<BaseCatalog1> getCatalog1();

    //二级分类
    List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2);

    //三级分类
    List<BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3);

    //详细信息
    List<BaseAttrInfo> attrInfoList(BaseAttrInfo baseAttrInfo);

    //添加属性
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    public List<BaseAttrValue> getAttrValueList(String attrId);

    BaseAttrInfo getAtrrInfo(String attrId);
}
