package com.starsofocean.hotel.pojo;

import lombok.Data;

import java.util.List;

@Data
public class PageResult{
    private Long total;
    private List<HotelDoc> hotels;
}
