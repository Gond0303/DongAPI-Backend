package com.dong.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.dong.dongapicommon.model.entity.User;
import com.dong.project.model.dto.user.UserBindEmailRequest;
import com.dong.project.model.dto.user.UserEmailLoginRequest;
import com.dong.project.model.dto.user.UserEmailRegister;
import com.dong.project.model.dto.user.UserUnBindEmailRequest;
import com.dong.project.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
 * @author dong
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userName 用户昵称
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param invitationCode 邀请码
     * @return 新用户 id
     */
    long userRegister(String userName,String userAccount, String userPassword, String checkPassword, String invitationCode);

    /**
     * 邮箱注册
     * @param userEmailRegister
     */
    long userEmailRegister(UserEmailRegister userEmailRegister);


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 邮箱登录
     * @param userEmailLoginRequest 邮箱包装类（邮箱、验证码）
     * @param request
     * @return
     */
    UserVO userEmailLogin(UserEmailLoginRequest userEmailLoginRequest,HttpServletRequest request);


    /**
     * 绑定邮箱
     * @param userBindEmailRequest
     * @param request
     * @return
     */
    UserVO userBindEmail(UserBindEmailRequest userBindEmailRequest,HttpServletRequest request);

    /**
     * 解绑邮箱
     * @param userUnBindEmailRequest
     * @param request
     * @return
     */
    UserVO userUnBindEmail(UserUnBindEmailRequest userUnBindEmailRequest,HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    UserVO getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 检验用户
     * @param user
     * @param add
     */
    void legalUser(User user,boolean add);



    /**
     * 添加钱包余额
     *
     * @param userId    用户id
     * @param addPoints 添加点
     * @return boolean
     */
    boolean addWalletBalance(Long userId, Integer addPoints);


    /**
     * 更新ak\sk
     * @param user
     * @return
     */
    UserVO updateVoucher(User user);

    /**
     * 判断是否是游客
     * 是游客会直接返回null
     * @param request 要求
     * @return {@link User}
     */
    User isTourist(HttpServletRequest request);

    /**
     * 减少钱包余额
     *
     * @param userId      用户id
     * @param reduceScore 减少分数
     * @return boolean
     */
    boolean reduceWalletBalance(Long userId, Long reduceScore);
}
