package com.rulin.xubibackend.controller;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.util.Arrays;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.rulin.xubibackend.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rulin.xubibackend.common.BaseResponse;
import com.rulin.xubibackend.common.ErrorCode;
import com.rulin.xubibackend.common.ResultUtils;
import com.rulin.xubibackend.constant.FileConstant;
import com.rulin.xubibackend.exception.BusinessException;
import com.rulin.xubibackend.manager.CosManager;
import com.rulin.xubibackend.model.dto.file.UploadFileRequest;
import com.rulin.xubibackend.model.enums.FileUploadBizEnum;
import com.rulin.xubibackend.service.UserService;


/**
 * 文件控制器类
 * 处理文件上传等相关操作
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService; // 用户服务，用于获取登录用户信息

    @Resource
    private CosManager cosManager; // COS对象存储管理器，用于文件上传

    /**
     * 文件上传接口
     * @param multipartFile 上传的文件
     * @param uploadFileRequest 上传文件请求参数，包含业务类型等信息
     * @param request HTTP请求对象，用于获取用户登录信息
     * @return 返回文件在COS中的访问地址
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        // 获取业务类型并进行校验
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验文件合法性
        validFile(multipartFile, fileUploadBizEnum);
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 生成8位随机字母数字组合作为UUID
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        // 拼接生成新的文件名，格式为"UUID-原始文件名"
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        // 构建文件存储路径，格式为"/业务类型/用户ID/文件名"
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(FileConstant.COS_HOST + filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

/**
 * 验证上传文件的方法
 * @param multipartFile 上传的文件对象
 * @param fileUploadBizEnum 文件上传业务类型枚举
 */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 定义常量：1M的大小（以字节为单位）
        final long ONE_M = 1024 * 1024L;
        // 判断文件上传业务类型是否为用户头像
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            // 检查文件大小是否超过1M
            if (fileSize > ONE_M) {
                // 如果文件大小超过限制，抛出业务异常，提示文件大小不能超过1M
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            // 检查文件后缀是否在允许的类型列表中
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                // 如果文件类型不在允许列表中，抛出业务异常，提示文件类型错误
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
