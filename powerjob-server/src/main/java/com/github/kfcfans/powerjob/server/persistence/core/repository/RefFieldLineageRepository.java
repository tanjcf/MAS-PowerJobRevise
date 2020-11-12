package com.github.kfcfans.powerjob.server.persistence.core.repository;

import com.github.kfcfans.powerjob.server.persistence.core.model.RefFieldLineageDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefFieldLineageRepository  extends JpaRepository<RefFieldLineageDO, Long> {
    List<RefFieldLineageDO> findByTableNameLike(String tableName);

    List<RefFieldLineageDO> findByFileNameIn(List<Long> fileName);

}
