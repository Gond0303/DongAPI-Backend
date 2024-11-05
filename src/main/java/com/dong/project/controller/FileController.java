package com.dong.project.controller;

import cn.hutool.core.io.FileUtil;
import com.dong.project.common.AliOSSUtils;
import com.dong.project.common.BaseResponse;
import com.dong.project.common.ErrorCode;
import com.dong.project.common.ResultUtils;
import com.dong.project.exception.BusinessException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;

/**
 * 文件上传 阿里OSS
 */
@RestController
@RequestMapping("/file")
public class FileController {
    final long MaxSize = 1024 * 1024L;
    @Resource
    private AliOSSUtils aliOSSUtils;

    /**
     * 文件上传
     * @param multipartFile
     * @return
     */
    @PostMapping("/upload")
    //@RequestPart 是 Spring MVC 框架中的一个注解，它主要用于处理 HTTP 请求中的“部分”内容，特别是在处理 multipart/form-data 类型的请求时，比如文件上传等场景。
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile){
        //检验文件格式
        validFile(multipartFile);

        String imgUrl = null;
        try {
            imgUrl = aliOSSUtils.upload(multipartFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (imgUrl == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"头像上传到阿里oss失败");
        }
        return ResultUtils.success(imgUrl);
    }

    /**
     * 文件检测
     * @param multipartFile
     */
    private void validFile(MultipartFile multipartFile){
        //文件大小
        long size = multipartFile.getSize();
        //文件后缀
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        if (size > MaxSize){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件过大，请重新上传");
        }
        if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(suffix)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"文件后缀不是图片格式");
        }
    }
}
