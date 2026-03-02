package com.rulin.xubibackend.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rulin.xubibackend.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author rulin
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2026-02-09 13:18:41
* @Entity com.rulin.xubibackend.model.entity.User
*/
@Mapper  // MyBatis-Plus的注解，表示这是一个Mapper接口
public interface UserMapper extends BaseMapper<User> {  // 继承BaseMapper，获得基本的CRUD操作能力


    // UserMapper接口，用于定义与用户表相关的数据库操作方法
    // 由于继承了BaseMapper<User>，已经包含了常用的增删改查方法
    // 如果需要自定义查询方法，可以在此接口中添加定义
}
