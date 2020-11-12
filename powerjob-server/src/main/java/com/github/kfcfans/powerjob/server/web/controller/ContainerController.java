package com.github.kfcfans.powerjob.server.web.controller;

import com.github.kfcfans.powerjob.common.ExecuteType;
import com.github.kfcfans.powerjob.common.OmsConstant;
import com.github.kfcfans.powerjob.common.ProcessorType;
import com.github.kfcfans.powerjob.common.TimeExpressionType;
import com.github.kfcfans.powerjob.common.request.http.FileSaveLinfoRequest;
import com.github.kfcfans.powerjob.common.response.ResultDTO;
import com.github.kfcfans.powerjob.server.akka.OhMyServer;
import com.github.kfcfans.powerjob.server.akka.actors.FileTransferClient;
import com.github.kfcfans.powerjob.server.common.constans.ContainerSourceType;
import com.github.kfcfans.powerjob.server.common.constans.SwitchableStatus;
import com.github.kfcfans.powerjob.server.common.utils.ContainerTemplateGenerator;
import com.github.kfcfans.powerjob.server.common.utils.OmsFileUtils;
import com.github.kfcfans.powerjob.server.common.utils.SqlConextUtils;
import com.github.kfcfans.powerjob.server.persistence.PageResult;
import com.github.kfcfans.powerjob.server.persistence.core.model.*;
import com.github.kfcfans.powerjob.server.persistence.core.repository.AppInfoRepository;
import com.github.kfcfans.powerjob.server.persistence.core.repository.ContainerInfoRepository;
import com.github.kfcfans.powerjob.server.persistence.core.repository.FileInfoRepository;
import com.github.kfcfans.powerjob.server.service.AppInfoService;
import com.github.kfcfans.powerjob.server.service.ContainerService;
import com.github.kfcfans.powerjob.server.service.FileContainerService;
import com.github.kfcfans.powerjob.server.service.SqlFieldService;
import com.github.kfcfans.powerjob.server.web.request.*;
import com.github.kfcfans.powerjob.server.web.response.ContainerInfoVO;
import com.github.kfcfans.powerjob.server.web.response.FileInfoVO;
import com.github.kfcfans.powerjob.server.web.response.InstanceInfoVO;
import com.github.kfcfans.powerjob.server.web.response.WorkflowInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.github.kfcfans.powerjob.common.request.http.SaveSQLInfoRequest;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 容器信息控制层
 *
 * @author tjq
 * @since 2020/5/15
 */
@Slf4j
@RestController
@RequestMapping("/container")
public class ContainerController {

    @Value("${server.port}")
    private int port;
    @Resource
    private AppInfoService appInfoService;
    @Resource
    private ContainerService containerService;
    @Resource
    private AppInfoRepository appInfoRepository;
    @Resource
    private ContainerInfoRepository containerInfoRepository;
    @Resource
    private FileInfoRepository fileInfoRepository;
    @Resource
    private SqlFieldService sqlFieldService;
    @Resource
    private FileContainerService fileContainerService;

    private static ContainerInfoVO convert(ContainerInfoDO containerInfoDO) {
        ContainerInfoVO vo = new ContainerInfoVO();
        BeanUtils.copyProperties(containerInfoDO, vo);
        if (containerInfoDO.getLastDeployTime() == null) {
            vo.setLastDeployTime("N/A");
        }else {
            vo.setLastDeployTime(DateFormatUtils.format(containerInfoDO.getLastDeployTime(), OmsConstant.TIME_PATTERN));
        }
        SwitchableStatus status = SwitchableStatus.of(containerInfoDO.getStatus());
        vo.setStatus(status.name());
        ContainerSourceType sourceType = ContainerSourceType.of(containerInfoDO.getSourceType());
        vo.setSourceType(sourceType.name());
        return vo;
    }

    @GetMapping("/downloadJar")
    public void downloadJar(String version, HttpServletResponse response) throws IOException {
        File file = containerService.fetchContainerJarFile(version);
        if (file.exists()) {
            OmsFileUtils.file2HttpResponse(file, response);
        }
    }


