package com.kyson.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.kyson.common.constant.ProductConstant;
import com.kyson.mall.product.dao.AttrAttrgroupRelationDao;
import com.kyson.mall.product.dao.AttrGroupDao;
import com.kyson.mall.product.dao.CategoryDao;
import com.kyson.mall.product.entity.AttrAttrgroupRelationEntity;
import com.kyson.mall.product.entity.AttrGroupEntity;
import com.kyson.mall.product.entity.CategoryEntity;
import com.kyson.mall.product.service.CategoryService;
import com.kyson.mall.product.vo.AttrGroupRelationVo;
import com.kyson.mall.product.vo.AttrRespVo;
import com.kyson.mall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;

import com.kyson.mall.product.dao.AttrDao;
import com.kyson.mall.product.entity.AttrEntity;
import com.kyson.mall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params)
    {
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), new QueryWrapper<AttrEntity>());

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAttr(AttrVo attr)
    {
        AttrEntity attrEntity = new AttrEntity();
        //???vo?????? ????????? po???
        BeanUtils.copyProperties(attr, attrEntity);

        //1?????????????????????
        this.save(attrEntity);

        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null)
        {
            //2?????????????????????
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationDao.insert(relationEntity);
        }

    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType)
    {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type", "base".equalsIgnoreCase(attrType) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        if (catelogId == 0)
        {
            queryWrapper.eq("catelog_id", catelogId);
        }

        String key = (String) params.get("key");

        if (!StringUtils.isEmpty(key))
        {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();

        List<AttrRespVo> respVos = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);

            //????????????????????????

            if ("base".equalsIgnoreCase(attrType))
            {
                AttrAttrgroupRelationEntity attrId = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (attrId != null && attrId.getAttrGroupId() != null)
                {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrId.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null)
            {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());

        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId)
    {
        AttrRespVo respVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);

        BeanUtils.copyProperties(attrEntity, respVo);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())
        {
            //1?????????????????????
            AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (relationEntity != null)
            {
                respVo.setAttrGroupId(relationEntity.getAttrGroupId());

                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());

                if (attrGroupEntity != null)
                {
                    respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }


        //2?????????????????????
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        respVo.setCatelogPath(catelogPath);

        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (categoryEntity != null)
        {

            respVo.setCatelogName(categoryEntity.getName());
        }
        return respVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attrVo)
    {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.updateById(attrEntity);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())
        {
            Long count = relationDao.selectCount(new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            //?????????????????? pms_category_brand_relation
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();

            relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            relationEntity.setAttrId(attrVo.getAttrId());

            if (count > 0)
            {
                relationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            } else
            {
                //??????
                relationDao.insert(relationEntity);
            }
        }

    }

    /**
     * ?????????id?????????????????????????????????
     *
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId)
    {
        List<AttrAttrgroupRelationEntity> entities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));

        List<Long> attrIds = entities.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        if (attrIds == null || attrIds.size() < 1)
        {
            return null;
        }

        List<AttrEntity> attrEntities = this.listByIds(attrIds);
        return attrEntities;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos)
    {
        //relationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",1).eq("attr_group_id",1));

        //delete from `` where (attr_id = ? and attr_group_id = ?) or (attr_id = ? and attr_group_id = ?)

        List<AttrAttrgroupRelationEntity> relationEntities = Arrays.asList(vos).stream().map((item) -> {

            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());

        relationDao.deleteBatchRelation(relationEntities);
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId)
    {
        //1?????????????????????????????????????????????????????????????????????
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        //2??????????????????????????????????????????????????????????????????????????????????????????????????????
        List<AttrGroupEntity> groupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        List<Long> collect = groupEntities.stream().map((item) -> {
            return item.getCatelogId();
        }).collect(Collectors.toList());

        //?????????????????????????????????
        List<AttrAttrgroupRelationEntity> groupId = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));

        List<Long> attrIds = groupId.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());

        //???????????????????????????????????????????????????
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds != null && attrIds.size() > 0)
        {
            wrapper.notIn("attr_id", attrIds);
        }

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key))
        {
            wrapper.and(w -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }

}