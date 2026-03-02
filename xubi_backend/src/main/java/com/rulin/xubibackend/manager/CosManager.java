package com.rulin.xubibackend.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.rulin.xubibackend.config.CosClientConfig;

import java.io.File;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;


@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig; // 注入COS客户端配置对象

    @Resource
    private COSClient cosClient; // 注入COS客户端对象

    /**
     * 将本地文件上传到指定的存储桶中
     *
     * @param key           文件在存储桶中的键（对象名称）
     * @param localFilePath 本地文件的完整路径
     * @return PutObjectResult 包含文件上传结果的对象
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        // 创建上传请求对象，指定存储桶名称、对象键和本地文件路径
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                new File(localFilePath));
        // 执行上传操作并返回结果
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传文件到对象存储服务
     *
     * @param key  文件在存储桶中的唯一标识符（键名）
     * @param file 要上传的本地文件对象
     * @return PutObjectResult 包含文件上传结果的对象，如文件ETag等
     */
    public PutObjectResult putObject(String key, File file) {
        // 创建上传请求对象，指定存储桶名称、文件键名和本地文件对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 执行文件上传操作并返回上传结果
        return cosClient.putObject(putObjectRequest);
    }
}
