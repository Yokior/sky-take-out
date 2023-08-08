package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService
{
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额数据统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end)
    {
        // 用于存放从begin到end之间的日期集合
        List<LocalDate> dateArrayList = new ArrayList<>();
        // 用于存放每天的营业集合
        List<Double> turnoverList = new ArrayList<>();

        dateArrayList.add(begin);

        LocalDate temp = begin;
        while (!temp.equals(end))
        {
            // 日期计算
            temp = temp.plusDays(1);
            dateArrayList.add(temp);
            // 营业额计算
            // 查询date日期对应的营业额数据 状态为已完成的订单金额总额
            LocalDateTime beginTime = LocalDateTime.of(temp, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(temp, LocalTime.MAX);

            HashMap hashMap = new HashMap();
            hashMap.put("begin", beginTime);
            hashMap.put("end", endTime);
            hashMap.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(hashMap);
            if (turnover == null)
            {
                turnover = 0.0;
            }

            turnoverList.add(turnover);
        }

        String dateList = StringUtils.join(dateArrayList, ",");
        String turnoverList2 = StringUtils.join(turnoverList, ",");

        return TurnoverReportVO.builder()
                .dateList(dateList)
                .turnoverList(turnoverList2)
                .build();
    }

    /**
     * 用户数据统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end)
    {
        // 用于存放从begin到end之间的日期集合
        List<LocalDate> dateArrayList = new ArrayList<>();

        // 每天新增用户数量
        List<Integer> newUserList = new ArrayList<>();
        // 用户总数
        List<Integer> totalUserList = new ArrayList<>();

        dateArrayList.add(begin);

        LocalDate temp = begin;
        while (!temp.equals(end))
        {
            // 日期计算
            temp = temp.plusDays(1);
            dateArrayList.add(temp);

            HashMap hashMap = new HashMap();
            LocalDateTime minTime = LocalDateTime.of(temp, LocalTime.MIN);
            LocalDateTime maxTime = LocalDateTime.of(temp, LocalTime.MAX);
            hashMap.put("end", maxTime);
            // 总用户数量
            Integer totalUserCount = userMapper.countByMap(hashMap);
            if (totalUserCount == null)
            {
                totalUserCount = 0;
            }
            totalUserList.add(totalUserCount);

            hashMap.put("begin", minTime);
            // 新增用户数量
            Integer dayNewUserCount = userMapper.countByMap(hashMap);
            if (dayNewUserCount == null)
            {
                dayNewUserCount = 0;
            }
            newUserList.add(dayNewUserCount);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateArrayList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 订单数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end)
    {
        // 用于存放从begin到end之间的日期集合
        List<LocalDate> dateArrayList = new ArrayList<>();

        // 存放每天的订单总数
        List<Integer> orderCountList = new ArrayList<>();

        // 存放每天的有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();

        dateArrayList.add(begin);

        LocalDate temp = begin;
        while (!temp.equals(end))
        {
            // 日期计算
            temp = temp.plusDays(1);
            dateArrayList.add(temp);

            LocalDateTime beginTime = LocalDateTime.of(temp, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(temp, LocalTime.MAX);
            // 查询每天订单总数
            Integer orderCount = getOrderCountByMap(beginTime, endTime, null);
            orderCountList.add(orderCount);
            // 查询每天的有效订单数
            Integer validOrderCount = getOrderCountByMap(beginTime, endTime, Orders.COMPLETED);
            validOrderCountList.add(validOrderCount);
        }

        // 计算时间区间内订单的总数量
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();

        // 计算时间区间内的有效订单数量
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        // 计算订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0)
        {
           orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateArrayList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 根据条件统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCountByMap(LocalDateTime begin,LocalDateTime end,Integer status)
    {
        HashMap hashMap = new HashMap();
        hashMap.put("begin",begin);
        hashMap.put("end",end);
        hashMap.put("status",status);

        Integer count = orderMapper.countByMap(hashMap);
        return count;
    }
}
