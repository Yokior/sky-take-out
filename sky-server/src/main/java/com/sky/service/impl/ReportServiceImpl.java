package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
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

    /**
     * 营业额数据统计
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
            hashMap.put("begin",beginTime);
            hashMap.put("end",endTime);
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
}
