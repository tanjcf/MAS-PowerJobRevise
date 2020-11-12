package com.github.kfcfans.powerjob.server.service;

import com.github.kfcfans.powerjob.server.persistence.core.model.RefDatasetLineageDO;
import com.github.kfcfans.powerjob.server.persistence.core.repository.RefDatasetLineageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class RefDatasetLineageService {
    @Resource
    private RefDatasetLineageRepository refDatasetLineage;

    /**
     * 保存/修改 数据级别血缘关系
     * @param request 请求
     */
    public Boolean save(RefDatasetLineageDO request) {
        try {
            System.out.println(request.toString());
            refDatasetLineage.saveAndFlush(request);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }


    }
