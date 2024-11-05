package com.dong.project.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.dongapicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author 黄伟东
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
* @createDate 2024-07-10 18:58:08
* @Entity com.dong.project.model.entity.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {



    /*
    接口调用次数TOP3
    select interfaceInfoId,sum(totalNum) as totalNum from user_interface_info group by interfaceInfoId
    order by totalNum desc limit 3
   */
    List<UserInterfaceInfo> ListTopInvokeInterfaceInfo(int limit);
}




