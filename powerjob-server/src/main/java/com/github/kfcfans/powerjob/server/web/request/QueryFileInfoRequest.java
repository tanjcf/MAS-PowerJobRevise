package com.github.kfcfans.powerjob.server.web.request;

import lombok.Data;

@Data
public class QueryFileInfoRequest {
    // 所属应用ID
    private Long appId;
    // 当前页码
    private Integer index;
    // 页大小
    private Integer pageSize;
    // 查询全部任务实例或用户所有实例
    private boolean workAll;
}
