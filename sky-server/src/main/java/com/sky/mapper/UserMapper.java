package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.Map;

@Mapper
public interface UserMapper
{
    /**
     * 根据openid返回用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);
    
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 根据map查询
     * @param hashMap
     * @return
     */
    Integer countByMap(Map hashMap);
}
