package com.rulin.xubibackend.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.rulin.xubibackend.annotation.AuthCheck;
import com.rulin.xubibackend.common.BaseResponse;
import com.rulin.xubibackend.common.DeleteRequest;
import com.rulin.xubibackend.common.ErrorCode;
import com.rulin.xubibackend.common.ResultUtils;
import com.rulin.xubibackend.constant.CommonConstant;
import com.rulin.xubibackend.constant.UserConstant;
import com.rulin.xubibackend.exception.BusinessException;
import com.rulin.xubibackend.exception.ThrowUtils;
import com.rulin.xubibackend.manager.AiManager;
import com.rulin.xubibackend.manager.RedisLimiterManager;
import com.rulin.xubibackend.model.dto.chart.*;
import com.rulin.xubibackend.model.entity.Chart;
import com.rulin.xubibackend.model.entity.User;
import com.rulin.xubibackend.model.vo.BiResponse;
import com.rulin.xubibackend.service.ChartService;
import com.rulin.xubibackend.service.UserService;
import com.rulin.xubibackend.utils.ExcelUtils;
import com.rulin.xubibackend.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 队列控制器类，提供线程池任务的添加和状态查询功能
 * 仅在local环境下激活
 */
@RestController
@RequestMapping("/queue")
@Slf4j
@Profile({"local"})
public class QueueController {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor; // 线程池执行器

    /**
     * 添加任务到线程池
     * @param name 任务名称
     */
    @GetMapping("/add")//接收一个参数name，然后将任务添加到线程池中
    public void add(String name){
        //使用CompletableFuture运行一个异步任务
        CompletableFuture.runAsync(()->{
            //打印一条日志信息，包括任务名称和执行线程的名称
            log.info("任务执行中："+name+"，执行人："+Thread.currentThread().getName());

            try{
                Thread.sleep(600000); // 模拟任务执行，休眠600秒
            }catch (InterruptedException e){
                e.printStackTrace(); // 打印中断异常信息
            }
        },threadPoolExecutor);
    }

    /**
     * 获取线程池状态信息
     * @return 包含线程池各项状态信息的JSON字符串
     */
    @GetMapping("/get")
    //该方法返回线程池的状态信息
    public String get(){
        //创建一个HashMap存储线程池的状态信息
        Map<String,Object> map = new HashedMap<>();

        //获取线程池的队列长度
        int size = threadPoolExecutor.getQueue().size();

        //将队列长度放入map中
        map.put("队列长度",size);

        //获取线程池已接收的任务总数
        long taskCount = threadPoolExecutor.getTaskCount();

        //将任务总数放入map中
        map.put("任务总数",taskCount);

        //获取线程池已完成的任务数
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();

        //将已完成的任务数放入map中
        map.put("已完成任务数",completedTaskCount);

        //获取线程池中正在执行任务的线程数
        int activeCount = threadPoolExecutor.getActiveCount();

        //将正在工作的线程数放入map中
        map.put("正在工作的线程数",activeCount);

        //将map转换为JSON字符串并返回
        return JSONUtil.toJsonStr(map);
    }

}
