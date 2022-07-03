package com.starsofocean.hotel.controller;


import com.starsofocean.hotel.pojo.PageResult;
import com.starsofocean.hotel.pojo.RequestParams;
import com.starsofocean.hotel.service.IHotelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hotel")
public class HotelController {

    @Resource
    private IHotelService iHotelService;

    @PostMapping("/list")
    public PageResult search(@RequestBody RequestParams requestParams){
        return iHotelService.search(requestParams);
    }

    @PostMapping("/filters")
    public Map<String, List<String>> getFilters(@RequestBody RequestParams requestParams){
        return iHotelService.filters(requestParams);
    }

    @GetMapping("/suggestion")
    public List<String> getSuggestions(@RequestParam("key") String key){
        return iHotelService.getSuggestions(key);

    }
}
