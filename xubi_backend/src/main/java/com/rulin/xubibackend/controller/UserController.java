package com.rulin.xubibackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rulin.xubibackend.annotation.AuthCheck;
import com.rulin.xubibackend.common.BaseResponse;
import com.rulin.xubibackend.common.DeleteRequest;
import com.rulin.xubibackend.common.ErrorCode;
import com.rulin.xubibackend.common.ResultUtils;
import com.rulin.xubibackend.config.WxOpenConfig;
import com.rulin.xubibackend.constant.UserConstant;
import com.rulin.xubibackend.exception.BusinessException;
import com.rulin.xubibackend.exception.ThrowUtils;
import com.rulin.xubibackend.model.dto.user.UserAddRequest;
import com.rulin.xubibackend.model.dto.user.UserLoginRequest;
import com.rulin.xubibackend.model.dto.user.UserQueryRequest;
import com.rulin.xubibackend.model.dto.user.UserRegisterRequest;
import com.rulin.xubibackend.model.dto.user.UserUpdateMyRequest;
import com.rulin.xubibackend.model.dto.user.UserUpdateRequest;
import com.rulin.xubibackend.model.entity.User;
import com.rulin.xubibackend.model.vo.LoginUserVO;
import com.rulin.xubibackend.model.vo.UserVO;
import com.rulin.xubibackend.service.UserService;

