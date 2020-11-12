package com.github.kfcfans.powerjob.server.service;

import com.github.kfcfans.powerjob.common.utils.CommonUtils;
import com.github.kfcfans.powerjob.server.common.utils.OmsFileUtils;
import com.github.kfcfans.powerjob.server.persistence.mongodb.GridFsManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * 文件上传服务
 *
 * @author tjq
 * @since 2020/5/16
 */
@Slf4j
@Service
public class FileContainerService {
    @Resource
    private GridFsManager gridFsManager;
    /**
     * 上传用于部署的容器的 Jar 文件
     * @param file 接受的文件
     * @return 该文件的 md5 值
     * @throws IOException 异常
     */
    public String uploadContainerJarFile(MultipartFile file) throws IOException {
        String workerDirStr = OmsFileUtils.genTemporaryWorkPath();
        String tmpFileStr = workerDirStr + file.getOriginalFilename();
        File workerDir = new File(workerDirStr);
        File tmpFile = new File(tmpFileStr);

        try {
            // 下载到本地
            FileUtils.forceMkdirParent(tmpFile);
            file.transferTo(tmpFile);

            // 生成MD5，这兄弟耗时有点小严重
            String md5 = OmsFileUtils.md5(tmpFile);
            String fileName =String.format("%s-%s",md5,file.getOriginalFilename());

            // 上传到 mongoDB，这兄弟耗时也有点小严重，导致这个接口整体比较慢...不过也没必要开线程去处理
//         /   gridFsManager.store(tmpFile, GridFsManager.CONTAINER_BUCKET, fileName);

            // 将文件拷贝到正确的路径
            String finalFileStr = OmsFileUtils.genContainerJarPath() + fileName;
            File finalFile = new File(finalFileStr);
            if (finalFile.exists()) {
                FileUtils.forceDelete(finalFile);
            }
            FileUtils.moveFile(tmpFile, finalFile);
            return finalFile.toString();

        }finally {
            CommonUtils.executeIgnoreException(() -> FileUtils.forceDelete(workerDir));
        }
    }
    private static String genContainerJarName(String version) {
        return String.format("oms-container-%s.jar", version);
    }
}
