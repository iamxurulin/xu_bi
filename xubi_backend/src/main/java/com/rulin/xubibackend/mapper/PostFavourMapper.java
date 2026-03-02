package com.rulin.xubibackend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rulin.xubibackend.model.entity.Post;
import com.rulin.xubibackend.model.entity.PostFavour;

import org.apache.ibatis.annotations.Param;


/**
 * PostFavourMapper接口
 * 继承BaseMapper<Post>，提供对PostFavour实体的基本CRUD操作
 */
public interface PostFavourMapper extends BaseMapper<PostFavour> {

    /**
     * 分页查询用户收藏的帖子列表
     * @param page 分页参数，包含分页信息
     * @param queryWrapper 查询条件包装器，用于构建查询条件
     * @param favourUserId 收藏用户ID，用于筛选特定用户收藏的帖子
     * @return 返回分页结果，包含帖子列表和分页信息
     */
    Page<Post> listFavourPostByPage(IPage<Post> page, @Param(Constants.WRAPPER) Wrapper<Post> queryWrapper,
            long favourUserId);

}




