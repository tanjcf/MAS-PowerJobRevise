package com.github.kfcfans.powerjob.common.request.http;

import com.github.kfcfans.powerjob.common.ExecuteType;
import com.github.kfcfans.powerjob.common.ProcessorType;
import com.github.kfcfans.powerjob.common.TimeExpressionType;
import com.github.kfcfans.powerjob.common.model.PEWorkflowDAG;
import com.github.kfcfans.powerjob.common.utils.CommonUtils;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 *
 * @author jctan
 * @since 2020/9/29
 *
 */
@Data
public class SaveSQLInfoRequest {
    //sql语句
    private String  paramBody;
    // 所属应用ID（OpenClient不需要用户填写，自动填充）
    private Long appId;
    //配置参数
    private String paramHeader;
    //上传方式
    private String uploadType;
    //任务流名称
    private String workflowName;
    //工作流程描述
    private String workDescription;
    // 点线表示法
    private PEWorkflowDAG pEWorkflowDAG;
    /* ************************** 运行时配置 ************************** */
    // 最大同时运行任务数，默认 1
    private Integer maxInstanceNum;
    // 并发度，同时执行某个任务的最大线程数量
    private Integer concurrency;
    // 任务整体超时时间
    private Long instanceTimeLimit;
    /* ************************** 重试配置 ************************** */
    private Integer instanceRetryNum;
    private Integer taskRetryNum;
    /* ************************** 繁忙机器配置 ************************** */
    // 最低CPU核心数量，0代表不限
    private double minCpuCores;
    // 最低内存空间，单位 GB，0代表不限
    private double minMemorySpace;
    // 最低磁盘空间，单位 GB，0代表不限
    private double minDiskSpace;
    /* ************************** 定时参数 ************************** */
    // 时间表达式类型，仅支持 CRON 和 API
    private TimeExpressionType timeExpressionType;
    // 时间表达式，CRON/NULL/LONG/LONG
    private String timeExpression;

    // 最大同时运行的工作流个数，默认 1
    private Integer maxWfInstanceNum = 1;

    // ENABLE / DISABLE
    private boolean enable = true;
    /* ************************** 集群配置 ************************** */
    // 指定机器运行，空代表不限，非空则只会使用其中的机器运行（多值逗号分割）
    private String designatedWorkers;
    // 最大机器数量
    private Integer maxWorkerCount;
    // 工作流整体失败的报警
    private List<Long> notifyUserIds = Lists.newLinkedList();
    // 执行类型，单机/广播/MR
    private ExecuteType executeType;
    // 执行器类型，Java/Shell
    private ProcessorType processorType;


    public void valid() {
        CommonUtils.requireNonNull(paramBody, "paramBody can't be empty");
        CommonUtils.requireNonNull(paramHeader, "paramHeader can't be empty");
        CommonUtils.requireNonNull(uploadType, "uploadType can't be empty");

    }
}
