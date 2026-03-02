package com.rulin.xubibackend.model.dto.file;

import java.io.Serializable;
import lombok.Data;


/**
 * 上传文件请求类
 * 实现了Serializable接口，支持序列化操作
 * 使用@Data注解自动生成getter、setter、toString等方法
 */
@Data
public class UploadFileRequest implements Serializable {

    /**
     * 业务
     * 该字段用于标识文件上传的业务场景，可以根据不同业务场景进行不同的处理
     */
    private String biz;

    /**
     * 序列化版本UID
     * 用于序列化和反序列化过程中验证版本一致性，确保类的兼容性
     */
    private static final long serialVersionUID = 1L;
}