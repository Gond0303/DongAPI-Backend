package com.dong.project.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dong.dongapicommon.model.entity.User;
import com.dong.dongapicommon.model.vo.UserVO;
import com.dong.dongapicommon.service.InnerUserService;
import com.dong.project.common.ErrorCode;
import com.dong.project.exception.BusinessException;
import com.dong.project.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;


@DubboService
public class InnerUserServiceImpl implements InnerUserService {


    @Resource
    private UserMapper userMapper;

    /**
     * 1.数据库中查是否已分配给用户密钥（accessKey、secretKey，返回用户信息，为空表示不存在）
     * @param accessKey
     * @return
     */
    @Override
    public UserVO getInvokeUser(String accessKey) {
        if (StringUtils.isBlank(accessKey)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getAccessKey,accessKey);
        User user = userMapper.selectOne(userLambdaQueryWrapper);
        if (user == null){
            return  null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return userVO;
    }
}
