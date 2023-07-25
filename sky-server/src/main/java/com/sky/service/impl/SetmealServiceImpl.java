package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService
{

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private CategoryMapper categoryMapper;

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

    /**
     * 修改套餐信息和关联菜品信息
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDTO setmealDTO)
    {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        // 修改套餐信息
        setmealMapper.update(setmeal);

        // 修改关联信息 先删除再添加
        Long setmealId = setmealDTO.getId();
        ArrayList<Long> setmealIds = new ArrayList<>();
        setmealIds.add(setmealId);
        setmealDishMapper.deleteBatchBySetmealIds(setmealIds);
        // 添加setmealId
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        setmealDishList.stream()
                .forEach(setmealDish ->
                {
                    setmealDish.setSetmealId(setmealId);
                });

        setmealDishMapper.insertBatch(setmealDishList);
    }

    /**
     * 根据id查询套餐信息
     * @param id
     * @return
     */
    @Override
    public SetmealVO getSetmealById(Long id)
    {
        // 查询套餐基本信息
        Setmeal setmeal = setmealMapper.selectById(id);
        // 查询categoryName
        Category category = categoryMapper.selectById(setmeal.getCategoryId());
        // 查询套餐菜品关联表信息
        List<SetmealDish> setmealDishList = setmealDishMapper.selectDishBySetmealId(id);

        // 封装vo
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setCategoryName(category.getName());
        setmealVO.setSetmealDishes(setmealDishList);

        return setmealVO;
    }

    /**
     * 起售/停售套餐
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id)
    {
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);

        setmealMapper.update(setmeal);
    }
}
