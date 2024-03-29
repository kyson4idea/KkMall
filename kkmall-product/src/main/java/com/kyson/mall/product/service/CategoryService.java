package com.kyson.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kyson.common.utils.PageUtils;
import com.kyson.mall.product.entity.CategoryEntity;
import com.kyson.mall.product.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-07-29 16:39:52
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    /**
     * 找到catelog 完整路径
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    /**
     * 级联更新所有关联的数据
     * @param category
     */
    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getLevel1Category();

    Map<String, List<Catalog2Vo>> getCataLogJson();
}