import static com.rulin.xubibackend.service.impl.UserServiceImpl.SALT;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求，包括注册、登录、注销、用户信息管理等
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    /**
     * 用户服务接口
     * 用于处理用户相关的业务逻辑
     */
    @Resource
    private UserService userService;

    /**
     * 微信开放平台配置
     * 用于处理微信相关的配置信息
     */
    @Resource
    private WxOpenConfig wxOpenConfig;

    // region 登录相关


    /**
     * 用户注册接口
     *
     * @param userRegisterRequest 用户注册请求参数，包含用户账号、密码和确认密码
     * @return BaseResponse<Long> 返回注册成功的用户ID
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 检查请求参数是否为空
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取用户注册信息
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 检查账号、密码或确认密码是否为空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        // 调用用户服务进行注册操作
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        // 返回注册成功的用户ID
        return ResultUtils.success(result);
    }


    /**
     * 处理用户登录请求的接口方法
     *
     * @param userLoginRequest 包含用户登录信息的请求体，包含用户账号和密码
     * @param request          HTTP请求对象，用于获取请求相关信息
     * @return 返回登录用户的视图对象(LoginUserVO)封装在BaseResponse中
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 检查请求体是否为空
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取用户账号和密码
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        // 检查账号或密码是否为空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 调用userService处理用户登录逻辑
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        // 返回登录成功的响应结果
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户登出接口
     *
     * @param request HTTP请求对象，包含用户会话信息
     * @return 返回操作结果，包含登出是否成功的信息
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        // 检查请求对象是否为空
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 调用用户服务执行登出操作
        boolean result = userService.userLogout(request);
        // 返回操作成功结果
        return ResultUtils.success(result);
    }


    /**
     * 获取当前登录用户信息的接口
     *
     * @param request HTTP请求对象，用于获取当前会话信息
     * @return BaseResponse<LoginUserVO> 包含登录用户信息的响应对象
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        // 通过userService获取当前登录用户实体对象
        User user = userService.getLoginUser(request);
        // 将用户实体对象转换为视图对象并返回成功响应
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    // endregion

    // region 增删改查

    /**
     * 添加用户接口
     *
     * @param userAddRequest 添加用户的请求参数
     * @param request        HTTP请求对象
     * @return 返回新添加用户的ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 需要管理员权限才能访问
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        // 检查请求参数是否为空
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建新用户对象
        User user = new User();
        // 将请求参数复制到用户对象中
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        String defaultPassword = "12345678";
// 使用MD5算法对密码进行加密处理
// 将盐值(SALT)与默认密码(defaultPassword)拼接后进行MD5加密，并将结果转换为十六进制字符串
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
// 将加密后的密码设置到用户对象中
        user.setUserPassword(encryptPassword);
// 调用userService的save方法保存用户信息，并将返回结果存储在result变量中
        boolean result = userService.save(user);
// 检查保存操作是否成功，如果失败则抛出操作异常
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
// 返回成功结果，包含用户ID
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户接口
     * 需要管理员权限才能访问
     *
     * @param deleteRequest 删除请求对象，包含要删除的用户ID
     * @param request       HTTP请求对象
     * @return 返回删除操作的结果，成功返回true，失败返回false
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 权限检查，必须是管理员角色
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 参数校验：检查请求对象是否为空或ID是否有效
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 调用服务层方法删除用户
        boolean b = userService.removeById(deleteRequest.getId());
        // 返回操作结果
        return ResultUtils.success(b);
    }

    /**
     * 更新用户信息的接口
     * 需要管理员权限才能访问
     *
     * @param userUpdateRequest 包含用户更新信息的请求体
     * @param request           HTTP请求对象
     * @return 返回操作结果，成功返回true，失败抛出异常
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 验证用户权限，必须是管理员角色
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        // 检查请求参数是否合法，如果请求体或ID为空则抛出参数错误异常
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建用户对象并复制请求体中的属性
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        // 调用服务层更新用户信息，如果更新失败则抛出操作异常
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回成功结果
        return ResultUtils.success(true);
    }


    /**
     * 根据用户ID获取用户信息
     * 需要管理员权限才能访问
     *
     * @param id      用户ID
     * @param request HTTP请求对象
     * @return 返回用户信息的BaseResponse封装结果
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 权限检查，要求必须是管理员角色
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        // 参数校验：ID必须大于0
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据ID查询用户
        User user = userService.getById(id);
        // 如果用户不存在，抛出异常
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        // 返回成功响应，包含用户信息
        return ResultUtils.success(user);
    }


    /**
     * 根据用户ID获取用户视图对象(UserVO)
     *
     * @param id      用户ID
     * @param request HTTP请求对象
     * @return 返回包含用户视图对象的基础响应
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        // 调用getUserById方法获取用户基础信息
        BaseResponse<User> response = getUserById(id, request);
        // 从响应中获取用户数据
        User user = response.getData();
        // 调用userService的getUserVO方法将用户对象转换为视图对象并返回成功响应
        return ResultUtils.success(userService.getUserVO(user));
    }


    /**
     * 分页查询用户列表接口
     * 需要管理员权限才能访问
     *
     * @param userQueryRequest 分页查询请求参数，包含当前页码和每页大小
     * @param request          HTTP请求对象，用于获取请求相关信息
     * @return 返回分页查询结果，包含用户列表和分页信息
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 需要管理员权限才能访问此接口
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
        long current = userQueryRequest.getCurrent(); // 获取当前页码
        long size = userQueryRequest.getPageSize(); // 获取每页大小
        // 调用服务层方法进行分页查询，使用查询条件构造器构建查询条件
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage); // 返回查询成功结果
    }

    /**
     * 分页获取用户封装列表
     * 该接口用于分页查询用户数据，并返回封装后的视图对象(UserVO)
     *
     * @param userQueryRequest 分页查询条件请求对象，包含当前页码、页大小等分页参数
     * @param request          HTTP请求对象，用于获取请求相关信息
     * @return 返回分页结果，包含用户视图对象列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                       HttpServletRequest request) {
        // 检查查询请求对象是否为空
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前页码和每页大小
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫，防止一次性获取过多数据
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 执行分页查询，获取用户数据
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        // 创建用户视图对象分页实例
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        // 将用户记录转换为视图对象
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        // 设置视图对象列表到分页结果中
        userVOPage.setRecords(userVO);
        // 返回成功响应结果
        return ResultUtils.success(userVOPage);
    }

    // endregion


    /**
     * 更新当前登录用户信息的接口
     *
     * @param userUpdateMyRequest 包含用户更新信息的请求体
     * @param request             HTTP请求对象，用于获取当前登录用户信息
     * @return 返回操作结果，成功返回true，失败抛出异常
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        // 检查请求参数是否为空
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 创建新的User对象并复制请求中的属性
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        // 设置用户ID为当前登录用户的ID
        user.setId(loginUser.getId());
        // 更新用户信息
        boolean result = userService.updateById(user);
        // 如果更新失败，抛出异常
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回成功结果
        return ResultUtils.success(true);
    }
}
