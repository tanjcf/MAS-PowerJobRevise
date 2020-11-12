package com.github.kfcfans.powerjob.server.persistence.core.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * 用于保存上传文件信息
 *
 * @author tjq
 * @since 2020/5/15
 */
@Data
@Entity
@Table(name = "file_info")
public class FileInfoDO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    // 所属的应用ID
    private Long appId;
    // 上一次部署时间
    private Date lastDeployTime;
    // 上一次部署时间
    private Date gmtCreate;
    private Date gmtModified;

    private String serverPathFile;

    private String workPathFile;
    private String ipAddress;
}
