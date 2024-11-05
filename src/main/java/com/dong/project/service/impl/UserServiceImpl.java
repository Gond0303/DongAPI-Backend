package com.dong.project.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.dongapicommon.model.entity.User;
import com.dong.project.annotation.AuthCheck;
import com.dong.project.common.BaseResponse;
import com.dong.project.common.ErrorCode;
import com.dong.project.common.IdRequest;
import com.dong.project.exception.BusinessException;
import com.dong.project.mapper.UserMapper;
import com.dong.project.model.dto.user.UserBindEmailRequest;
import com.dong.project.model.dto.user.UserEmailLoginRequest;
import com.dong.project.model.dto.user.UserEmailRegister;
import com.dong.project.model.dto.user.UserUnBindEmailRequest;
import com.dong.project.model.enums.UserAccountStatusEnum;
import com.dong.project.model.vo.UserVO;
import com.dong.project.service.UserService;
import com.dong.project.utils.RedissonLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Random;
import java.util.regex.Pattern;

import static com.dong.project.constant.EmailConstant.CAPTCHA_CACHE_KEY;
import static com.dong.project.constant.UserConstant.ADMIN_ROLE;
import static com.dong.project.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户服务实现类
 *
 * @author hwd
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Resource
    private RedissonLockUtil redissonLockUtil;


    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "hwd";

    /**
     * 用户注册
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long userRegister(String userName,String userAccount, String userPassword, String checkPassword, String invitationCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userName,userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userName.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户昵称过长");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短,4位以上");
        }
        //  账户不包含特殊字符
        // 匹配由数字、小写字母、大写字母组成的字符串,且字符串的长度至少为1个字符
        String pattern = "[0-9a-zA-Z]+";
        if (!userAccount.matches(pattern)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号由数字、小写字母、大写字母组成");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短，8位以上");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        //分布式锁
        String redissonLock = ("userRegister_"+ userAccount).intern();
        return redissonLockUtil.redissonDistributedLocks(redissonLock,()->{
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            //是否有输入邀请码
            User invitationCodeUser = null;
            if (StringUtils.isNotBlank(invitationCode)){
                //邀请码不为空
                LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
                userLambdaQueryWrapper.eq(User::getInvitationCode,invitationCode);
                //可能出现重复的邀请码，虽然概率不大，查出来的不是一条
                invitationCodeUser = this.getOne(userLambdaQueryWrapper);
                if (invitationCodeUser == null){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"该邀请码无效");
                }
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

            //3.分配accessKey和secretKey
            //DigestUtil.md5Hex():hutool工具的摘要加密,这里通过盐值、用户名、随机数生成的加密，其中随机数在hutool里面数字表示的是数值的长度
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            // 4. 插入数据,账号、密码、签名标识、签名密钥
            User user = new User();
            user.setUserName(userName);
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            if (invitationCodeUser != null){
                //有推荐人给注册账户一百,没有的话默认是30
                user.setBalance(100);
                //也给推荐人账户一百
                this.addWalletBalance(invitationCodeUser.getId(),100);
            }
            //创建8位数邀请码
            user.setInvitationCode(generateRandomString(8));
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        },"注册账户失败");
    }

    /**
     * 邮箱注册,邮箱账号不能为空，邮箱注册的邮箱号码作为用户账号
     * @param userEmailRegister 邮箱注册请求体
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long userEmailRegister(UserEmailRegister userEmailRegister) {
        String emailAccount = userEmailRegister.getEmailAccount();
        String userName = userEmailRegister.getUserName();
        String invitationCode = userEmailRegister.getInvitationCode();
        String userPassword = userEmailRegister.getUserPassword();
        String checkPassword = userEmailRegister.getCheckPassword();
        String captcha = userEmailRegister.getCaptcha();
        if (StringUtils.isAnyBlank(userName,emailAccount,captcha)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱注册请求数据为空");
        }
        //验证用户名、密码
        if (userName.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户昵称过长");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短，8位以上");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        //验证邮箱是否合法
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
        }
        //拿到redis缓存中的验证码, 有效期为5分钟,因为在controller层已经把发送验证码的请求单独封装为一个请求了，这可以直接拿
        String cacheCaptcha = redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + emailAccount);
        if (StringUtils.isBlank(cacheCaptcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"验证码有效期为5分钟，已过期");
        }
        //判断验证码是否和用户输入的一致
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"验证码错误!");
        }
        String redissonLock = ("userEmailRegister_" + emailAccount).intern();
        return redissonLockUtil.redissonDistributedLocks(redissonLock,()->{
            //账户不能重复
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("userAccount",emailAccount);
            Long count = userMapper.selectCount(userQueryWrapper);
            if (count > 0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号重复，请更换邮箱");
            }
            User invitationCodeUser = null;
            if (StringUtils.isNotBlank(invitationCode)){
                //如果邀请码不为空,检验邀请码用户是否存在数据库
                LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
                userLambdaQueryWrapper.eq(User::getInvitationCode,invitationCode);
                //可能出现不止一个，但是概率很低
                invitationCodeUser = userMapper.selectOne(userLambdaQueryWrapper);
                if (invitationCodeUser == null){
                    throw new BusinessException(ErrorCode.OPERATION_ERROR,"邀请码无效");
                }
            }
            //3.分配accessKey和secretKey
            //DigestUtil.md5Hex():hutool工具的摘要加密,这里通过盐值、用户名、随机数生成的加密，其中随机数在hutool里面数字表示的是数值的长度
            String accessKey = DigestUtil.md5Hex(SALT + emailAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + emailAccount + RandomUtil.randomNumbers(8));

            //插入数据
            User user = new User();
            user.setUserAccount(emailAccount);
            user.setUserName(userName);
            user.setEmail(emailAccount);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            //邀请码存在的话，加钱
            if (invitationCodeUser != null){
                //自身加个100
                user.setBalance(100);
                //推荐人也加100
                this.addWalletBalance(invitationCodeUser.getId(),100);
            }
            //生成8位数邀请码
            user.setInvitationCode(generateRandomString(8));
            boolean save = this.save(user);
            if (!save){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败，数据库异常");
            }
            return user.getId();
        },"邮箱注册失败");

    }


    /**
     * 登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return
     */
    @Override
    public UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
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
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        if (user.getStatus().equals(UserAccountStatusEnum.BAN.getValue())) {
            throw new BusinessException(ErrorCode.PROHIBITED, "该账号已被封禁,请联系管理员");
        }

        //将查询到的用户信息拷贝一份返回
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, userVO);
        return userVO;
    }

    /**
     * 邮箱登录
     * @param userEmailLoginRequest 邮箱包装类（邮箱、验证码）
     * @param request
     * @return
     */
    @Override
    public UserVO userEmailLogin(UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request) {
        String emailAccount = userEmailLoginRequest.getEmailAccount();
        String captcha = userEmailLoginRequest.getCaptcha();
        if (StringUtils.isAnyBlank(emailAccount,captcha)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"输入的邮箱或者验证码为空");
        }
        //检验邮箱的合法性
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern,emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
        }
        //获取redis中存储的验证码
        String cacheCaptcha = redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY+emailAccount);
        if (StringUtils.isBlank(cacheCaptcha)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码已过期,请重新获取");
        }
        //去除首尾空白字符
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
        }
        //查询用户是否存在
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("email",emailAccount);
        User user = userMapper.selectOne(userQueryWrapper);
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该邮箱未绑定账号,请先注册");
        }
        //判断用户是否被封禁了
        if (user.getStatus().equals(UserAccountStatusEnum.BAN.getValue())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该账号已被封禁,请联系管理员");
        }
        //将查询到的用户信息拷贝一份返回
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);

        //将用户状态存储在redis中
        request.getSession().setAttribute(USER_LOGIN_STATE,userVO);
        return userVO;
    }

    /**
     * 绑定邮箱
     * @param userBindEmailRequest
     * @param request
     * @return
     */
    @Override
    public UserVO userBindEmail(UserBindEmailRequest userBindEmailRequest, HttpServletRequest request) {
        if (userBindEmailRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String emailAccount = userBindEmailRequest.getEmailAccount();
        String captcha = userBindEmailRequest.getCaptcha();
        if (StringUtils.isAnyBlank(emailAccount,captcha)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //检验邮箱合法性
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
        }
        String cacheCaptcha = redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + emailAccount);
        if (StringUtils.isBlank(cacheCaptcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"验证码已过期");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"验证码错误");
        }
        //查看用户是否绑定该邮箱
        UserVO loginUser = this.getLoginUser(request);
        if (loginUser.getEmail() != null && emailAccount.equals(loginUser.getEmail())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"该账号已绑定此邮箱，请更换新的邮箱！");
        }
        //查看该邮箱是否已经被别人绑定
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("email",emailAccount);
        User one = this.getOne(userQueryWrapper);
        if (one != null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"此邮箱已被绑定，请更换新的邮箱");
        }
        User user = new User();
        user.setId(loginUser.getId());
        user.setEmail(emailAccount);
        boolean userBindEmailResult = this.updateById(user);
        if (!userBindEmailResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"邮箱绑定失败，请稍后再试");
        }
        //存入数据并返回，免了一次查询
        loginUser.setEmail(emailAccount);
        return loginUser;
    }

    /**
     * 解绑邮箱
     * @param userUnBindEmailRequest
     * @param request
     * @return
     */
    @Override
    public UserVO userUnBindEmail(UserUnBindEmailRequest userUnBindEmailRequest, HttpServletRequest request) {
        String emailAccount = userUnBindEmailRequest.getEmailAccount();
        String captcha = userUnBindEmailRequest.getCaptcha();
        if (StringUtils.isAnyBlank(emailAccount,captcha)){
            throw new BusinessException(ErrorCode.PROHIBITED,"解绑数据为空");
        }
        //检验邮箱是否合法
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
        }
        //拿到缓存的验证码
        String cacheCaptcha = redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + emailAccount);
        //检验验证码：1.是否过期 2.是否正确
        if (StringUtils.isBlank(cacheCaptcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"验证码已过期");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"验证码错误");
        }
        //查询用户是否绑定该邮箱
        UserVO loginUser = this.getLoginUser(request);
        if (loginUser.getEmail() == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"该账号并未绑定邮箱");
        }
        if (!emailAccount.equals(loginUser.getEmail())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"该账号未绑定该邮箱");
        }
        //解绑
        User user = new User();
        user.setId(loginUser.getId());
        user.setEmail(null);
        boolean userUnBindEmailResult = this.updateById(user);
        if (!userUnBindEmailResult){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"解绑邮箱失败，请稍后重试");
        }
        loginUser.setEmail(null);
        return loginUser;
    }




    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public UserVO getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserVO currentUser = (UserVO) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (user.getStatus().equals(UserAccountStatusEnum.BAN.getValue())){
            throw new BusinessException(ErrorCode.PROHIBITED,"账号已封禁");
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);

        return userVO;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserVO user = (UserVO) userObj;
        return user != null && ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 检验用户
     * @param user
     * @param add
     */
    @Override
    public void legalUser(User user, boolean add) {
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = user.getUserAccount();
        String userPassword = user.getUserPassword();
        Integer balance = user.getBalance();

        //创建时,所有参数必须非空
        if (add){
            //账号密码为空报错
            if (StringUtils.isAnyBlank(userAccount,userPassword)){
                throw new BusinessException(ErrorCode.PROHIBITED);
            }
            //添加用户生成8位数邀请码
            user.setInvitationCode(generateRandomString(8));
        }

        //有账号的话账号不包含特殊符号
        String pattern = "[0-9a-zA-Z]+";
        if (StringUtils.isNotBlank(userAccount) && !userAccount.matches(pattern)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号由数字、小写字母、大写字母组成");
        }
        //余额有的话不能小于0
        if (ObjectUtils.isNotEmpty(balance) && balance < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"余额不能小于0");
        }
        //有密码的话
        if (StringUtils.isNotBlank(userPassword)){
            //加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            user.setUserPassword(encryptPassword);
        }
        //账号不能重复
        if (StringUtils.isNotBlank(userAccount)){
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("userAccount",userAccount);
            Long count = userMapper.selectCount(userQueryWrapper);
            if (count > 0){
                throw new BusinessException(ErrorCode.PROHIBITED,"账号重复");
            }
        }
    }

    /**
     * 增加钱包余额
     * @param userId    用户id
     * @param addPoints 添加点
     * @return
     */
    @Override
    public boolean addWalletBalance(Long userId, Integer addPoints) {
        LambdaUpdateWrapper<User> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userLambdaUpdateWrapper.eq(User::getId, userId);
        userLambdaUpdateWrapper.setSql("balance = balance + " + addPoints);
        return this.update(userLambdaUpdateWrapper);
    }

    /**
     * 更新ak、sk
     * @param user
     * @return
     */
    @Override
    public UserVO updateVoucher(User user) {
        //分配accessKey和secretKey
        //DigestUtil.md5Hex():hutool工具的摘要加密,这里通过盐值、用户名、随机数生成的加密，其中随机数在hutool里面数字表示的是数值的长度
        String userAccount = user.getUserAccount();
        String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
        user.setAccessKey(accessKey);
        user.setSecretKey(secretKey);
        boolean result = this.updateById(user);
        if (!result){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"更新失败");
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return userVO;
    }

    /**
     * 是游客
     * @param request 要求
     * @return
     */
    @Override
    public User isTourist(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserVO currentUser = (UserVO) userObj;
        if (currentUser == null || currentUser.getId() == null){
            return null;
        }
        //从数据库查，追求性能的话可以注释，直接走缓存
        Long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 更新钱包余额，调用接口减少相对应的余额
     * @param userId      用户id
     * @param reduceScore 减少分数
     * @return
     */
    @Override
    public boolean reduceWalletBalance(Long userId, Long reduceScore) {
        LambdaUpdateWrapper<User> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userLambdaUpdateWrapper.eq(User::getId,userId);
        userLambdaUpdateWrapper.setSql("balance = balance - " + reduceScore);
        return this.update(userLambdaUpdateWrapper);
    }

    /**
     * 生成随机字符串
     *
     * @param length 长
     * @return {@link String}
     */
    public String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            char randomChar = characters.charAt(index);
            sb.append(randomChar);
        }
        return sb.toString();
    }

}




