package com.dong.project.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.dongapicommon.model.entity.UserInterfaceInfo;
import com.dong.dongapicommon.service.InnerUserInterfaceInfoService;
import com.dong.project.common.ErrorCode;
import com.dong.project.exception.BusinessException;
import com.dong.project.mapper.UserInterfaceInfoMapper;
import com.dong.project.service.InterfaceInfoService;
import com.dong.project.service.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@DubboService
public class InnerUserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements InnerUserInterfaceInfoService {

/*    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;*/

    @Resource
    private InterfaceInfoService interfaceInfoService;
    @Resource
    private UserService userService;
    /**
     * 3.调用接口统计,调用成功，调用次数+1,用户积分减少
     * @param interfaceInfoId   接口id
     * @param userId            调用的用户id
     * @param reduceScore       减少的积分
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean invoke(Long interfaceInfoId, Long userId, Long reduceScore) {
        if (ObjectUtils.anyNull(interfaceInfoId,userId,reduceScore)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"数据为空");
        }
        LambdaQueryWrapper<UserInterfaceInfo> userInterfaceInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userInterfaceInfoLambdaQueryWrapper.eq(UserInterfaceInfo::getUserId,userId);
        userInterfaceInfoLambdaQueryWrapper.eq(UserInterfaceInfo::getInterfaceInfoId,interfaceInfoId);
        UserInterfaceInfo userInterfaceInvoke  = this.getOne(userInterfaceInfoLambdaQueryWrapper);

        //不存在就创建一条该用户与该接口的数据（用户-接口信息表）
        boolean invokeResult;
        if (userInterfaceInvoke == null){
            UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setUserId(userId);
            userInterfaceInfo.setInterfaceInfoId(interfaceInfoId);
            userInterfaceInfo.setTotalInvokes(1L);
            invokeResult = this.save(userInterfaceInfo);
        }else {
            //存在则接口调用次数加一
            LambdaUpdateWrapper<UserInterfaceInfo> userInterfaceInfoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            userInterfaceInfoLambdaUpdateWrapper.eq(UserInterfaceInfo::getInterfaceInfoId,interfaceInfoId);
            userInterfaceInfoLambdaUpdateWrapper.eq(UserInterfaceInfo::getUserId,userId);
            userInterfaceInfoLambdaUpdateWrapper.setSql("totalInvokes = totalInvokes + 1");
            invokeResult = this.update(userInterfaceInfoLambdaUpdateWrapper);
        }
        //更新接口总调用次数(接口表)
        boolean interfaceUpdateInvokeSave = interfaceInfoService.updateTotalInvokes(interfaceInfoId);
        // 更新用户钱包积分（用户表）
        boolean reduceWalletBalanceResult = userService.reduceWalletBalance(userId, reduceScore);
        boolean updateResult = invokeResult && interfaceUpdateInvokeSave && reduceWalletBalanceResult;
        if (!updateResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "调用失败");
        }
        return true;

    }
}
