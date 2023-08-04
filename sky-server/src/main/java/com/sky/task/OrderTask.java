package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask
{
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     * 每分钟处理一次
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void processTimeOutOrder()
    {
        // 下单后15分钟没有支付 处理订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT,time);

        if (ordersList != null && ordersList.size() > 0)
        {
            ordersList.stream()
                    .forEach(orders ->
                    {
                        orders.setStatus(Orders.CANCELLED);
                        orders.setCancelReason("订单超时自动取消");
                        orders.setCancelTime(LocalDateTime.now());
                        orderMapper.update(orders);
                    });
            log.info("已自动处理超时订单！");
        }
    }

    /**
     * 一直处于派送中的订单
     * 每天凌晨1点处理
     */
    @Scheduled(cron = "0 0 1 * * ? ")
    public void processDeliveryOrder()
    {

        LocalDateTime time = LocalDateTime.now().plusHours(-1);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        if (ordersList != null && ordersList.size() > 0)
        {
            ordersList.stream()
                    .forEach(orders ->
                    {
                        orders.setStatus(Orders.COMPLETED);
                        orderMapper.update(orders);
                    });
            log.info("已自动处理上一天的派送中订单！");
        }
    }
}
