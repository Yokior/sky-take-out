package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper
{
    /**
     * 根据菜品Id查询多个对应的菜品
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量添加套餐菜品关联表
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);
}
