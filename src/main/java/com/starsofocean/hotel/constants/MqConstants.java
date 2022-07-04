package com.starsofocean.hotel.constants;

public class MqConstants {

    /**
     * 交换机
     */
    public final static String HOTEL_EXCHANGE="hotel.topic";

    /**
     * 监听新增和修改的消息队列
     */
    public final static String HOTEL_INSERT_QUEUE="hotel.insert.queue";

    /**
     * 监听删除的消息队列
     */
    public final static String HOTEL_DELETE_QUEUE="hotel.delete.queue";

    /**
     * 新增和修改的RoutingKey
     */
    public final static String HOTEL_INSERT_KEY="hotel.insert.key";

    /**
     * 删除的RoutingKey
     */
    public final static String HOTEL_DELETE_KEY="hotel.delete.key";
}
