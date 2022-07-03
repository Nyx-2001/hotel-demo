package com.starsofocean.hotel;

import com.starsofocean.hotel.service.IHotelService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class HotelDemoApplicationTest {

    @Resource
    private IHotelService iHotelService;

//    @Test
//    void contextLoads(){
//        Map<String, List<String>> filters = iHotelService.filters();
//        System.out.println(filters);
//    }
}
