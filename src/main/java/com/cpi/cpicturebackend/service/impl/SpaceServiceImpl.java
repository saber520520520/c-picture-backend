package com.cpi.cpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cpi.cpicturebackend.exception.BusinessException;
import com.cpi.cpicturebackend.exception.ErrorCode;
import com.cpi.cpicturebackend.exception.ThrowUtils;
import com.cpi.cpicturebackend.model.dto.space.SpaceAddRequest;
import com.cpi.cpicturebackend.model.dto.space.SpaceQueryRequest;
import com.cpi.cpicturebackend.model.entity.Picture;
import com.cpi.cpicturebackend.model.entity.Space;
import com.cpi.cpicturebackend.model.entity.User;
import com.cpi.cpicturebackend.model.enums.SpaceLevelEnum;
import com.cpi.cpicturebackend.model.vo.PictureVO;
import com.cpi.cpicturebackend.model.vo.SpaceVO;
import com.cpi.cpicturebackend.model.vo.UserVO;
import com.cpi.cpicturebackend.service.SpaceService;
import com.cpi.cpicturebackend.mapper.SpaceMapper;
import com.cpi.cpicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 86198
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-07-10 08:49:02
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{


    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id=spaceQueryRequest.getId();
        Long userId=spaceQueryRequest.getUserId();
        String spacename=spaceQueryRequest.getSpaceName();
        Integer spaceLevel=spaceQueryRequest.getSpaceLevel();
        String sortField=spaceQueryRequest.getSortField();
        String sortOrder=spaceQueryRequest.getSortOrder();

        //拼接查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spacename), "spacename", spacename);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        // 要创建
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
        }
        // 修改数据时，如果要改空间级别
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        //1.填充参数默认值
        //转换实体类和DTO
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        if (StrUtil.isBlank(space.getSpaceName()))
        {
            space.setSpaceName("未命名空间");
        }
        if (space.getSpaceLevel() == null)
        {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        //填充容量和大小
        this.fillSpaceBySpaceLevel( space);
        //2.校验参数
        this.validSpace(space,true);
        //3.校验权限，非管理员只能创建普通空间
        Long userId = loginUser.getId();
        space.setUserId(userId);
      if (SpaceLevelEnum.COMMON.getValue()!=space.getSpaceLevel()&&!userService.isAdmin(loginUser))
    {
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权创建指定空间");
    }
        //4.控制同一用户只能创建一个私有空间
        String lock=String.valueOf(userId).intern();
      synchronized (lock)
      {
          //封装事务
        Long newSpaceId =  transactionTemplate.execute(status -> {
              //判断是否有空间
              boolean exists = this.lambdaQuery()
                      .eq(Space::getUserId, userId)
                      .exists();
              //如果有空间就不创建
              ThrowUtils.throwIf(exists,ErrorCode.OPERATION_ERROR,"用户已创建空间");
              //创建
              boolean save = this.save(space);
              ThrowUtils.throwIf(!save,ErrorCode.OPERATION_ERROR,"创建空间失败");
              //返回id
              return space.getId();
          });
          return Optional.ofNullable(newSpaceId).orElse(-1L);
      }
    }


}




