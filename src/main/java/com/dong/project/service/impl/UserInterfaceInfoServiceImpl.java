package com.dong.project.service.impl;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.dongapicommon.model.entity.UserInterfaceInfo;
import com.dong.project.common.ErrorCode;
import com.dong.project.exception.BusinessException;
import com.dong.project.mapper.UserInterfaceInfoMapper;
import com.dong.project.service.UserInterfaceInfoService;
import org.springframework.stereotype.Service;

/**
* @author 黄伟东
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
* @createDate 2024-07-10 18:58:08
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService{

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //创建时，所以参数必须非空
        if (add){
            if (userInterfaceInfo.getInterfaceInfoId() <= 0 || userInterfaceInfo.getUserId() <= 0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"接口或用户不存在");
            }
        }
    }

    /**
     * 调用接口统计
     * @param interfaceInfoId   接口id
     * @param userId            调用的用户id
     * @return
     */
    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        //判断
        if (interfaceInfoId <= 0 || userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数异常");
        }
        //修改数据库字段，总调用总数+1，剩余调用数-1
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceInfoId",interfaceInfoId);
        updateWrapper.eq("userId",userId);
        //todo 要加验证，如果剩余次数为0之后会报错，没校验。而且这边得加个锁，因为可能一瞬间调用很多次
//        updateWrapper.gt("leftNum",0);
        updateWrapper.setSql("totalNum = totalNum + 1");

        boolean result = this.update(updateWrapper);
        return result;
    }
}




