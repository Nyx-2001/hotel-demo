package com.starsofocean.hotel.service.impl;

import com.alibaba.fastjson.JSON;
import com.starsofocean.hotel.mapper.HotelMapper;
import com.starsofocean.hotel.pojo.Hotel;
import com.starsofocean.hotel.pojo.HotelDoc;
import com.starsofocean.hotel.pojo.PageResult;
import com.starsofocean.hotel.pojo.RequestParams;
import com.starsofocean.hotel.service.IHotelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
    @Resource
    private RestHighLevelClient client;
    @Override
    public PageResult search(RequestParams requestParams) {
        try {
            SearchRequest request=new SearchRequest("hotel");
            buildBasicQuery(requestParams,request);
            Integer page = requestParams.getPage();
            Integer size = requestParams.getSize();
            request.source().from((page-1)*size).size(size);
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            return handleResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildBasicQuery(RequestParams requestParams,SearchRequest request) {
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
        request.source().query(boolQuery);
    }

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
