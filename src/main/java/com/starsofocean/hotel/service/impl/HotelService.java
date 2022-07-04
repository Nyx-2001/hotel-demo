package com.starsofocean.hotel.service.impl;

import com.alibaba.fastjson.JSON;
import com.starsofocean.hotel.mapper.HotelMapper;
import com.starsofocean.hotel.pojo.Hotel;
import com.starsofocean.hotel.pojo.HotelDoc;
import com.starsofocean.hotel.pojo.PageResult;
import com.starsofocean.hotel.pojo.RequestParams;
import com.starsofocean.hotel.service.IHotelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Resource
    private RestHighLevelClient client;

    /**
     * 根据条件查询并分页展示
     * @param requestParams
     * @return
     */
    @Override
    public PageResult search(RequestParams requestParams) {
        try {
            //1、准备request
            SearchRequest request=new SearchRequest("hotel");
            //2、构建查询条件query
            buildBasicQuery(requestParams,request);
            //3、分页
            Integer page = requestParams.getPage();
            Integer size = requestParams.getSize();
            request.source().from((page-1)*size).size(size);
            //4、排序
            String location = requestParams.getLocation();
            if(location != null && !location.equals("")){
                request.source().sort("price", SortOrder.ASC);
                request.source().sort(SortBuilders
                        .geoDistanceSort("location",new GeoPoint(location))
                        .order(SortOrder.ASC)
                        .unit(DistanceUnit.KILOMETERS));
            }
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            return handleResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 数据聚合返回
     * @return
     */
    @Override
    public Map<String, List<String>> filters(RequestParams requestParams) {
        try {
            //准备request
            SearchRequest request = new SearchRequest("hotel");
            //准备DSL，query,设置size
            buildBasicQuery(requestParams,request);
            request.source().size(0);
            //聚合
            buildAggregation(request);
            //发出请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //解析结果
            Map<String,List<String>> result=new HashMap<>();
            Aggregations aggregations = response.getAggregations();
            //根据聚合名称获取聚合结果
            List<String> brandList = getAggByName(aggregations,"brandAgg");
            result.put("brand",brandList);
            List<String> cityList = getAggByName(aggregations,"cityAgg");
            result.put("city",cityList);
            List<String> starList = getAggByName(aggregations,"starAgg");
            result.put("starName",starList);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 自动搜索补全
     * @param key
     * @return
     */
    @Override
    public List<String> getSuggestions(String key) {
        try {
            //准备request
            SearchRequest request = new SearchRequest("hotel");
            //准备DSL
            request.source().suggest(new SuggestBuilder().addSuggestion(
                    "suggestions",
                    SuggestBuilders.completionSuggestion("suggestion")
                    .prefix(key)
                    .skipDuplicates(true)
                    .size(10)
            ));
            //发起请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //解析结果
            Suggest suggest = response.getSuggest();
            //根据补全查询名称，获取补全结果
            CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
            //获取options
            List<CompletionSuggestion.Entry.Option> options = suggestions.getOptions();
            //遍历
            List<String> list=new ArrayList<>(options.size());
            for (CompletionSuggestion.Entry.Option option : options) {
                String text = option.getText().toString();
                list.add(text);
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * elasticsearch数据的新增和修改
     * @param id
     */
    @Override
    public void insertById(Long id) {
        try {
            //根据id查数据
            Hotel hotel = getById(id);
            //转换为文档数据
            HotelDoc hotelDoc=new HotelDoc(hotel);
            //准备request对象
            IndexRequest request = new IndexRequest("hotel").id(hotel.getId().toString());
            //准备json文档
            request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
            //发送请求
            client.index(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * elasticsearch数据的删除
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        try {
            //准备request
            DeleteRequest request = new DeleteRequest("hotel", id.toString());
            //发送请求
            client.delete(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getAggByName(Aggregations aggregations,String aggName) {
        Terms terms = aggregations.get(aggName);
        //获取bucket
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        //遍历
        List<String> brandList=new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            String key = bucket.getKeyAsString();
            brandList.add(key);
        }
        return brandList;
    }

    private void buildAggregation(SearchRequest request) {
        request.source().aggregation(AggregationBuilders
                        .terms("brandAgg")
                        .field("brand")
                        .size(100));
        request.source().aggregation(AggregationBuilders
                .terms("cityAgg")
                .field("city")
                .size(100));
        request.source().aggregation(AggregationBuilders
                .terms("starAgg")
                .field("starName")
                .size(100));
    }

    //条件查询
    private void buildBasicQuery(RequestParams requestParams,SearchRequest request) {
        //1、原始查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        String key= requestParams.getKey();
        if(key==null||"".equals(key)){
            boolQuery.must(QueryBuilders.matchAllQuery());
        }
        else{
            boolQuery.must(QueryBuilders.matchQuery("all",key));
        }
        if(requestParams.getCity()!=null && !requestParams.getCity().equals("")){
            boolQuery.filter(QueryBuilders.termQuery("city", requestParams.getCity()));
        }
        if(requestParams.getBrand()!=null && !requestParams.getBrand().equals("")){
            boolQuery.filter(QueryBuilders.termQuery("brand", requestParams.getBrand()));
        }
        if(requestParams.getStarName()!=null && !requestParams.getStarName().equals("")){
            boolQuery.filter(QueryBuilders.termQuery("starName", requestParams.getStarName()));
        }
        if(requestParams.getMinPrice()!=null && requestParams.getMaxPrice()!=null){
            boolQuery.filter(QueryBuilders.rangeQuery("price")
                     .gte(requestParams.getMinPrice())
                     .lte(requestParams.getMaxPrice()));
        }
        //2、算分控制
        FunctionScoreQueryBuilder functionScoreQuery =
                QueryBuilders.functionScoreQuery(
                        //原始查询，相关性算分
                        boolQuery,
                        //function score数组
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                                //一个function score元素
                                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                        //过滤条件
                                        QueryBuilders.termQuery("isAD",true),
                                        //算分函数
                                        ScoreFunctionBuilders.weightFactorFunction(5)
                                )
                        });

        request.source().query(functionScoreQuery);
    }

    //解析响应
    private PageResult handleResponse(SearchResponse response) {
        SearchHits hits = response.getHits();
        long total = hits.getTotalHits().value;
        SearchHit[] searchHits = hits.getHits();
        PageResult result = new PageResult();
        result.setTotal(total);
        List<HotelDoc> hotelList = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            String json = searchHit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
            Object[] sortValues = searchHit.getSortValues();
            if(sortValues.length>0){
                Object distance = sortValues[0];
                hotelDoc.setDistance(distance);
            }
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                HighlightField highlightField = highlightFields.get("name");
                if (highlightField!=null) {
                    String name = highlightField.getFragments()[0].string();
                    hotelDoc.setName(name);
                }
            }
            hotelList.add(hotelDoc);
        }
        result.setHotels(hotelList);
        return result;
    }
}
