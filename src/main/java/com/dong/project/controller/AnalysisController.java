package com.dong.project.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.dongapicommon.model.entity.InterfaceInfo;
import com.dong.dongapicommon.model.entity.UserInterfaceInfo;
import com.dong.project.annotation.AuthCheck;
import com.dong.project.common.BaseResponse;
import com.dong.project.common.ErrorCode;
import com.dong.project.common.ResultUtils;
import com.dong.project.exception.BusinessException;
import com.dong.project.mapper.UserInterfaceInfoMapper;
import com.dong.project.service.InterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员分析控制器
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoService interfaceInfoService;


    /**
     * 管理员可视功能
     * @return
     */
    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<InterfaceInfo>> ListTopInvokeInterfaceInfo(){
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoMapper.ListTopInvokeInterfaceInfo(3);
        //根据接口id分组
        Map<Long, List<UserInterfaceInfo>> interfaceInfoIdObjMap = userInterfaceInfoList.stream().
                collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));

        QueryWrapper<InterfaceInfo> interfaceInfoQueryWrapper = new QueryWrapper<>();

        System.out.println("interfaceInfoMap的key，也就是id："+ interfaceInfoIdObjMap.keySet());
        interfaceInfoIdObjMap.entrySet().stream().forEach(entry -> {
            System.out.println("interfaceInfoMap的值："+entry.getValue());
        });

        interfaceInfoQueryWrapper.in("id",interfaceInfoIdObjMap.keySet());
        List<InterfaceInfo> list = interfaceInfoService.list(interfaceInfoQueryWrapper);
        if (CollectionUtil.isEmpty(list)){
            //除非你这张表一条数据都没有，正常不会
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
//        List<InterfaceInfoVO> interfaceInfoVOList = list.stream().map(interfaceInfo -> {
//            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
//            //将查到的list复制给vo
//            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
//            Long totalNum = interfaceInfoIdObjMap.get(interfaceInfo.getId()).get(0).getTotalInvokes();
//            interfaceInfoVO.setTotalNum(totalNum);
//            return interfaceInfoVO;
//        }).collect(Collectors.toList());
        return ResultUtils.success(list);


    }
}
