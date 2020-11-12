package com.github.kfcfans.powerjob.server.web.response;

import com.github.kfcfans.powerjob.common.ExecuteType;
import com.github.kfcfans.powerjob.common.OmsConstant;
import com.github.kfcfans.powerjob.common.ProcessorType;
import com.github.kfcfans.powerjob.common.TimeExpressionType;
import com.github.kfcfans.powerjob.common.utils.CommonUtils;
import com.github.kfcfans.powerjob.server.common.SJ;
import com.github.kfcfans.powerjob.server.common.constans.SwitchableStatus;
import com.github.kfcfans.powerjob.server.persistence.core.model.FileInfoDO;
import com.github.kfcfans.powerjob.server.persistence.core.model.JobInfoDO;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * FIleInfo 对外展示对象
 *
 * @author jctan
 * @since 2020/4/12
 */
@Data
public class FileInfoVO {

    private Long id;

    /* ************************** 任务基本信息 ************************** */
    // 任务所属的应用ID
    private Long appId;
    private String gmtCreate;
    private String gmtModified;
    private Date lastDeployFile;
    private String workPathFile;
    private String ipAddress;


    public static FileInfoVO from(FileInfoDO fileInfoDO) {
        FileInfoVO vo = new FileInfoVO();
        BeanUtils.copyProperties(fileInfoDO, vo);
        vo.setGmtCreate(DateFormatUtils.format(fileInfoDO.getGmtCreate(), OmsConstant.TIME_PATTERN));
        vo.setGmtModified(DateFormatUtils.format(fileInfoDO.getGmtModified(), OmsConstant.TIME_PATTERN));
        return vo;
    }
}
