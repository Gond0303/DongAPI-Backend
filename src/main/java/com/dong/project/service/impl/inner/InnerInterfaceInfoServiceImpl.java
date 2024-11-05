package com.dong.project.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.dongapicommon.model.entity.InterfaceInfo;
import com.dong.dongapicommon.service.InnerInterfaceInfoService;
import com.dong.project.common.ErrorCode;
import com.dong.project.exception.BusinessException;
import com.dong.project.mapper.InterfaceInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;


@DubboService
@Slf4j
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    /**
     * 2.从数据库中查询模拟接口是否存在（请求路径、请求方法、请求参数，返回接口信息，为空表示不存在）
     * @param url
     * @param method
     * @return
     */
    @Override
    public InterfaceInfo getInterfaceInfo(String url, String method) {
        if (StringUtils.isAnyBlank(url,method)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //如果是get请求只要前面的不要后面的携带参数
        if (url.contains("?")){
            url = url.substring(0,url.indexOf("?"));
        }
        //todo 不要协议的前缀,这边是因为网关不知道为什么接收到的地址是http的但是其实发送的是https的这边修改成https
        if (url.startsWith("http://")){
            url = url.substring(7);
        }
        url = "https://" + url;
        log.info("【查询的地址】:"+url);

        QueryWrapper<InterfaceInfo> interfaceInfoQueryWrapper = new QueryWrapper<>();
        interfaceInfoQueryWrapper.eq("url",url);
        interfaceInfoQueryWrapper.eq("method",method);
        //通过接口路径、请求方法查询是否存在该接口
        return interfaceInfoMapper.selectOne(interfaceInfoQueryWrapper);
    }
}
