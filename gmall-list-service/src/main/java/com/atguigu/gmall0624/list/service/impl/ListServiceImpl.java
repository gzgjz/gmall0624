package com.atguigu.gmall0624.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0624.bean.SkuLsInfo;
import com.atguigu.gmall0624.bean.SkuLsParams;
import com.atguigu.gmall0624.bean.SkuLsResult;
import com.atguigu.gmall0624.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private JestClient jestClient;

    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";

    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {
        Index build =
                new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();
        try {
            jestClient.execute(build);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {

        //dsl语句
        String query = makeQueryStringForSearch(skuLsParams);

        //定义执行动作
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();

        SearchResult result = null;
        //执行
        try {
            result = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkuLsResult skuLsResult = makeResultForSearch(result,skuLsParams);

        //返回数据

        return skuLsResult;
    }

    //返回值
    private SkuLsResult makeResultForSearch(SearchResult result,SkuLsParams skuLsParams) {
        SkuLsResult skuLsResult = new SkuLsResult();

        List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
        List<SearchResult.Hit<SkuLsInfo,Void>> hits = result.getHits(SkuLsInfo.class);
        if (hits != null && hits.size()>0){
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo skuLsInfo = hit.source;

                //获取高亮的skuName
                if(hit.highlight != null && hit.highlight.size() > 0){
                    List<String> list = hit.highlight.get("skuName");

                    String skuNameHI = list.get(0);
                    skuLsInfo.setSkuName(skuNameHI);
                }

                skuLsInfoList.add(skuLsInfo);
            }
        }

        skuLsResult.setSkuLsInfoList(skuLsInfoList);
        //查询条数
        skuLsResult.setTotal(result.getTotal());
        //页数
        long totalPages = (result.getTotal()+skuLsParams.getPageSize()-1)/skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPages);

        List<String> stringList = new ArrayList<>();
        // stringList 赋值 平台属性值Id 添加到集合
        TermsAggregation groupby_attr = result.getAggregations().getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        if (buckets!=null && buckets.size()>0){
            for (TermsAggregation.Entry bucket : buckets) {
                String valueId = bucket.getKey();
                stringList.add(valueId);
            }
        }

        skuLsResult.setAttrValueIdList(stringList);
        return skuLsResult;
    }

    //dsl语句
    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        //创建查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if(skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0){
            //说明用户第一次访问是通过三级id检索的
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }

        if(skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
            //平台属性值不为空
            //需要循环遍历
            for (String valueId : skuLsParams.getValueId()) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }

        }

        if(skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length()>0){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",skuLsParams.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);

            //判断高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();

            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style=color:red>");
            highlightBuilder.postTags("</span>");

            searchSourceBuilder.highlight(highlightBuilder);
        }
        //排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        //分页
        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());

        //聚合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);
        searchSourceBuilder.query(boolQueryBuilder);
        String query = searchSourceBuilder.toString();
        System.out.println("query:"+query);
        return query;
    }
}
