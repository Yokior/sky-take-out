package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService
{
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

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
     *
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
                .dateList(StringUtils.join(dateArrayList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量排名Top10
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end)
    {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime, endTime);

        List<String> stringList = goodsSalesDTOList.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.toList());

        List<Integer> integerList = goodsSalesDTOList.stream()
                .map(GoodsSalesDTO::getNumber)
                .collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(stringList, ","))
                .numberList(StringUtils.join(integerList, ","))
                .build();
    }

    /**
     * 导出运营数据报表
     *
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response)
    {
        // 查询数据库 获取营业数据 查询最近30天数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);

        BusinessDataVO businessData = workspaceService.getBusinessData(begin, end);

        // 通过POI将数据写入excel文件中
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try
        {
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            // 填充数据
            XSSFSheet sheet = excel.getSheet("Sheet1");
            // 时间
            sheet.getRow(1).getCell(1).setCellValue("时间" + dateBegin + "至" + dateEnd);
            // 获取第4行
            XSSFRow row4 = sheet.getRow(3);
            // 营业额
            row4.getCell(2).setCellValue(businessData.getTurnover());
            // 订单完成率
            row4.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            // 新增用户
            row4.getCell(6).setCellValue(businessData.getNewUsers());

            // 获取第5行
            XSSFRow row5 = sheet.getRow(4);
            // 有效订单
            row5.getCell(2).setCellValue(businessData.getValidOrderCount());
            // 平均客单价
            row5.getCell(4).setCellValue(businessData.getUnitPrice());

            // 填充明细数据
            for (int i = 0; i < 30; i++)
            {
                LocalDate date = dateBegin.plusDays(i);
                // 查询某一天的营业数据
                BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                XSSFRow row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessDataVO.getTurnover());
                row.getCell(3).setCellValue(businessDataVO.getValidOrderCount());
                row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessDataVO.getUnitPrice());
                Integer newUsers = businessDataVO.getNewUsers();
                if (newUsers == null)
                {
                    newUsers = 0;
                }
                row.getCell(6).setCellValue(newUsers);
            }

            // 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            outputStream.close();
            excel.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * 根据条件统计订单数量
     *
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCountByMap(LocalDateTime begin, LocalDateTime end, Integer status)
    {
        HashMap hashMap = new HashMap();
        hashMap.put("begin", begin);
        hashMap.put("end", end);
        hashMap.put("status", status);

        Integer count = orderMapper.countByMap(hashMap);
        return count;
    }
}
