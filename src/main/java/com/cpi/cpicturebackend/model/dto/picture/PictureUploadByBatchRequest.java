package com.cpi.cpicturebackend.model.dto.picture;

import lombok.Data;


/**
 * 图片批量导入请求
 *
 * @author
 */
@Data
public class PictureUploadByBatchRequest {  
  
    /**  
     * 搜索词  
     */  
    private String searchText;  
  
    /**  
     * 抓取数量  
     */  
    private Integer count = 10;

    /**
     * 名称前缀
     */
    private String namePrefix;

    private static final long serialVersionUID = 1L;
}
