package com.github.kfcfans.powerjob.server.persistence.core.repository;

import com.github.kfcfans.powerjob.server.persistence.core.model.RefDatasetLineageDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 用与访问ref_dataset_lineage
 * @author jctan
 * @since 2020/09/21
 */
public interface RefDatasetLineageRepository extends JpaRepository<RefDatasetLineageDO, Long> {
    List<RefDatasetLineageDO> findByAnotherNameLike(String anotherName);

    List<RefDatasetLineageDO> findByJobInfoIdIn(List<Long> JOBInfoId);

}
