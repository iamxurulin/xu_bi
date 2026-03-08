package com.rulin.xubibackend.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.rulin.xubibackend.annotation.AuthCheck;
import com.rulin.xubibackend.bizmq.BiMessageProducer;
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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 图表控制器类，处理图表相关的HTTP请求
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    // 注入所需的服务
    @Resource
    private ChartService chartService;    // 图表服务

    @Resource
    private UserService userService;      // 用户服务

    @Resource
    private AiManager aiManager;         // AI管理器

    @Resource
    private RedisLimiterManager redisLimiterManager;  // Redis限流管理器

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;      // 线程池执行器

    @Resource
    private BiMessageProducer biMessageProducer;       // BI消息生产者


    // region 增删改查

    /**
     * 添加图表接口
     *
     * @param chartAddRequest 图表添加请求参数
     * @param request         HTTP请求对象
     * @return 返回新创建的图表ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        // 参数校验：如果请求参数为空，则抛出参数错误异常
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建新的图表对象
        Chart chart = new Chart();
        // 将请求参数复制到图表对象中
        BeanUtils.copyProperties(chartAddRequest, chart);
        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 设置图表的用户ID为当前登录用户的ID
        chart.setUserId(loginUser.getId());
        // 保存图表到数据库
        boolean result = chartService.save(chart);
        // 如果保存失败，抛出操作错误异常
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 获取新创建的图表ID
        long newChartId = chart.getId();
        // 返回成功响应，包含新图表ID
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除图表的请求处理方法
     *
     * @param deleteRequest 包含要删除的图表ID的请求体
     * @param request       HTTP请求对象，用于获取用户信息
     * @return 返回操作结果，包含删除是否成功的布尔值
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 检查请求参数是否有效
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 从请求中获取登录用户信息
        User user = userService.getLoginUser(request);
        // 获取要删除的图表ID
        long id = deleteRequest.getId();
        // 从数据库中获取指定ID的图表信息
        Chart oldChart = chartService.getById(id);
        // 如果图表不存在，抛出"未找到"异常
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除 - 检查当前用户是否为图表所有者或管理员
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            // 如果用户既不是图表所有者也不是管理员，抛出"无权限"异常
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 执行删除操作，根据ID删除图表
        boolean b = chartService.removeById(id);
        // 返回操作结果，删除成功返回true，失败返回false
        return ResultUtils.success(b);
    }


    /**
     * 更新图表接口
     *
     * @param chartUpdateRequest 图表更新请求参数
     * @return 返回操作结果，成功为true，失败为false
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 需要管理员权限才能访问
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        // 校验请求参数是否合法
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建图表对象并复制请求参数
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取图表信息的接口
     *
     * @param id      图表ID，必须为正数
     * @param request HTTP请求对象，用于获取请求相关信息
     * @return BaseResponse<Chart> 包含图表信息的响应对象
     * @throws BusinessException 当参数错误或图表不存在时抛出
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        // 检查ID参数是否有效，如果ID小于等于0则抛出参数错误异常
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据ID从数据库中查询图表信息
        Chart chart = chartService.getById(id);
        // 检查查询结果是否存在，如果不存在则抛出未找到异常
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 返回成功响应，包含查询到的图表信息
        return ResultUtils.success(chart);
    }


    /**
     * 分页查询图表列表接口
     *
     * @param chartQueryRequest 图表查询请求参数
     * @param request           HTTP请求对象
     * @return 返回分页查询结果，包含图表列表数据
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {

        // 获取分页参数
        long current = chartQueryRequest.getCurrent();    // 当前页码
        long size = chartQueryRequest.getPageSize();       // 每页大小
        // 限制爬虫：设置每页最大数据量为20，防止恶意大量爬取数据
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 执行分页查询，使用QueryWrapper构建查询条件
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        // 返回查询成功结果
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页查询当前用户的图表列表
     *
     * @param chartQueryRequest 图表查询请求参数
     * @param request           HTTP请求对象
     * @return 返回分页查询结果，包含图表列表数据
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        // 参数校验：如果查询请求为空，则抛出参数错误异常
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 设置查询条件中的用户ID为当前登录用户ID
        chartQueryRequest.setUserId(loginUser.getId());
        // 获取分页参数
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫：如果每页大小超过20条，则抛出参数错误异常
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 执行分页查询，获取当前用户的图表列表
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        // 返回查询成功结果
        return ResultUtils.success(chartPage);
    }

    // endregion


    /**
     * 编辑图表接口
     *
     * @param chartEditRequest 图表编辑请求参数
     * @param request          HTTP请求对象，用于获取用户登录信息
     * @return 返回操作结果，包含是否编辑成功
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        // 校验请求参数是否有效
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建图表对象并复制请求参数
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }


    /**
     * 根据图表查询请求参数构建查询条件包装器
     *
     * @param chartQueryRequest 图表查询请求对象，包含查询条件
     * @return QueryWrapper<Chart> 构建好的查询条件包装器，用于数据库查询
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        // 创建查询条件包装器实例
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        // 如果查询请求为空，直接返回空包装器
        if (chartQueryRequest == null) {
            return queryWrapper;
        }

        // 从查询请求中获取各个查询条件参数
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();

        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        // 添加ID查询条件，id必须大于0才添加条件
        queryWrapper.eq(id != null && id > 0, "id", id);
        // 添加名称模糊查询条件
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        // 添加目标精确查询条件
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        // 添加图表类型精确查询条件
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        // 添加用户ID查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 添加未删除条件，只查询未删除的记录
        queryWrapper.eq("isDelete", false);
        // 添加排序条件，验证排序字段有效性后进行排序
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        // 返回构建好的查询条件包装器
        return queryWrapper;
    }

    /**
     * 生成图表的接口方法
     *
     * @param multipartFile       用户上传的Excel文件
     * @param genChartByAiRequest 生成图表的请求参数
     * @param request             HTTP请求对象，用于获取用户信息
     * @return 返回生成图表的结果封装对象
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        // 从请求参数中获取名称、目标和图表类型
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //校验分析目标是否为空
        //如果分析目标为空，就抛出请求参数错误异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");

        //如果名称不为空，并且名称长度大于100，就抛出异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");

        //校验文件，首先拿到用户请求的文件，取到原始文件大小
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();

        //校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过1M");

        String suffix = FileUtil.getSuffix(originalFilename);
        System.out.println("当前文件后缀：" + suffix);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        //通过response对象拿到用户id（必须登录才能使用）
        User loginUser = userService.getLoginUser(request);

        //限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        //指定一个模型id（把id写死，也可以定义成一个常量）
        long biModelId = CommonConstant.BI_MODEL_ID;

        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        //拼接分析目标
        String userGoal = goal;

        //如果图表类型不为空
        if (StringUtils.isNotBlank(chartType)) {
            //就将分析目标拼接上“请使用”+图表类型
            userGoal += ".请使用" + chartType;
        }

        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");

        //压缩后的数据（把multipartFile传进来）
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        //调用 AI
        String result = aiManager.sendMsgToXunFeiSpark(true, userInput.toString());

        //对返回结果做拆分，按照4个中括号进行拆分
        String[] splits = result.split("【【【【");
        //拆分之后还要进行校验
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        }

        String genChart = splits[1].trim();
        String genResult = splits[2].trim();


        //先把图表保存到数据库中
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());

        // ✅✅✅ 【新增这一行】同步调用成功后，必须手动设为成功状态
        chart.setStatus("succeed");

        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        BiResponse biResponse = new BiResponse();

        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());

        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析（异步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //校验
        //如果分析目标为空，就抛出请求参数错误异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");

        //如果名称不为空，并且名称长度大于100，就抛出异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");

        //校验文件，首先拿到用户请求的文件，取到原始文件大小
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();

        //校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过1M");

        String suffix = FileUtil.getSuffix(originalFilename);
        System.out.println("当前文件后缀：" + suffix);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        //通过response对象拿到用户id（必须登录才能使用）
        User loginUser = userService.getLoginUser(request);

        //限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        //指定一个模型id（把id写死，也可以定义成一个常量）
        long biModelId = CommonConstant.BI_MODEL_ID;

        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        //拼接分析目标
        String userGoal = goal;

        //如果图表类型不为空
        if (StringUtils.isNotBlank(chartType)) {
            //就将分析目标拼接上“请使用”+图表类型
            userGoal += ".请使用" + chartType;
        }

        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");

        //压缩后的数据（把multipartFile传进来）
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        //先把图表保存到数据库中
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);

        //设置任务状态为排队中
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        CompletableFuture.runAsync(() -> {
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            //把任务状态改为执行中
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);

            if (!b) {
                handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
                return;
            }

            //调用 AI
            String result = aiManager.sendMsgToXunFeiSpark(true, userInput.toString());

            //对返回结果做拆分，按照5个中括号进行拆分
            String[] splits = result.split("【【【【");
            //拆分之后还要进行校验
            if (splits.length < 3) {
                handleChartUpdateError(chart.getId(), "AI 生成错误");
                return;
            }

            // 从splits数组中获取并清理生成图表的字符串
            String genChart = splits[1].trim();

            // 从splits数组中获取并清理生成结果的字符串
            String genResult = splits[2].trim();

            // 创建一个新的Chart对象用于更新
            Chart updateChartResult = new Chart();
            // 设置更新后的图表ID为原图表ID
            updateChartResult.setId(chart.getId());
            // 设置生成图表的内容
            updateChartResult.setGenChart(genChart);
            // 设置生成结果的内容
            updateChartResult.setGenResult(genResult);
            // 将图表状态设置为"succeed"表示成功
            updateChartResult.setStatus("succeed");

            // 调用chartService更新图表信息
            boolean updateResult = chartService.updateById(updateChartResult);
            // 如果更新失败，处理错误情况
            if (!updateResult) {
                handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
            }
        }, threadPoolExecutor);  // 使用线程池执行上述操作


        // 创建BiResponse对象用于返回响应
        BiResponse biResponse = new BiResponse();

        // 设置响应中的图表ID
        biResponse.setChartId(chart.getId());

        // 返回成功的响应结果
        return ResultUtils.success(biResponse);
    }


    /**
     * 智能分析（异步消息队列）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                        GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //校验
        //如果分析目标为空，就抛出请求参数错误异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");

        //如果名称不为空，并且名称长度大于100，就抛出异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");

        //校验文件，首先拿到用户请求的文件，取到原始文件大小
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();

        //校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过1M");

        String suffix = FileUtil.getSuffix(originalFilename);
        System.out.println("当前文件后缀：" + suffix);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        //通过response对象拿到用户id（必须登录才能使用）
        User loginUser = userService.getLoginUser(request);

        //限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        //指定一个模型id（把id写死，也可以定义成一个常量）
        long biModelId = CommonConstant.BI_MODEL_ID;

        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        //拼接分析目标
        String userGoal = goal;

        //如果图表类型不为空
        if (StringUtils.isNotBlank(chartType)) {
            //就将分析目标拼接上“请使用”+图表类型
            userGoal += ".请使用" + chartType;
        }

        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");

        //压缩后的数据（把multipartFile传进来）
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        //先把图表保存到数据库中
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);

        //设置任务状态为排队中
// 设置图表状态为"等待中"
        chart.setStatus("wait");
// 设置当前登录用户的ID为图表的用户ID
        chart.setUserId(loginUser.getId());
// 保存图表信息到数据库，并获取保存结果
        boolean saveResult = chartService.save(chart);
// 如果保存失败，则抛出系统错误异常
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

// 获取新保存的图表ID
        long newChartId = chart.getId();
// 发送消息，将新图表ID作为消息内容
        biMessageProducer.sendMessage(String.valueOf(newChartId));

// 创建BI响应对象
        BiResponse biResponse = new BiResponse();

// 设置响应对象中的图表ID
        biResponse.setChartId(chart.getId());

// 返回成功响应，包含图表ID信息
        return ResultUtils.success(biResponse);
    }


    /**
     * 处理图表更新错误的方法
     *
     * @param chartId     图表ID
     * @param execMessage 执行消息/错误信息
     */
    private void handleChartUpdateError(long chartId, String execMessage) {
        // 创建一个新的图表对象用于更新状态
        Chart updateChartResult = new Chart();
        // 设置图表ID
        updateChartResult.setId(chartId);
        // 设置图表状态为"失败"
        updateChartResult.setStatus("failed");
        // 设置执行消息/错误信息
        updateChartResult.setExecMessage(execMessage);
        // 调用服务层方法更新图表信息
        boolean updateResult = chartService.updateById(updateChartResult);
        // 如果更新失败，记录错误日志
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }
}
