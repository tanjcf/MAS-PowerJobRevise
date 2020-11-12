package com.github.kfcfans.powerjob.server.service;

import com.github.kfcfans.powerjob.common.ExecuteType;
import com.github.kfcfans.powerjob.common.ProcessorType;
import com.github.kfcfans.powerjob.common.TimeExpressionType;
import com.github.kfcfans.powerjob.common.model.PEWorkflowDAG;
import com.github.kfcfans.powerjob.common.request.http.SaveSQLInfoRequest;
import com.github.kfcfans.powerjob.common.request.http.SaveWorkflowRequest;
import com.github.kfcfans.powerjob.common.response.ResultDTO;
import com.github.kfcfans.powerjob.server.common.SJ;
import com.github.kfcfans.powerjob.server.common.constans.SwitchableStatus;
import com.github.kfcfans.powerjob.server.common.utils.CronExpression;
import com.github.kfcfans.powerjob.server.common.utils.PowerSqlFileToString;
import com.github.kfcfans.powerjob.server.common.utils.SQLLineageUtils;
import com.github.kfcfans.powerjob.server.persistence.core.model.*;
import com.github.kfcfans.powerjob.server.persistence.core.repository.JobInfoRepository;
import com.github.kfcfans.powerjob.server.persistence.core.repository.RefFieldLineageRepository;
import com.github.kfcfans.powerjob.server.persistence.core.repository.WorkflowInfoRepository;
import com.github.kfcfans.powerjob.server.service.workflow.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
public class SqlFieldService {
    private String sql;
    @Resource
    private RefFileldLineageService refFileldLineageService;
    @Resource
    private RefDatasetLineageService refDatasetLineageService;
    @Resource
    private RefFieldLineageRepository refFieldLineageRepository;
    @Resource
    private JobInfoRepository jobInfoRepository;
    private PowerSqlFileToString psToString = new PowerSqlFileToString();
    @Resource
    private WorkflowInfoRepository workflowInfoRepository;
    @Resource
    private WorkflowService workflowService;

    /**
     * 根据sql 创建job
     *
     * @param query
     * @return
     */

