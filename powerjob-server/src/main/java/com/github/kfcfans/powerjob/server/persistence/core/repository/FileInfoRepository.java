package com.github.kfcfans.powerjob.server.persistence.core.repository;


import com.github.kfcfans.powerjob.server.persistence.core.model.FileInfoDO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;

public interface FileInfoRepository extends JpaRepository<FileInfoDO, Long> {
    Page<FileInfoDO> findAllBy( Pageable pageable);
    Page<FileInfoDO> findAllByAppId(Long Appid, Pageable pageable);

}
