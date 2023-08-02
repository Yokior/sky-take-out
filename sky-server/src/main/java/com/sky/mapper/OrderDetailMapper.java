package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper
{
    /**
     * 批量插入订单明细数据
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    /**
     * 根据OrderId查询
     * @param id
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> selectByOrderId(Long orderId);
}
