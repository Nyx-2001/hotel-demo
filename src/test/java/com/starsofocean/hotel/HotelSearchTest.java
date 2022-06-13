package com.starsofocean.hotel;

import com.alibaba.fastjson.JSON;
import com.jayway.jsonpath.internal.Utils;
import com.starsofocean.hotel.pojo.Hotel;
import com.starsofocean.hotel.pojo.HotelDoc;
import com.starsofocean.hotel.service.IHotelService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class HotelSearchTest {
    private RestHighLevelClient client;

    @Test
    void testInit(){
        System.out.println(client);
    }

    @Test
    void matchAll() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchAllQuery());
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        handleResponse(response);
    }

    @Test
    void match() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchQuery("all","如家"));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        handleResponse(response);
    }

    @Test
    void boolQuery() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.termQuery("city","上海"))
                 .filter(QueryBuilders.rangeQuery("price").lt(500));
        request.source().query(boolQuery);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        handleResponse(response);
    }

    @Test
    void pageQueryAndSort() throws IOException {
        int page=3,size=5;
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().from((page-1)*size).size(size);
        request.source().sort("price", SortOrder.ASC);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        handleResponse(response);
    }

    @Test
    void keywordHighlight() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchQuery("all","如家"));
        request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        handleResponse(response);
    }

    //抽取代码快捷键ctrl+alt+m
    private void handleResponse(SearchResponse response) {
        SearchHits hits = response.getHits();
        long total = hits.getTotalHits().value;
        SearchHit[] searchHits = hits.getHits();
        System.out.println("共搜索到:" + total + "条数据");
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
            System.out.println(hotelDoc);
        }
    }

    @BeforeEach
    void setUp(){
        this.client=new RestHighLevelClient(RestClient.builder(
                HttpHost.create("")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }
}
