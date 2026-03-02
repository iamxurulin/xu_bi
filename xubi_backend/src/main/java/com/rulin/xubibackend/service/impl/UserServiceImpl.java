package com.rulin.xubibackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rulin.xubibackend.common.ErrorCode;
import com.rulin.xubibackend.constant.CommonConstant;
import com.rulin.xubibackend.exception.BusinessException;
import com.rulin.xubibackend.mapper.UserMapper;
import com.rulin.xubibackend.model.dto.user.UserQueryRequest;
import com.rulin.xubibackend.model.entity.User;
import com.rulin.xubibackend.model.enums.UserRoleEnum;
import com.rulin.xubibackend.model.vo.LoginUserVO;
import com.rulin.xubibackend.model.vo.UserVO;
import com.rulin.xubibackend.service.UserService;
import com.rulin.xubibackend.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.rulin.xubibackend.constant.UserConstant.USER_LOGIN_STATE;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "rulin";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验参数合法性
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        // 从会话中获取登录用户对象
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        // 将获取的对象强制转换为User类型
        User currentUser = (User) userObj;
        // 检查用户对象或用户ID是否为空，如果为空则抛出未登录异常
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        // 获取用户ID
        long userId = currentUser.getId();
        // 根据用户ID从数据库中查询用户信息
        currentUser = this.getById(userId);
        // 检查查询结果是否为空，如果为空则抛出未登录异常
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 返回当前登录用户信息
        return currentUser;
    }

    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 方法功能：检查当前请求用户是否为管理员
        // 仅管理员可查询
        // 从会话中获取当前登录用户信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        // 将获取到的用户对象强制转换为User类型
        User user = (User) userObj;
        // 调用isAdmin方法判断用户是否为管理员
        return isAdmin(user);
    }

    @Override    // 重写父类的isAdmin方法

    /**
     * 判断用户是否为管理员
     * @param user 用户对象
     * @return 如果用户存在且角色为管理员，则返回true；否则返回false
     */
    public boolean isAdmin(User user) {    // 判断用户是否为管理员的方法
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());    // 返回用户是否为管理员的判断结果：用户不为空且用户角色等于管理员角色
    }

    /**
     * 用户登出方法
     *
     * @param request HTTP请求对象，用于获取会话信息
     * @return 登出成功返回true
     * @throws BusinessException 当用户未登录时抛出业务异常
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 检查用户是否已登录
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            // 如果未登录，抛出业务异常，提示"未登录"
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态，清除会话中的用户登录状态信息
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        // 返回true表示登出成功
        return true;
    }

    @Override
    /**
     * 获取登录用户视图对象
     * @param user 用户实体对象
     * @return 登录用户视图对象，如果用户为null则返回null
     */
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {  // 检查用户对象是否为null
            return null;     // 如果为null，直接返回null
        }
        LoginUserVO loginUserVO = new LoginUserVO();  // 创建登录用户视图对象
        BeanUtils.copyProperties(user, loginUserVO);  // 将用户对象的属性复制到登录用户视图对象中
        return loginUserVO;  // 返回填充好数据的登录用户视图对象
    }

    /**
     * 将User对象转换为UserVO对象
     *
     * @param user 用户实体对象
     * @return 转换后的用户视图对象，如果输入为null则返回null
     */
    @Override
    public UserVO getUserVO(User user) {
        // 检查输入参数是否为null
        if (user == null) {
            return null;
        }
        // 创建新的UserVO对象
        UserVO userVO = new UserVO();
        // 使用Spring的BeanUtils将user对象的属性复制到userVO对象中
        BeanUtils.copyProperties(user, userVO);
        // 返回转换后的UserVO对象
        return userVO;
    }

    @Override
    /**
     * 将User对象列表转换为UserVO对象列表
     * @param userList 用户对象列表
     * @return 用户视图对象列表
     */
    public List<UserVO> getUserVO(List<User> userList) {
        // 如果输入的用户列表为空，则返回一个空列表
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        // 使用Stream流将User对象列表转换为UserVO对象列表
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    /**
     * 根据用户查询请求构建查询条件包装器
     * @param userQueryRequest 用户查询请求对象，包含查询条件
     * @return QueryWrapper<User> 构建好的查询条件包装器，用于数据库查询
     * @throws BusinessException 当请求参数为空时抛出业务异常
     */
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        // 检查请求参数是否为空，为空则抛出异常
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        // 从请求对象中获取各个查询条件
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        // 创建查询条件包装器
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 添加ID查询条件，id不为null时才添加条件
        queryWrapper.eq(id != null, "id", id);
        // 添加unionId查询条件，unionId不为空时才添加条件
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        // 添加mpOpenId查询条件，mpOpenId不为空时才添加条件
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        // 添加userRole查询条件，userRole不为空时才添加条件
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        // 添加userProfile模糊查询条件，userProfile不为空时才添加条件
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        // 添加userName模糊查询条件，userName不为空时才添加条件
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        // 添加排序条件，验证排序字段有效性后添加排序
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}
