package com.dong.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dong.project.common.BaseResponse;
import com.dong.project.common.ErrorCode;
import com.dong.project.common.ResultUtils;
import com.dong.project.exception.BusinessException;
import com.dong.project.model.entity.DailyCheckIn;
import com.dong.project.model.vo.UserVO;
import com.dong.project.service.DailyCheckInService;
import com.dong.project.service.UserService;
import com.dong.project.utils.RedissonLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户每日签到
 */
@RestController
@RequestMapping("/dailyCheckIn")
@Slf4j
public class DailyCheckInController {
    @Resource
    private DailyCheckInService dailyCheckInService;
    @Resource
    private UserService userService;
    @Resource
    private RedissonLockUtil redissonLockUtil;

    /**
     * 用户签到
     * @param request
     * @return
     */
    @PostMapping("/doCheckIn")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> doDailyCheckIn(HttpServletRequest request){
        UserVO loginUser = userService.getLoginUser(request);
        String redissonLock = ("doDailyCheckIn_" + loginUser.getUserAccount()).intern();
        return redissonLockUtil.redissonDistributedLocks(redissonLock,() -> {
            LambdaQueryWrapper<DailyCheckIn> dailyCheckInLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dailyCheckInLambdaQueryWrapper.eq(DailyCheckIn::getUserId,loginUser.getId());
            DailyCheckIn dailyCheckIn = dailyCheckInService.getOne(dailyCheckInLambdaQueryWrapper);
            //如果没有
            if (ObjectUtils.isNotEmpty(dailyCheckIn)){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"签到失败，今日已签到");
            }
            dailyCheckIn = new DailyCheckIn();
            dailyCheckIn.setUserId(loginUser.getId());
            dailyCheckIn.setAddPoints(10);
            boolean dailyCheckInResult = dailyCheckInService.save(dailyCheckIn);
            boolean addWalletBalanceResult = userService.addWalletBalance(loginUser.getId(), 10);
            boolean result = dailyCheckInResult & addWalletBalanceResult;
            //失败
            if (!result){
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            return ResultUtils.success(true);
        },"签到失败");
    }
}
