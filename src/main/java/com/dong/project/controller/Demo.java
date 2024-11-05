package com.dong.project.controller;

import com.dong.dongapiclientsdk.client.DongApiClient;
import com.dong.dongapiclientsdk.exception.ApiException;
import com.dong.dongapiclientsdk.model.params.*;
import com.dong.dongapiclientsdk.model.request.*;
import com.dong.dongapiclientsdk.model.response.LoveResponse;
import com.dong.dongapiclientsdk.model.response.ResultResponse;
import com.dong.dongapiclientsdk.service.ApiService;
import com.dong.project.common.BaseResponse;
import com.dong.project.common.ResultUtils;
import com.dong.project.exception.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/cs")
public class Demo {
    @Resource
    private  ApiService apiService;


    /**
     * 随机情话
     * @return
     */
    @GetMapping("/loveTalk")
    public LoveResponse loveResponse(){
        LoveResponse loveResponse = null;
        try {
            loveResponse = apiService.randomLoveTalk();
            System.out.println("loveResponse:"+loveResponse.getData());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return loveResponse;
    }

    @GetMapping("/loveTalk/easyWeb")
    public  BaseResponse<LoveResponse> loveResponses(){
        LoveResponse loveResponse = null;
        try {
            loveResponse = apiService.randomLoveTalk();
        } catch (ApiException e) {
            throw new BusinessException(e.getCode(), e.getMessage());
        }
        return ResultUtils.success(loveResponse);
    }

    @GetMapping("/loveTalk/setKey")
    public LoveResponse loveResponseByKey(){
        LoveResponse loveResponse;
        DongApiClient dongApiClient = new DongApiClient("b7d5557e8027f0b898bcd2e51ba551bc","db9d4b82f32670fa1c67889a73639dd9");
        try {
            loveResponse = apiService.randomLoveTalk(dongApiClient);
        } catch (ApiException e) {
            throw new BusinessException(e.getCode(), e.getMessage());
        }
        return loveResponse;
    }



    @GetMapping("weatherInfo")
    public ResultResponse getWeatherInfo(WeatherParams weatherParams){
        ResultResponse resultResponse;
        try {
            WeatherRequest weatherRequest = new WeatherRequest();
            weatherRequest.setRequestParams(weatherParams);
            resultResponse = apiService.weatherResponse(weatherRequest);
            System.out.println("resultResponse:"+resultResponse.getData());
        } catch (ApiException e) {
            throw new BusinessException(e.getCode(), e.getMessage());
        }
        return resultResponse;
    }


    /**
     * 抖音视频
     * @param dyParams
     * @return
     */
    @GetMapping("/getDy")
    public ResultResponse getDy(DyParams dyParams){
        ResultResponse resultResponse;
        try {
            dyParams.setType("json");
            DyRequest dyRequest = new DyRequest();
            dyRequest.setRequestParams(dyParams);
            resultResponse = apiService.dyResponse(dyRequest);
            System.out.println("resultResponse:"+resultResponse.getData());
        } catch (ApiException e) {
            throw new BusinessException(e.getCode(), e.getMessage());
        }
        return resultResponse;
    }

    @GetMapping("/getDy/easyWeb")
    public BaseResponse<ResultResponse> getDyEasyWeb(DyParams dyParams) {
        ResultResponse resultResponse;
        try {
            dyParams.setType("json");
            DyRequest dyRequest = new DyRequest();
            dyRequest.setRequestParams(dyParams);
            resultResponse = apiService.dyResponse(dyRequest);
        } catch (ApiException e) {
            throw new BusinessException(e.getCode(), e.getMessage());
        }
        return ResultUtils.success(resultResponse);
    }


    @GetMapping("/getDy/setKey")
    public ResultResponse getDyBySetKey(DyParams dyParams){
        ResultResponse resultResponse;
        DongApiClient dongApiClient = new DongApiClient("b7d5557e8027f0b898bcd2e51ba551bc","db9d4b82f32670fa1c67889a73639dd9");
        dyParams.setType("json");
        try {
            DyRequest dyRequest = new DyRequest();
            dyRequest.setRequestParams(dyParams);
            resultResponse = apiService.dyResponse(dongApiClient,dyRequest);
        } catch (ApiException e) {
            throw new BusinessException(e.getCode(), e.getMessage());
        }
        return resultResponse;
    }


    /**
     * 网易云音乐测试
     * @param wyyMusicParams
     * @return
     */
    @GetMapping("/getWyy")
    public ResultResponse getWyyMusic(WyyMusicParams wyyMusicParams){
        wyyMusicParams.setType("json");
        ResultResponse resultResponse;
        try {
            WyyMusicRequest wyyMusicRequest = new WyyMusicRequest();
            wyyMusicRequest.setRequestParams(wyyMusicParams);
            resultResponse = apiService.wyyMusicResponse(wyyMusicRequest);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return resultResponse;
    }


    @GetMapping("/getWyy/easyWeb")
    public BaseResponse<ResultResponse> getWyyMusicByEasyWeb(WyyMusicParams wyyMusicParams){
        wyyMusicParams.setType("json");
        ResultResponse resultResponse;
        try {
            WyyMusicRequest wyyMusicRequest = new WyyMusicRequest();
            wyyMusicRequest.setRequestParams(wyyMusicParams);
            resultResponse = apiService.wyyMusicResponse(wyyMusicRequest);
        } catch (ApiException e) {
            throw new BusinessException(e.getCode(), e.getMessage());
        }
        return ResultUtils.success(resultResponse);
    }


    @GetMapping("/getWyy/setKey")
    public ResultResponse getWyyMusicBySetKey(WyyMusicParams wyyMusicParams){
        wyyMusicParams.setType("json");
        ResultResponse resultResponse;
        try {
            DongApiClient dongApiClient = new DongApiClient("b7d5557e8027f0b898bcd2e51ba551bc","db9d4b82f32670fa1c67889a73639dd9");
            WyyMusicRequest wyyMusicRequest = new WyyMusicRequest();
            wyyMusicRequest.setRequestParams(wyyMusicParams);
            resultResponse = apiService.wyyMusicResponse(dongApiClient,wyyMusicRequest);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return resultResponse;
    }

    /**
     * 百度短剧
     * @param bddjParams
     * @return
     */
    @GetMapping("/bddj")
    public ResultResponse getBddj(BddjParams bddjParams){
        ResultResponse resultResponse;
        bddjParams.setText("换亲");
        try {
            BddjRequest bddjRequest = new BddjRequest();
            bddjRequest.setRequestParams(bddjParams);
            resultResponse = apiService.bddjResponse(bddjRequest);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return resultResponse;
    }

    @GetMapping("/bddj/easyWeb")
    public BaseResponse<ResultResponse> getBddjByEasyWeb(BddjParams bddjParams){
        ResultResponse resultResponse;
        bddjParams.setText("换亲");

        try {
            BddjRequest bddjRequest = new BddjRequest();
            bddjRequest.setRequestParams(bddjParams);
            resultResponse = apiService.bddjResponse(bddjRequest);
        } catch (ApiException e) {
            throw new BusinessException(e.getCode(), e.getMessage());
        }
        return ResultUtils.success(resultResponse);
    }


    @GetMapping("/bddj/setKey")
    public ResultResponse getBddjBySetKey(BddjParams bddjParams){
        ResultResponse resultResponse;
        bddjParams.setText("换亲");
        try {
            DongApiClient dongApiClient = new DongApiClient("b7d5557e8027f0b898bcd2e51ba551bc","db9d4b82f32670fa1c67889a73639dd9");
            BddjRequest bddjRequest = new BddjRequest();
            bddjRequest.setRequestParams(bddjParams);
            resultResponse = apiService.bddjResponse(dongApiClient,bddjRequest);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return resultResponse;
    }


    /**
     * 小众头像
     * @param avatarParams
     * @return
     */
    @GetMapping("/avatar")
    public ResultResponse getAvatar(AvatarParams avatarParams){
        avatarParams.setType("json");
        ResultResponse resultResponse;
        try {
            AvatarRequest avatarRequest = new AvatarRequest();
            avatarRequest.setRequestParams(avatarParams);
            resultResponse = apiService.avatarResponse(avatarRequest);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return resultResponse;
    }

    @GetMapping("/avatar/easyWeb")
    public BaseResponse<ResultResponse> getAvatarByEasyWeb(AvatarParams avatarParams){
        avatarParams.setType("json");
        ResultResponse resultResponse;
        try {
            AvatarRequest avatarRequest = new AvatarRequest();
            avatarRequest.setRequestParams(avatarParams);
            resultResponse = apiService.avatarResponse(avatarRequest);
        } catch (ApiException e) {
            throw new BusinessException(e.getCode(), e.getMessage());
        }
        return ResultUtils.success(resultResponse);
    }


    @GetMapping("/avatar/setKey")
    public ResultResponse getAvatarBySetKey(AvatarParams avatarParams){
        avatarParams.setType("json");
        ResultResponse resultResponse;
        try {
            AvatarRequest avatarRequest = new AvatarRequest();
            avatarRequest.setRequestParams(avatarParams);
            DongApiClient dongApiClient = new DongApiClient("b7d5557e8027f0b898bcd2e51ba551bc","db9d4b82f32670fa1c67889a73639dd9");
            resultResponse = apiService.avatarResponse(dongApiClient,avatarRequest);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return resultResponse;
    }


    @GetMapping("/randomWallpaper")
    public ResultResponse getRandomWallpaper() {
        ResultResponse resultResponse;
        try {
            RandomWallpaperRequest randomWallpaperRequest = new RandomWallpaperRequest();
            resultResponse = apiService.getRandomWall(randomWallpaperRequest);
        } catch (ApiException e) {
            throw new BusinessException(e.getCode(), e.getMessage());
        }
        return resultResponse;
    }







}
