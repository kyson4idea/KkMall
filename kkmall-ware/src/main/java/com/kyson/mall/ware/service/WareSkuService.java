package com.kyson.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kyson.common.to.mq.OrderTo;
import com.kyson.common.to.mq.StockLockedTo;
import com.kyson.common.utils.PageUtils;
import com.kyson.mall.ware.entity.WareSkuEntity;
import com.kyson.mall.ware.vo.SkuHasStockVo;
import com.kyson.mall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-08-01 14:39:59
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unLockStock(StockLockedTo to);

    void unLockStock(OrderTo to);
}

