package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService
{

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO)
    {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        // 新增套餐表信息
        setmealMapper.insert(setmeal);

        Long setmealId = setmeal.getId();

        // 新增套餐菜品关联信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.stream()
                .forEach(setmealDish ->
                {
                    setmealDish.setSetmealId(setmealId);
                });
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO)
    {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> setmealVOPage = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(setmealVOPage.getTotal(),setmealVOPage.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids)
    {
        // 查询套餐状态
        ids.stream()
                .forEach(id ->
                {
                    Setmeal setmeal = setmealMapper.selectById(id);
                    // 起售中不能删除
                    if (setmeal.getStatus() == StatusConstant.ENABLE)
                    {
                        throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
                    }
                });

        // 删除套餐表信息
        setmealMapper.deleteBatchByIds(ids);

        // 删除关联信息
        setmealDishMapper.deleteBatchBySetmealIds(ids);
    }
}