    public Boolean setCreateTableUtils(final String query,SaveSQLInfoRequest response) {
        // final String id, final String shellText, final String workflowName, final Long appid, final ProcessorType proType, final ExecuteType executeType, final String timeExpression, final TimeExpressionType timeExpressionType, boolean enable

        SQLLineageUtils lep = new SQLLineageUtils();
        String uid = "" + System.currentTimeMillis();
        String tableName;
        String lineField;
        RefFieldLineageDO refFieldLineageDO = new RefFieldLineageDO();
        JobInfoDO jobInfoDO = new JobInfoDO();
        try {
            // 减一去掉分号（;） 进行sql分析
            lep.getLineageInfo(query.substring(0, query.length() - 1));

            tableName = psToString.toString(lep.getOutputTableList());
            if (lep.getOutputTableList().size() == 0) tableName = psToString.toString(lep.getInputTableList());
            lineField = psToString.toString(lep.getTableLineList());

            refFieldLineageDO.setTableName(tableName);
            refFieldLineageDO.setFileName(lineField);
            //

            refFieldLineageDO.setId(0L);
            List<RefFieldLineageDO> prfl = refFieldLineageRepository.findByTableNameLike(tableName);
            TreeSet<String> tempInputTableList = lep.getInputTableList();
            if (lep.getWithTableList().size() > 0) {
                List<String> WithTablelist = new ArrayList<String>(lep.getWithTableList());
                tempInputTableList.removeAll(WithTablelist);
            }
            BeanUtils.copyProperties(response, jobInfoDO);
            jobInfoDO.setJobName(psToString.toString(lep.getOutputTableList()));
            jobInfoDO.setGmtModified(new Date());
            jobInfoDO.setProcessorType(response.getProcessorType().getV());
            jobInfoDO.setExecuteType(response.getExecuteType().getV());
            jobInfoDO.setStatus(response.isEnable() ? SwitchableStatus.ENABLE.getV() : SwitchableStatus.DISABLE.getV());
            jobInfoDO.setTimeExpressionType(response.getTimeExpressionType().getV());
            jobInfoDO.setInputTables(psToString.toString(tempInputTableList));
            jobInfoDO.setWfName(response.getWorkflowName());
            jobInfoDO.setProcessorInfo(response.getParamHeader() + query + "\"");
            jobInfoDO.setRefTableUuid(uid);
            refreshJob(jobInfoDO);
            // 转化报警用户列表
            if (!CollectionUtils.isEmpty(response.getNotifyUserIds())) {
                jobInfoDO.setNotifyUserIds(SJ.commaJoiner.join(response.getNotifyUserIds()));
            }
            if (jobInfoDO.getId() == null) {
                jobInfoDO.setGmtCreate(new Date());
            }
            jobInfoRepository.saveAndFlush(jobInfoDO);
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
        return true;

    }

    /**
     * 插入数据血缘关系（ref_dataset_lineage）
     *
     * @param
     * @return
     */
    public Boolean setTableUtils(final String id, final SQLLineageUtils lep) {
        if (lep.asMap.size() > 0) {
            for (String t : lep.asMap.keySet()) {
                RefDatasetLineageDO refDatasetLineageDO = new RefDatasetLineageDO();
                //表名
                String str = psToString.toString(lep.asMap.get(t).first);
                //字段
                String str1 = psToString.toString(lep.asMap.get(t).second);
                //条件字段
                String str2 = psToString.toString(lep.asMap.get(t).third);
                //表条件
                String str3 = psToString.toString(lep.asMap.get(t).four);

                System.out.println(id + "_" + t + " as " + str + "," + str1 + "," + str2 + "," + str3);
                refDatasetLineageDO.setJobInfoId(0L);
                refDatasetLineageDO.setAnotherName(t);
                refDatasetLineageDO.setTableName(str);
                refDatasetLineageDO.setLineField(str1);
                refDatasetLineageDO.setLineWhereField(str2);
                refDatasetLineageDO.setLineWhereField(str3);
                if (!refDatasetLineageService.save(refDatasetLineageDO)) return false;
            }
        }   //插入到表


        return true;
    }

    /**
     * 自动生成job
     *
     * @param queryList
     * @return
     */
    public Boolean setSqlConext(final List<String> queryList,final SaveSQLInfoRequest response) {
        for (String query : queryList) {
            if (!(setCreateTableUtils(query,response))) {

                return false;
            }
        }
        return true;
    }

    /**
     * 自动生成任务流程
     *
     * @param wfName
     * @return
     */
    public ResultDTO<Long> setWorkflowInfo(final Long appid,final String wfName, final String timeExpression, final TimeExpressionType timeExpressionType,final List<Long> notifyUserIds) throws Exception {
        PEWorkflowDAG DAG = new PEWorkflowDAG();
        List<PEWorkflowDAG.Node> nodeList = new ArrayList<>();
        List<PEWorkflowDAG.Edge> edgeList = new ArrayList<>();
        PEWorkflowDAG.Edge edge;
        List<JobInfoDO> jobInfos = jobInfoRepository.findByWfNameLike(wfName);
        Map<String, ArrayList<Long>> jobtoke = jobToke(jobInfos);

        for (JobInfoDO j : jobInfos) {
            // NODE
            PEWorkflowDAG.Node node = new PEWorkflowDAG.Node();
            node.setJobId(j.getId());
            node.setJobName(j.getJobName());
            nodeList.add(node);

            // EDGES

                if (!Objects.equals(j.getInputTables(),null)) {

                    String[] str = j.getInputTables().split(",");

                    for (String s : str) {

                        if (jobtoke.containsKey(s)) {
                            // 提取所有的加入DAG
                            for (Long l: jobtoke.get(s)) {
                                // 剔除job1->job1
                                if ( l != j.getId()){

                                    edge = new PEWorkflowDAG.Edge();
                                    edge.setTo(j.getId());
                                    edge.setFrom(l);
                                    edgeList.add(edge);
                                }

                            }

                        }
                    }

                }


        }
        // 检查是否存在环路存
        edgeList = getRodeEdges(edgeList);

        DAG.setNodes(nodeList);
        DAG.setEdges(edgeList);
        // 导入到表
        SaveWorkflowRequest request = new SaveWorkflowRequest();
        request.setPEWorkflowDAG(DAG);
        request.setWfName(wfName);
        request.setTimeExpression(timeExpression);
        request.setTimeExpressionType(timeExpressionType);
        request.setAppId(appid);
        request.setNotifyUserIds(notifyUserIds);
        return ResultDTO.success(workflowService.saveWorkflow(request));
    }

    /**
     * @param jobInfos
     * @return map<表名, 对用job id>
     */
    public Map<String, ArrayList<Long>> jobToke(final List<JobInfoDO> jobInfos) {
        Map<String,  ArrayList<Long>> jobtoke = new HashMap<>();

        for (JobInfoDO j : jobInfos) {
            String[] str = j.getJobName().split(",");
            if (str.length < 2) {
                // 判断表是否存在，存在往原有的 jobId集合里面插入
                if (jobtoke.containsKey(j.getJobName())){
                    jobtoke.get(j.getJobName()).add(j.getId());
                }else {

                    jobtoke.put(j.getJobName(),new ArrayList<Long>(Collections.singleton(j.getId())));
                }
            } else {
                for (String s : str) {
                    // 判断表是否存在，存在往原有的 jobId集合里面插入
                    if (jobtoke.containsKey(s)){
                        jobtoke.get(s).add(j.getId());
                    }else {

                        jobtoke.put(s,new ArrayList<>(Collections.singleton(j.getId())));
                    }
                }
            }
        }
        return jobtoke;
    }

    /**
     * 去除环路
     *
     * @param edges
     * @return
     */
    public List<PEWorkflowDAG.Edge> getRodeEdges(final List<PEWorkflowDAG.Edge> edges) {

        int sumRemove = 0;
        List<PEWorkflowDAG.Edge> edgeList = edges;
        PEWorkflowDAG.Edge edge = new PEWorkflowDAG.Edge();

        for (int i = 0; i < edgeList.size(); i++) {
            if (i >= edgeList.size() - 1) break;
            edge = edgeList.get(i);
            Long l1 = edge.getFrom();
            Long l2 = edge.getTo();
            for (int j = i; j < edgeList.size(); j++) {
                edge = edgeList.get(j);
                Long l3 = edge.getFrom();
                Long l4 = edge.getTo();
                if (((l1 == l4) && (l2 == l3))) {
                    sumRemove ++;
                    edgeList.remove(j);
                    if (j >= edgeList.size() - 1) break;

                }
            }

        }
        // 判断是否清洗干净，清洗赶紧返回
        if (sumRemove<1){
            return edgeList;
        }
        return getRodeEdges(edgeList);
    }

    private void refreshJob(JobInfoDO jobInfoDO) throws Exception {
        // 计算下次调度时间
        Date now = new Date();
        TimeExpressionType timeExpressionType = TimeExpressionType.of(jobInfoDO.getTimeExpressionType());

        if (timeExpressionType == TimeExpressionType.CRON) {
            CronExpression cronExpression = new CronExpression(jobInfoDO.getTimeExpression());
            Date nextValidTime = cronExpression.getNextValidTimeAfter(now);
            jobInfoDO.setNextTriggerTime(nextValidTime.getTime());
        }else if (timeExpressionType == TimeExpressionType.API || timeExpressionType == TimeExpressionType.WORKFLOW) {
            jobInfoDO.setTimeExpression(null);
        }
        // 重写最后修改时间
        jobInfoDO.setGmtModified(now);
    }

}
