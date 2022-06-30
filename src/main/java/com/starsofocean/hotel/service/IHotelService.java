package com.starsofocean.hotel.service;

import com.starsofocean.hotel.pojo.Hotel;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starsofocean.hotel.pojo.PageResult;
import com.starsofocean.hotel.pojo.RequestParams;

public interface IHotelService extends IService<Hotel> {
    PageResult search(RequestParams requestParams);
}
