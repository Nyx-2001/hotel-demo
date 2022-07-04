package com.starsofocean.hotel.service;

import com.starsofocean.hotel.pojo.Hotel;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starsofocean.hotel.pojo.PageResult;
import com.starsofocean.hotel.pojo.RequestParams;

import java.util.List;
import java.util.Map;

public interface IHotelService extends IService<Hotel> {

    PageResult search(RequestParams requestParams);

    Map<String, List<String>> filters(RequestParams requestParams);

    List<String> getSuggestions(String key);

    void insertById(Long id);

    void deleteById(Long id);
}
