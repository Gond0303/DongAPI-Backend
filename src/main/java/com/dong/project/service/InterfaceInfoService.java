package com.dong.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.dong.dongapicommon.model.entity.InterfaceInfo;

/**
* @author 黄伟东
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2024-06-18 17:18:59
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    /**
     * 校验
     *
     * @param interfaceInfo
     * @param add 是否为创建校验
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    /**
     * 更新总调用数
     *
     * @param interfaceId 接口id
     * @return boolean
     */
    boolean updateTotalInvokes(long interfaceId);
}
