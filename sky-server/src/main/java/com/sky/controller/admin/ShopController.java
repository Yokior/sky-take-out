package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.RedisCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController
{
    private static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisCache redisCache;

    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status)
    {
        log.info("设置店铺营业状态:{}",status);
        redisCache.setCacheObject(KEY,status);
        return Result.success();
    }


    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus()
    {
        Integer shop_status = redisCache.getCacheObject(KEY);
        log.info("获取店铺营业状态:{}",shop_status);
        return Result.success(shop_status);
    }
}
