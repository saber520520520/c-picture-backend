package com.cpi.cpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cpi.cpicturebackend.model.dto.picture.PictureQueryRequest;
import com.cpi.cpicturebackend.model.dto.picture.PictureUploadRequest;
import com.cpi.cpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cpi.cpicturebackend.model.entity.User;
import com.cpi.cpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author 86198
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-06-26 14:38:10
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


    /**
     * 获取查询对象
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片封装类(单条)
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片分页封装类
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);


    /**
     * 校验图片
     * @param picture
     */
    void validPicture(Picture picture);
}
