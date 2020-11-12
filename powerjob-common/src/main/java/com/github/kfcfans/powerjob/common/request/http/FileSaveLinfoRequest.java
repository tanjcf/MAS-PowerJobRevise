package com.github.kfcfans.powerjob.common.request.http;

import lombok.Data;

import java.util.List;

/**
 *
 * @author jctan
 * @since 2020/9/29
 *
 */
@Data
public class FileSaveLinfoRequest {

    // 任务ID（jobId），null -> 插入，否则为更新
    private Long id;
    // 所属应用ID（OpenClient不需要用户填写，自动填充）
    private Long appId;
    // 文件上传到服务器目录
    private List<String> serverPathFile;
    // worke ip 集合
    private List<String> workeAddres;

}
