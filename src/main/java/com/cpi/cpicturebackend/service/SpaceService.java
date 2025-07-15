package com.cpi.cpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cpi.cpicturebackend.model.dto.space.SpaceAddRequest;
import com.cpi.cpicturebackend.model.dto.space.SpaceQueryRequest;
import com.cpi.cpicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cpi.cpicturebackend.model.entity.User;
import com.cpi.cpicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 86198
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-07-10 08:49:02
*/
public interface SpaceService extends IService<Space> {

    /**
     * 获取查询对象
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取空间封装类(单条)
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间分页封装类
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);


    /**
     * 校验空间
     * @param space
     * @param add 表示是否为创建时校验
     */
    void validSpace(Space space,boolean add);

    /**
     * 根据空间级别填充空间信息
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 创建空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);
}
