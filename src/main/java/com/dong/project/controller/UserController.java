package com.dong.project.controller;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.dong.dongapicommon.model.entity.User;
import com.dong.project.annotation.AuthCheck;
import com.dong.project.common.*;
import com.dong.project.config.EmailConfig;
import com.dong.project.exception.BusinessException;
import com.dong.project.model.dto.user.*;
import com.dong.project.model.enums.UserAccountStatusEnum;
import com.dong.project.model.vo.UserVO;
import com.dong.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dong.project.constant.EmailConstant.*;
import static com.dong.project.constant.UserConstant.ADMIN_ROLE;
import static com.dong.project.utils.EmailUtil.buildEmailContent;

/**
 * 用户接口
 *
 * @author dong
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private EmailConfig emailConfig;

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"注册数据为空");
        }
        String userName = userRegisterRequest.getUserName();
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String invitationCode = userRegisterRequest.getInvitationCode();

        if (StringUtils.isAnyBlank(userName,userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userName,userAccount, userPassword, checkPassword,invitationCode);
        return ResultUtils.success(result);
    }

    /**
     * 邮箱注册请求
     * @param userEmailRegister
     * @return
     */
    @PostMapping("/email/register")
    public BaseResponse<Long> userEmailRegister(@RequestBody UserEmailRegister userEmailRegister){
        if (userEmailRegister == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱注册请求参数为空");
        }
        long result = userService.userEmailRegister(userEmailRegister);
        //删除redis缓存，保险做法。名称为：api:captcha:+邮箱号码
        redisTemplate.delete(CAPTCHA_CACHE_KEY + userEmailRegister.getEmailAccount());
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 邮箱登录
     * @param userEmailLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/email/login")
    public BaseResponse<UserVO> userEmailLogin(@RequestBody UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request){
        if (userEmailLoginRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO userVO = userService.userEmailLogin(userEmailLoginRequest, request);
        redisTemplate.delete(CAPTCHA_CACHE_KEY+userEmailLoginRequest.getEmailAccount());
        return ResultUtils.success(userVO);
    }

    /**
     * 获取验证码
     * @param emailAccount
     * @return
     */
    @GetMapping("/getCaptcha")
    public BaseResponse<Boolean> getCaptcha(String emailAccount){
        if (StringUtils.isBlank(emailAccount)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱账号为空");
        }
        //验证邮箱正确性
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
        }
        //生成验证码
        String captcha = RandomUtil.randomNumbers(6);
        try {
            senEmail(emailAccount,captcha);
            //写入缓存五分钟,五分钟后失效
            redisTemplate.opsForValue().set(CAPTCHA_CACHE_KEY+emailAccount,captcha,5, TimeUnit.MINUTES);
            return ResultUtils.success(true);
        } catch (MessagingException e) {
            log.error("[发送验证码失败]"+e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"验证码获取失败");
        }


    }

    /**
     * 发送邮件
     * @param emailAccount
     * @param captcha
     * @throws MessagingException
     */
    private void senEmail(String emailAccount,String captcha) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        //邮箱发送内容组成
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        //邮件标题
        mimeMessageHelper.setSubject(EMAIL_SUBJECT);
        //邮件内容,true表示为html组成的
        mimeMessageHelper.setText(buildEmailContent(EMAIL_HTML_CONTENT_PATH,captcha),true);
        mimeMessageHelper.setTo(emailAccount);
        mimeMessageHelper.setFrom(EMAIL_TITLE + '<' + emailConfig.getEmailFrom() + '>');
        mailSender.send(mimeMessage);
    }

    /**
     * 绑定邮箱
     * @param userBindEmailRequest
     * @param request
     * @return
     */
    @PostMapping("/bind/login")
    public BaseResponse<UserVO> userBindEmail(@RequestBody UserBindEmailRequest userBindEmailRequest,HttpServletRequest request){
        if (userBindEmailRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"绑定邮箱数据为空");
        }
        UserVO userVO = userService.userBindEmail(userBindEmailRequest, request);
        return ResultUtils.success(userVO);
    }

    /**
     * 解绑邮箱
     * @param userUnBindEmailRequest
     * @param request
     * @return
     */
    @PostMapping("/unbindEmail")
    public BaseResponse<UserVO> userUnBindEmail(@RequestBody UserUnBindEmailRequest userUnBindEmailRequest,HttpServletRequest request){
        if (userUnBindEmailRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO userVO = userService.userUnBindEmail(userUnBindEmailRequest, request);
        return ResultUtils.success(userVO);
    }

    /**
     * 封号
     * @param idRequest
     * @param request
     * @return
     */
    @PostMapping("/ban")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> banUser(@RequestBody IdRequest idRequest, HttpServletRequest request){
        if (ObjectUtils.anyNull(idRequest,idRequest.getId()) || idRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();
        User user = userService.getById(id);
        if (user == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        user.setStatus(UserAccountStatusEnum.BAN.getValue());
        return ResultUtils.success(userService.updateById(user));
    }

    /**
     * 解封
     * @param idRequest
     * @return
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @PostMapping("/normal")
    public BaseResponse<Boolean> normalUser(@RequestBody IdRequest idRequest) {
        if (ObjectUtils.anyNull(idRequest, idRequest.getId()) || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        user.setStatus(UserAccountStatusEnum.NORMAL.getValue());
        return ResultUtils.success(userService.updateById(user));
    }

    /**
     * 用户注销,退出登录
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        UserVO user = userService.getLoginUser(request);
        return ResultUtils.success(user);
    }



    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody IdRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<UserVO> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
       if (ObjectUtils.anyNull(userUpdateRequest, userUpdateRequest.getId()) || userUpdateRequest.getId() <= 0){
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
       }

       //管理员才可以操作
        boolean adminOperation = ObjectUtils.isNotEmpty(userUpdateRequest.getBalance())
                || StringUtils.isNoneBlank(userUpdateRequest.getUserRole());
       //检验是否登录
        UserVO loginUser = userService.getLoginUser(request);
        //不是管理员不让动用户权限和用户余额
        if (adminOperation && !loginUser.getUserRole().equals(ADMIN_ROLE)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"只有管理员可以修改");
        }

        //只有管理员和自己可以修改
        if (!loginUser.getUserRole().equals(ADMIN_ROLE) && !userUpdateRequest.getId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"只有本人和管理员可以修改");
        }

        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest,user);
        //检验用户信息
        userService.legalUser(user,false);

        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(User::getId,user.getId());
        boolean result = userService.updateById(user);
        if (!result){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"更新失败");
        }
        UserVO userVO = new UserVO();
        //user可能数据不全，更新完再去数据库查到最新的返回
        BeanUtils.copyProperties(userService.getById(user.getId()),userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 更新ak、sk
     * @param request
     * @return
     */
    @PostMapping("/update/voucher")
    public BaseResponse<UserVO> updateVoucher(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(loginUser, user);
        UserVO userVO = userService.updateVoucher(user);
        return ResultUtils.success(userVO);
    }


    /**
     * 根据 id 获取用户
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<UserVO> getUserById(int id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 获取用户列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<UserVO>> listUser(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        List<User> userList = userService.list(queryWrapper);
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(userVOList);
    }

    /**
     * 分页获取用户列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<UserVO>> listUserByPage(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        long current = 1;
        long size = 10;
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
            current = userQueryRequest.getCurrent();
            size = userQueryRequest.getPageSize();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        Page<User> userPage = userService.page(new Page<>(current, size), queryWrapper);
        Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userPage.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 通过邀请码获取用户
     * @param invitationCode
     * @return
     */
    @PostMapping("/get/invitationCode")
    public BaseResponse<UserVO> getUserByInvitationCode(String invitationCode){
        if (StringUtils.isBlank(invitationCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邀请码为空");
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getInvitationCode,invitationCode);
        User invitationCodeUser = userService.getOne(userLambdaQueryWrapper);
        if (invitationCodeUser == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"邀请码不存在");
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(invitationCodeUser,userVO);
        return ResultUtils.success(userVO);
    }

    // endregion
}