    @PostMapping("/downloadContainerTemplate")
    public void downloadContainerTemplate(@RequestBody GenerateContainerTemplateRequest req, HttpServletResponse response) throws IOException {
        File zipFile = ContainerTemplateGenerator.generate(req.getGroup(), req.getArtifact(), req.getName(), req.getPackageName(), req.getJavaVersion());
        OmsFileUtils.file2HttpResponse(zipFile, response);
    }

    @PostMapping("/jarUpload")
    public ResultDTO<String> fileUpload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResultDTO.failed("empty file");
        }
        return ResultDTO.success(containerService.uploadContainerJarFile(file));
      //  return ResultDTO.success(fileContainerService.uploadContainerJarFile(file));
    }
    @PostMapping("/fileUpuloadScript")
    public ResultDTO<String> Upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResultDTO.failed("empty file");
        }
        return ResultDTO.success(fileContainerService.uploadContainerJarFile(file));
    }
    @PostMapping("/fileSave")
    public ResultDTO<String> fileSaveContainer(@RequestBody FileSaveLinfoRequest request){


         containerService.fileSave(request);
        return ResultDTO.success("上传成功","200");
    }
    @PostMapping("/sqlUploadScript")
    public ResultDTO<String> sqlUpload(@RequestBody SaveSQLInfoRequest response) throws Exception{

        if (response.getParamBody().equals("")||response.getParamHeader().equals("")){
            return ResultDTO.success("ParamBody and ParamHeader  connot null","400");
        }
        SqlConextUtils sqlUtils = new SqlConextUtils();
        List<String> strList =sqlUtils.sqlConextStr(response.getParamBody());

        if (!sqlFieldService.setSqlConext(strList,response)){
           return ResultDTO.success("上传失败检查下语句","500");
       }
     ResultDTO<Long> resSqlFile =  sqlFieldService.setWorkflowInfo(response.getAppId(),response.getWorkflowName(),response.getTimeExpression(), response.getTimeExpressionType(),response.getNotifyUserIds());

        return ResultDTO.success(resSqlFile.getMessage(),resSqlFile.getCode());
    }

    @PostMapping("/sqlUploadFile")
    public ResultDTO<String> upload(
            @RequestParam("paramBody")MultipartFile file, @RequestParam("paramHeader") String header, @RequestParam("workflowName") String workflowName,
            @RequestParam("appId") Long appId, @RequestParam("timeExpressionType") TimeExpressionType timeExpressionType, @RequestParam("timeExpression") String timeExpression,
            @RequestParam("executeType")ExecuteType executeType,  @RequestParam("processorType") ProcessorType processorType,@RequestParam("maxInstanceNum")Integer maxInstanceNum,
            @RequestParam("concurrency") Integer concurrency,@RequestParam("instanceTimeLimit")Long instanceTimeLimit,@RequestParam("instanceRetryNum")Integer instanceRetryNum,
            @RequestParam("taskRetryNum")Integer taskRetryNum,@RequestParam("minCpuCores")Long minCpuCores,@RequestParam("minMemorySpace")Long minMemorySpace,@RequestParam("minDiskSpace")Long minDiskSpace,@RequestParam("enable")boolean enable,
            @RequestParam("designatedWorkers")String designatedWorkers,@RequestParam("maxWorkerCount")Integer maxWorkerCount,@RequestParam("notifyUserIds")List<Long> notifyUserIds) {
                SaveSQLInfoRequest res = new SaveSQLInfoRequest();

        if(file.isEmpty()){
            return  ResultDTO.success("请选择文件","500");
        }
        try {

            byte[] bytes = file.getBytes();
            String completeData = new String(bytes);
            SqlConextUtils sqlUtils = new SqlConextUtils();
            res.setParamBody(completeData.replace("\r",""));
            res.setParamHeader(header);
            res.setAppId(appId);
            res.setWorkflowName(workflowName);
            res.setTimeExpressionType(timeExpressionType);
            res.setTimeExpression(timeExpression);
            res.setExecuteType(executeType);
            res.setProcessorType(processorType);
            res.setMaxInstanceNum(maxInstanceNum);
            res.setConcurrency(concurrency);
            res.setInstanceTimeLimit(instanceTimeLimit);
            res.setInstanceRetryNum(instanceRetryNum);
            res.setTaskRetryNum(taskRetryNum);
            res.setMinCpuCores(minCpuCores);
            res.setMinDiskSpace(minMemorySpace);
            res.setMinDiskSpace(minDiskSpace);
            res.setEnable(enable);
            res.setDesignatedWorkers(designatedWorkers);
            res.setMaxWorkerCount(maxWorkerCount);
            res.setNotifyUserIds(notifyUserIds);


           this.sqlUpload(res);
            return ResultDTO.success("上传成功","200");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResultDTO.success("上传失败","400");
    }

    @PostMapping("/save")
    public ResultDTO<Void> saveContainer(@RequestBody SaveContainerInfoRequest request) {
        containerService.save(request);
        return ResultDTO.success(null);
    }

    @GetMapping("/delete")
    public ResultDTO<Void> deleteContainer(Long appId, Long containerId) {
        containerService.delete(appId, containerId);
        return ResultDTO.success(null);
    }

    @GetMapping("/list")
    public ResultDTO<List<ContainerInfoVO>> listContainers(Long appId) {
        List<ContainerInfoVO> res = containerInfoRepository.findByAppIdAndStatusNot(appId, SwitchableStatus.DELETED.getV())
                .stream().map(ContainerController::convert).collect(Collectors.toList());
        return ResultDTO.success(res);
    }
    @PostMapping("/fileList")
    public ResultDTO<PageResult<FileInfoVO>> fileList(@RequestBody QueryFileInfoRequest req) {
        Sort sort = Sort.by(Sort.Direction.DESC, "gmtCreate");
        PageRequest pageRequest = PageRequest.of(req.getIndex(), req.getPageSize(), sort);
        Page<FileInfoDO> fiPage;
        if (req.isWorkAll()){
            fiPage = fileInfoRepository.findAllBy(pageRequest);
        }else {
            fiPage = fileInfoRepository.findAllByAppId(req.getAppId(),  pageRequest);
        }
        return ResultDTO.success(convertPage(fiPage),"上传成功","200");
    }
    @GetMapping("/listDeployedWorker")
    public ResultDTO<String> listDeployedWorker(Long appId, Long containerId, HttpServletResponse response) {
        AppInfoDO appInfoDO = appInfoRepository.findById(appId).orElseThrow(() -> new IllegalArgumentException("can't find app by id:" + appId));
        String targetServer = appInfoDO.getCurrentServer();

        if (StringUtils.isEmpty(targetServer)) {
            return ResultDTO.failed("No workers have even registered！");
        }

        // 转发 HTTP 请求
        if (!OhMyServer.getActorSystemAddress().equals(targetServer)) {
            String targetIp = targetServer.split(":")[0];
            String url = String.format("http://%s:%d/container/listDeployedWorker?appId=%d&containerId=%d", targetIp, port, appId, containerId);
            try {
                response.sendRedirect(url);
                return ResultDTO.success(null);
            }catch (Exception e) {
                return ResultDTO.failed(e);
            }
        }
        return ResultDTO.success(containerService.fetchDeployedInfo(appId, containerId));
    }

    /**
     * 构造页码
     * @param FileInfoPage
     * @return
     */
    private static PageResult<FileInfoVO> convertPage(Page<FileInfoDO> FileInfoPage) {
        List<FileInfoVO> FileInfoPageVOList = FileInfoPage.getContent().stream().map(FileInfoVO::from).collect(Collectors.toList());
        PageResult<FileInfoVO> newPage = new PageResult<>(FileInfoPage);
        newPage.setData(FileInfoPageVOList);
      return newPage;
    }
}
