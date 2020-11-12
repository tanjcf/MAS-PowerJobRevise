package com.github.kfcfans.powerjob.server.service;

import com.github.kfcfans.powerjob.server.persistence.core.model.RefFieldLineageDO;
import com.github.kfcfans.powerjob.server.persistence.core.repository.RefFieldLineageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Slf4j
@Service
public class RefFileldLineageService {
    @Resource
    private RefFieldLineageRepository RefFieldLineage;

    /**
     * 保存/修改 数据级别血缘关系
     * @param request 请求
     */
    public void save(RefFieldLineageDO request) {
        try {
            RefFieldLineage.saveAndFlush(request);
        }catch (Exception e){
            e.printStackTrace();
          //  return false;
        }
     //   return true;
    }
}
