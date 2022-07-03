package com.starsofocean.hotel.controller;


import com.starsofocean.hotel.pojo.PageResult;
import com.starsofocean.hotel.pojo.RequestParams;
import com.starsofocean.hotel.service.IHotelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/hotel")
public class HotelController {

    @Resource
    private IHotelService iHotelService;

    @PostMapping("/list")
    public PageResult search(@RequestBody RequestParams requestParams){
        return iHotelService.search(requestParams);
    }
}
