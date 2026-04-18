package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Account;
import com.example.entity.Carousel;
import com.example.exception.CustomException;
import com.example.mapper.CarouselMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务处理
 **/
@Service
public class CarouselService {

    @Resource
    private CarouselMapper carouselMapper;

    /**
     * 新增
     */
    public void add(Carousel carousel) {
        //校验标题不能为空
        if(ObjectUtil.isNull(carousel.getTitle())){
            throw new CustomException("轮播图标题不能为空");
        }
        //校验图片不能为空
        if(ObjectUtil.isNull(carousel.getImg())){
            throw new CustomException("轮播图图片不能为空");
        }
        carouselMapper.insert(carousel);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        //校验轮播图是否存在
        if (ObjectUtil.isEmpty(selectById(id))) {
            throw new CustomException("轮播图不存在，删除失败");
        }
        carouselMapper.deleteById(id);
    }

    /**
     * 修改
     */
    public void updateById(Carousel carousel) {
        // 校验ID不能为空
        if (ObjectUtil.isEmpty(carousel.getId())) {
            throw new CustomException("轮播图ID不能为空");
        }
        // 校验标题不能为空
        if (ObjectUtil.isEmpty(carousel.getTitle())) {
            throw new CustomException("轮播图标题不能为空");
        }
        carouselMapper.updateById(carousel);
    }

    /**
     * 根据ID查询
     */
    public Carousel selectById(Integer id) {
        return carouselMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Carousel> selectAll(Carousel carousel) {
        return carouselMapper.selectAll(carousel);
    }

    /**
     * 分页查询
     */
    public PageInfo<Carousel> selectPage(Carousel carousel, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Carousel> list = carouselMapper.selectAll(carousel);
        return PageInfo.of(list);
    }

    /**
     * 统计轮播图总数
     */
    public int countAll() {
        return carouselMapper.countAll();
    }
}