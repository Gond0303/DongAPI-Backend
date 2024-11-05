package com.dong.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.dong.dongapicommon.model.entity.UserInterfaceInfo;

/**
* @author 黄伟东
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2024-07-10 18:58:08
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    /**
     * 校验
     *
     * @param userInterfaceInfo
     * @param add 是否为创建校验
     */
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);


    /**
     * 调用接口统计
     * @param interfaceInfoId   接口id
     * @param userId            调用的用户id
     * @return
     */
    boolean invokeCount(long interfaceInfoId,long userId);

}
