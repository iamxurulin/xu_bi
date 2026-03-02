package com.rulin.xubibackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rulin.xubibackend.model.entity.Post;

import java.util.Date;
import java.util.List;


public interface PostMapper extends BaseMapper<Post> {

    /**
     * 查询帖子列表（包括已被删除的数据）
     * 该方法用于获取更新时间大于指定值的帖子列表，包含已被标记为删除的帖子
     *
     * @param minUpdateTime 最小更新时间，用于筛选更新时间大于此值的帖子
     * @return 返回符合条件（更新时间大于minUpdateTime）的帖子列表，包含已被删除的帖子
     */
    List<Post> listPostWithDelete(Date minUpdateTime);

}




