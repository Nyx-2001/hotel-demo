package com.starsofocean.hotel.service.impl;

import com.starsofocean.hotel.mapper.HotelMapper;
import com.starsofocean.hotel.pojo.Hotel;
import com.starsofocean.hotel.service.IHotelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
}
