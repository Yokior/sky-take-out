package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService
{

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO)
    {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 菜品表插入数据
        dishMapper.insert(dish);

        Long dishId = dish.getId();

        // 口味表插入数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0)
        {
            flavors.stream()
                    .forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 分页查询菜品
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO)
    {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> dishVOPage = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(dishVOPage.getTotal(), dishVOPage.getResult());
    }

    /**
     * 单个/批量删除菜品
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids)
    {
        // 判断当前菜品是否可以删除
        ids.stream().forEach(id ->
        {
            Dish dish = dishMapper.selectById(id);
            // 是否起售中 起售中不能删除
            if (dish.getStatus() == StatusConstant.ENABLE)
            {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });

        // 是否被套餐关联 关联不能删除
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0)
        {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品表数据
        ids.stream().forEach(id ->
        {
            dishMapper.deleteById(id);
            // 删除对应的口味数据
            dishFlavorMapper.deleteByDishId(id);
        });
    }

    /**
     * 根据id获取菜品数据+口味数据
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id)
    {
        // 根据id查询菜品数据
        Dish dish = dishMapper.selectById(id);

        // 根据id查询口味数据
        List<DishFlavor> dishFlavorList = dishFlavorMapper.selectByDishId(id);

        // 封装vo
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavorList);

        return dishVO;
    }

    /**
     * 根据Id修改菜品和对应口味信息
     * @param dishDTO
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO)
    {
        Long dishId = dishDTO.getId();

        // 修改菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        // 删除口味表数据重新插入
        dishFlavorMapper.deleteByDishId(dishId);

        // 口味表插入数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0)
        {
            flavors.stream()
                    .forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> listDishByCategoryId(Long categoryId)
    {
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        // 起售中的菜品
        dish.setStatus(StatusConstant.ENABLE);

        List<Dish> dishList = dishMapper.selectByCategoryId(dish);

        return dishList;
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.selectByCategoryId(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
