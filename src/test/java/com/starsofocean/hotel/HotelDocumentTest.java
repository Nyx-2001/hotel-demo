package com.starsofocean.hotel;

import com.alibaba.fastjson.JSON;
import com.starsofocean.hotel.pojo.Hotel;
import com.starsofocean.hotel.pojo.HotelDoc;
import com.starsofocean.hotel.service.IHotelService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static com.starsofocean.hotel.constants.HotelConstants.MAPPING_TEMPLATE;

@SpringBootTest
public class HotelDocumentTest {
    private RestHighLevelClient client;

    @Autowired
    private IHotelService iHotelService;
    @Test
    void testInit(){
        System.out.println(client);
    }

    @Test
    void addDocument() throws IOException {
        Hotel hotel = iHotelService.getById(56392L);
        HotelDoc hotelDoc=new HotelDoc(hotel);
        IndexRequest request=new IndexRequest("hotel").id(hotelDoc.getId().toString());
        request.source(JSON.toJSONString(hotelDoc),XContentType.JSON);
        client.index(request,RequestOptions.DEFAULT);
    }

    @Test
    void getDocumentById() throws IOException {
        GetRequest request=new GetRequest("hotel","56392");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String json = response.getSourceAsString();
        HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
        System.out.println(hotelDoc);
    }

    @Test
    void deleteDocumentById() throws IOException {
        DeleteRequest request=new DeleteRequest("hotel","56392");
        client.delete(request,RequestOptions.DEFAULT);
    }

    @Test
    void bulkRequest() throws IOException {
        BulkRequest request = new BulkRequest();
        List<Hotel> hotels = iHotelService.list();
        for (Hotel hotel : hotels) {
            HotelDoc hotelDoc = new HotelDoc(hotel);
            request.add(new IndexRequest("hotel")
                    .id(hotelDoc.getId().toString())
                    .source(JSON.toJSONString(hotelDoc),XContentType.JSON));
        }
        client.bulk(request,RequestOptions.DEFAULT);
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
