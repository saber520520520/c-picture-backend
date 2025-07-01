package com.cpi.cpicturebackend.controller;


import com.cpi.cpicturebackend.annotation.AuthCheck;
import com.cpi.cpicturebackend.common.BaseResponse;
import com.cpi.cpicturebackend.common.ResultUtils;
import com.cpi.cpicturebackend.constant.UserConstant;
import com.cpi.cpicturebackend.exception.BusinessException;
import com.cpi.cpicturebackend.exception.ErrorCode;
import com.cpi.cpicturebackend.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    //测试文件上传
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file")MultipartFile multipartFile) {
        //指定文件目录和存储位置
        String filename=multipartFile.getOriginalFilename();
        String filepath=String.format("/test/%s",filename);

        File file=null;
        try {
            //上传文件
           file= File.createTempFile(filename,null);
           multipartFile.transferTo(file);
           cosManager.putObject(filepath,file);
           //返回可以访问的地址
            return ResultUtils.success(filepath);
        } catch (IOException e) {
            log.error("上传失败"+filepath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }finally {
            if (file != null) {
                //删除临时文件
               boolean delete = file.delete();
               if (!delete) {
                   log.error("删除临时文件失败");
               }
            }
        }

    }

    /**
     * 测试文件下载
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            //获取cos对象
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }


}
