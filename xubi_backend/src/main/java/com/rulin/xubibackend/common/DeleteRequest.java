package com.rulin.xubibackend.common;

import java.io.Serializable;
import lombok.Data;

import lombok.Data; // 导入Lombok的@Data注解，用于自动生成getter、setter等方法
import java.io.Serializable; // 导入Serializable接口，用于实现序列化

/**
 * DeleteRequest类，用于封装删除请求的数据
 * 实现了Serializable接口，使得该类的对象可以被序列化
 */
@Data // 使用Lombok的@Data注解，自动为类的所有字段生成getter、setter、toString、equals和hashCode方法
public class DeleteRequest implements Serializable { // 定义DeleteRequest类，实现Serializable接口
    private Long id; // 定义Long类型的id属性，用于标识要删除的对象的ID
    private static final long serialVersionUID = 1L; // 定义序列化版本UID，用于在序列化和反序列化过程中验证版本一致性
}
