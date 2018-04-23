package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.util.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.action.update.UpdateAction;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: ruochen
 * Date:2018/4/18 0018
 */
@Service
public class ListServiceImpl implements ListService {

    public static final String ES_INDEX_NAME="gmall_index";

    public static final String ES_TYPE_NAME="SkuInfo";

    @Autowired
    JestClient jestClient;

    @Autowired
    RedisUtil redisUtil;

    //向elasticsearch中插入数据
    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo){
        Index index =
                new Index.Builder(skuLsInfo).index(ES_INDEX_NAME).type(ES_TYPE_NAME).id(skuLsInfo.getId()).build();
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //从elasticsearch中查询筛选结果
    @Override
    public SkuLsResult search(SkuLsParams skuLsParams){
        String query = makeQueryStringForSearch(skuLsParams);

        Search search = new Search.Builder(query).addIndex(ES_INDEX_NAME).addType(ES_TYPE_NAME).build();

        SearchResult searchResult = null;

        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SkuLsResult skuLsResult = makeResultForSearch(skuLsParams,searchResult);

        return skuLsResult;

    }

    //生成elasticsearch查询语句
    private String makeQueryStringForSearch(SkuLsParams skuLsParams){
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (skuLsParams.getKeyword()!=null){
            //skuName关键字查询
            String keyword = skuLsParams.getKeyword();
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",keyword);
            boolQueryBuilder.must(matchQueryBuilder);

            //高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
            highlightBuilder.field("skuName");
            searchSourceBuilder.highlight(highlightBuilder);
        }
        //三级分类
        if (skuLsParams.getCatalog3Id()!=null){
            TermQueryBuilder termQueryBuilder =
                    new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        //平台属性
        if (skuLsParams.getValueId()!=null&&skuLsParams.getValueId().size()>0){
            List<String> valueIdList = skuLsParams.getValueId();
            for (String valueId : valueIdList) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }

        }
        searchSourceBuilder.query(boolQueryBuilder);

        //分组平台属性
        TermsBuilder groupby_valueId =
                AggregationBuilders.terms("groupby_valueId").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_valueId);


        //分页
        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());

        //排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        String query = searchSourceBuilder.toString();

       // System.err.println("query"+query);

        return query;
    }

    //处理elasticsearch查询结果
    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams,SearchResult searchResult){
        SkuLsResult skuLsResult = new SkuLsResult();

        //查询结果总数
        Long total = searchResult.getTotal();
        skuLsResult.setTotal(total);

        //总页数
        long totalPage =
                (total%skuLsParams.getPageSize()==0)?total%skuLsParams.getPageSize():total%skuLsParams.getPageSize()+1;
        skuLsResult.setTotalPages(totalPage);

        //平台属性集
        List<String> attrValueIdList =new ArrayList<>();
        MetricAggregation metricAggregation = searchResult.getAggregations();
        TermsAggregation termsAggregation = metricAggregation.getTermsAggregation("groupby_valueId");
        if (termsAggregation!=null){
            List<TermsAggregation.Entry> buckets = termsAggregation.getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                String key = bucket.getKey();
                attrValueIdList.add(key);
            }
            skuLsResult.setAttrValueIdList(attrValueIdList);
        }

        //skuLsInfo
        List<SkuLsInfo> skuLsInfoList =new ArrayList<>(skuLsParams.getPageSize());
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;
            //高亮
            Map<String, List<String>> highlight = hit.highlight;
            if (highlight!=null&&highlight.size()>0){
                List<String> skuNameList = highlight.get("skuName");
                skuLsInfo.setSkuName(skuNameList.get(0));
            }

            skuLsInfoList.add(skuLsInfo);
        }
        skuLsResult.setSkuLsInfoList(skuLsInfoList);

        return skuLsResult;
    }


    //更新热度评分
    @Override
    public void incrHotScore(String skuId) {
        Jedis jedis = redisUtil.getJedis();

        Double hotScore = jedis.zincrby("hotScore", 1, "skuId:" + skuId);

        if (hotScore%10 == 0) {
            updateHotScore(hotScore,skuId);
        }
        jedis.close();
    }

    private void updateHotScore(Double hotScore,String skuId){
        String updateJson="{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":"+hotScore+"\n" +
                "   }\n" +
                "}";

        Update update =
                new Update.Builder(updateJson).index(ES_INDEX_NAME).type(ES_TYPE_NAME).id(skuId).build();
        try {
            DocumentResult documentResult = jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
