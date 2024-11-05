package com.dong.project.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dong.dongapiclientsdk.client.DongApiClient;
import com.dong.dongapiclientsdk.model.request.CurrentRequest;
import com.dong.dongapiclientsdk.model.response.ResultResponse;
import com.dong.dongapiclientsdk.service.ApiService;
import com.dong.dongapicommon.model.entity.InterfaceInfo;
import com.dong.dongapicommon.model.entity.User;
import com.dong.project.annotation.AuthCheck;
import com.dong.project.common.*;
import com.dong.project.constant.CommonConstant;
import com.dong.project.exception.BusinessException;
import com.dong.project.model.dto.interfaceinfo.*;
import com.dong.project.model.enums.InterfaceInfoStatusEnum;
import com.dong.project.model.vo.UserVO;
import com.dong.project.service.InterfaceInfoService;
import com.dong.project.service.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dong.project.constant.UserConstant.ADMIN_ROLE;
import static com.dong.project.model.enums.InterfaceInfoStatusEnum.ONLINE;

/**
 * 帖子接口
 *
 * @author dong
 */
@RestController
@RequestMapping("/InterfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

/*    @Resource
    private DongApiClient dongApiClient;*/

    @Resource
    private ApiService apiService;




    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        //CollectionUtil检查集合,请求参数不为空
        if (CollectionUtil.isNotEmpty(interfaceInfoAddRequest.getRequestParams())){
            //拿到请求参数名不为空的数据
            List<RequestParamsField> requestParamsFields = interfaceInfoAddRequest.getRequestParams().stream().filter(field -> StringUtils.isNotBlank(field.getFieldName())).collect(Collectors.toList());
            String requestParams = JSONUtil.toJsonStr(requestParamsFields);
            interfaceInfo.setRequestParams(requestParams);
        }
        if (CollectionUtil.isNotEmpty(interfaceInfoAddRequest.getResponseParams())){
            List<ResponseParamsField> responseParamsFields = interfaceInfoAddRequest.getResponseParams().stream().filter(field -> StringUtils.isNotBlank(field.getFieldName())).collect(Collectors.toList());
            String responseParams = JSONUtil.toJsonStr(responseParamsFields);
            interfaceInfo.setResponseParams(responseParams);
        }
        BeanUtils.copyProperties(interfaceInfoAddRequest,interfaceInfo);
        //校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo,true);
        UserVO loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        boolean result = interfaceInfoService.save(interfaceInfo);
        if (!result){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        Long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);

//        BeanUtils.copyProperties(InterfaceInfoAddRequest, InterfaceInfo);
//        // 校验
//        interfaceInfoService.validInterfaceInfo(InterfaceInfo, true);
//        UserVO loginUser = userService.getLoginUser(request);
//        InterfaceInfo.setUserId(loginUser.getId());
//        boolean result = interfaceInfoService.save(InterfaceInfo);
//        if (!result) {
//            throw new BusinessException(ErrorCode.OPERATION_ERROR);
//        }
//        long newInterfaceInfoId = InterfaceInfo.getId();
//        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param InterfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest InterfaceInfoUpdateRequest,
                                            HttpServletRequest request) {
        if (InterfaceInfoUpdateRequest == null || InterfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo InterfaceInfo = new InterfaceInfo();
        //1.请求参数集合不为空
        if (CollectionUtil.isNotEmpty(InterfaceInfoUpdateRequest.getRequestParams())){
            List<RequestParamsField> requestParamsFields = InterfaceInfoUpdateRequest.getRequestParams().stream().filter(field -> StringUtils.isNotBlank(field.getFieldName())).collect(Collectors.toList());
            String requestParams = JSONUtil.toJsonStr(requestParamsFields);
            InterfaceInfo.setRequestParams(requestParams);
        }else {
            InterfaceInfo.setRequestParams("[]");
        }
        //2.响应参数集合不为空
        if (CollectionUtil.isNotEmpty(InterfaceInfoUpdateRequest.getResponseParams())){
            List<ResponseParamsField> responseParamsFields = InterfaceInfoUpdateRequest.getResponseParams().stream().filter(field -> StringUtils.isNotBlank(field.getFieldName())).collect(Collectors.toList());
            String responseParams = JSONUtil.toJsonStr(responseParamsFields);
            InterfaceInfo.setResponseParams(responseParams);
        }else {
            InterfaceInfo.setResponseParams("[]");
        }

        BeanUtils.copyProperties(InterfaceInfoUpdateRequest, InterfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(InterfaceInfo, false);
        UserVO user = userService.getLoginUser(request);
        long id = InterfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = interfaceInfoService.updateById(InterfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfo> getInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo InterfaceInfo = interfaceInfoService.getById(id);
        return ResultUtils.success(InterfaceInfo);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param InterfaceInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest InterfaceInfoQueryRequest) {
        InterfaceInfo InterfaceInfoQuery = new InterfaceInfo();
        if (InterfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(InterfaceInfoQueryRequest, InterfaceInfoQuery);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(InterfaceInfoQuery);
        List<InterfaceInfo> InterfaceInfoList = interfaceInfoService.list(queryWrapper);
        return ResultUtils.success(InterfaceInfoList);
    }

    /**
     * 分页获取列表
     *
     * @param InterfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest InterfaceInfoQueryRequest, HttpServletRequest request) {
        if (InterfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo InterfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(InterfaceInfoQueryRequest, InterfaceInfoQuery);
        //其实name和description的值是一样的都是搜索框输入的数据
        long current = InterfaceInfoQueryRequest.getCurrent();
        long size = InterfaceInfoQueryRequest.getPageSize();
        String name = InterfaceInfoQuery.getName();
        String sortField = InterfaceInfoQueryRequest.getSortField();
        String sortOrder = InterfaceInfoQueryRequest.getSortOrder();
        String description = InterfaceInfoQuery.getDescription();
        //这边暂时用不到还少写了reduceScore和returnFormat
        String url = InterfaceInfoQuery.getUrl();
        String method = InterfaceInfoQuery.getMethod();
        Integer status = InterfaceInfoQuery.getStatus();
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> interfaceInfoQueryWrapper = new QueryWrapper<>();
        //搜索框搜索，如果有描述或者名字
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(description)){
            interfaceInfoQueryWrapper.and(qw -> qw.like("name",name).or().
                    like("description",description));
        }
        //其他查询条件
        interfaceInfoQueryWrapper
                .like(StringUtils.isNotBlank(url),"url",url)
                .eq(StringUtils.isNotBlank(method),"method",method)
                .eq(ObjectUtils.isNotEmpty(status),"status",status);

        //如果不是管理员，则只查询上线的接口
        User isTouristUser = userService.isTourist(request);
        if (isTouristUser == null || !isTouristUser.getUserRole().equals(ADMIN_ROLE)){
            interfaceInfoQueryWrapper.eq("status",ONLINE.getValue());
        }
        //排序

        interfaceInfoQueryWrapper.orderBy(StringUtils.isNotBlank(sortField),sortOrder.equals(CommonConstant.SORT_ORDER_ASC),sortField);
        Page<InterfaceInfo> InterfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), interfaceInfoQueryWrapper);
        return ResultUtils.success(InterfaceInfoPage);
        /*//不是管理员只能查看已经上线的了,如果是游客或者普通用户
        //todo 这边不能一下子拿到所有的数据再进行分页展示，就会导致可能第一页数据为空，第二页才有数据，得想办法把所有的数据先筛选
        if (isTouristUser == null || !isTouristUser.getUserRole().equals(ADMIN_ROLE)){
            //过滤掉下线的接口信息,首先，通过interfaceInfoPage.getRecords()获取到当前interfaceInfoPage对象中存储的所有InterfaceInfo对象的列表。
            List<InterfaceInfo> filterInterfaceInfo = InterfaceInfoPage.getRecords().stream()
                    .filter(interfaceInfo -> interfaceInfo.getStatus().equals(ONLINE.getValue()))
                    .collect(Collectors.toList());
            InterfaceInfoPage.setRecords(filterInterfaceInfo);
        }*/
    }

    /**
     * 搜索框搜索接口信息
     * @param interfaceInfoSearchTextRequest
     * @param request
     * @return
     */
    @PostMapping("/get/searchText")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoBySearchTextPage(InterfaceInfoSearchTextRequest interfaceInfoSearchTextRequest, HttpServletRequest request){
        if (interfaceInfoSearchTextRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoSearchTextRequest,interfaceInfo);

        String searchText = interfaceInfoSearchTextRequest.getSearchText();
        long current = interfaceInfoSearchTextRequest.getCurrent();
        long pageSize = interfaceInfoSearchTextRequest.getPageSize();
        String sortOrder = interfaceInfoSearchTextRequest.getSortOrder();
        String sortField = interfaceInfoSearchTextRequest.getSortField();

        QueryWrapper<InterfaceInfo> interfaceInfoQueryWrapper = new QueryWrapper<>();
        //如果有查询值
        if (StringUtils.isNotBlank(searchText)){
            interfaceInfoQueryWrapper
                    .and(qw -> qw.like("name",searchText)
                    .or()
                    .like("description",searchText));
        }
        //不是管理员只能查看上线的，也就是游客和普通用户只能查看发布的接口
        if (!userService.isAdmin(request)){
            interfaceInfoQueryWrapper.eq("status",ONLINE.getValue());
        }
        //排序
        interfaceInfoQueryWrapper.orderBy(StringUtils.isNotBlank(sortOrder),sortOrder.equals(CommonConstant.SORT_ORDER_ASC),sortField);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, pageSize), interfaceInfoQueryWrapper);
       /* //不是管理员只能查看上线的，也就是游客与普通用户只能看上线的,filter存在则要，不存在则丢弃
        if (!userService.isAdmin(request)){
            List<InterfaceInfo> onlineInterfaceInfo = interfaceInfoPage.getRecords().stream().
                    filter(interfaceInfo1 -> interfaceInfo1.getStatus().equals(ONLINE.getValue()))
                    .collect(Collectors.toList());
            interfaceInfoPage.setRecords(onlineInterfaceInfo);
        }*/
        return ResultUtils.success(interfaceInfoPage);

    }


    /**
     * 接口发布
     *
     * @param idRequest
     * @param request
     * @return
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest,
                                                     HttpServletRequest request) {
        //传入的id
        Long id = idRequest.getId();
        //1.判断id是否存在
        if (idRequest == null || id <= 0 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.判断接口是否可用
      /*  com.dong.dongapiclientsdk.model.User user = new com.dong.dongapiclientsdk.model.User();
        user.setUsername("黄伟东1号");
        String username = dongApiClient.getUsernameByPost(user);
        if (StringUtils.isBlank(username)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口验证失败");
        }*/
        //3.修改数据库中status字段为1
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(ONLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 接口下线
     *
     * @param idRequest
     * @param request
     * @return
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest,
                                                     HttpServletRequest request) {
        //传入的id
        Long id = idRequest.getId();
        //1.判断id是否存在
        if (idRequest == null || id <= 0 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //3.修改数据库中status字段为0
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 管理员修改接口头像
     * @param avatarRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @PostMapping("/updateInterfaceInfoAvatar")
    public BaseResponse<Boolean> updateInterfaceInfoAvatar(InterfaceInfoUpdateAvatarRequest avatarRequest,HttpServletRequest request){
        if (ObjectUtils.anyNull(avatarRequest,avatarRequest.getId()) || avatarRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(avatarRequest,interfaceInfo);
        return ResultUtils.success(interfaceInfoService.updateById(interfaceInfo));
    }


    /**
     * 测试调用
     * @param interfaceInfoInvokeRequest
     * @param request
     * @return
     */
    @PostMapping("/invoke")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                      HttpServletRequest request) {
        if (ObjectUtils.anyNull(interfaceInfoInvokeRequest,interfaceInfoInvokeRequest.getId()) || interfaceInfoInvokeRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long id = interfaceInfoInvokeRequest.getId();
        //1.查看接口是否存在，是否正常状态
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"该接口不存在");
        }
        //如果是关闭就报错
        if (interfaceInfo.getStatus() == InterfaceInfoStatusEnum.OFFLINE.getValue()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"接口已关闭");
        }
        //2.构建请求参数
        List<InterfaceInfoInvokeRequest.Field> fieldList = interfaceInfoInvokeRequest.getRequestParams();
        String requestParams = "{}";
        //请求参数存在,初始化请求参数
        if (fieldList != null && fieldList.size() > 0){
            JsonObject jsonObject = new JsonObject();
            for (InterfaceInfoInvokeRequest.Field field : fieldList) {
                jsonObject.addProperty(field.getFieldName(), field.getValue());
            }
            log.info("jsonObject==>",jsonObject.toString());
            //将对象序列化为JSON字符串
            requestParams = new Gson().toJson(jsonObject);
        }
        // 使用Gson的fromJson方法和TypeToken来将JSON字符串转换为Map<String, Object>
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> params = gson.fromJson(requestParams, mapType);

        //调用测试
        UserVO loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();

        ResultResponse response = null;
        try {
            DongApiClient dongApiClient = new DongApiClient(accessKey,secretKey);
            CurrentRequest currentRequest = new CurrentRequest();
            currentRequest.setMethod(interfaceInfo.getMethod());
            currentRequest.setPath(interfaceInfo.getUrl());
            currentRequest.setRequestParams(params);
            response = apiService.request(dongApiClient, currentRequest);
            if (ObjectUtils.isEmpty(response.getData().get("code"))){
                return ResultUtils.success(response.getData());
            }else {
                return ResultUtils.error((Integer) response.getData().get("code"),response.getData(),(String) response.getData().get("errorMessage"));
            }

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,e.getMessage());
        }
//        //string转为对象，
//        Gson gson = new Gson();
//        com.dong.dongapiclientsdk.model.User user = gson.fromJson(userRequestParams, com.dong.dongapiclientsdk.model.User.class);
//        String usernameByPost = tempClient.getUsernameByPost(user);
//        return ResultUtils.success(usernameByPost);
    }


}
