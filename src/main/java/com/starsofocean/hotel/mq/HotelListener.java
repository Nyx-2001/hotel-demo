package com.starsofocean.hotel.mq;

import com.starsofocean.hotel.constants.MqConstants;
import com.starsofocean.hotel.service.IHotelService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class HotelListener {

    @Resource
    private IHotelService iHotelService;

    /**
     * 监听酒店新增和修改业务
     * @param id
     */
    @RabbitListener(queues = MqConstants.HOTEL_INSERT_QUEUE)
    public void listenHotelInsertOrUpdate(Long id){
        iHotelService.insertById(id);
    }

    /**
     * 监听酒店删除业务
     * @param id
     */
    @RabbitListener(queues = MqConstants.HOTEL_DELETE_QUEUE)
    public void listenHotelDelete(Long id){
        iHotelService.deleteById(id);
    }
}
