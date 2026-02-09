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
@Mapper
public interface UserMapper extends BaseMapper<User> {


}
