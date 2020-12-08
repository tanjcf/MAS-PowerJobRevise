package com.github.kfcfans.powerjob.server.persistence.core.repository;

import com.github.kfcfans.powerjob.server.persistence.core.model.WorkflowInstanceInfoDO;
import com.github.kfcfans.powerjob.server.persistence.core.model.WorkflowInstanceStuatusInfoDo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Optional;

public interface WorkflowInstanceStuatusRepository extends JpaRepository<WorkflowInstanceStuatusInfoDo, Long> {

    Optional<WorkflowInstanceStuatusInfoDo> findByWfInstanceId(Long wfInstanceId);

    // 删除历史数据，JPA自带的删除居然是根据ID循环删，2000条数据删了几秒，也太拉垮了吧...
    // 结果只能用 int 接收
    @Modifying
    @Transactional
    @Query(value = "delete from WorkflowInstanceStuatusInfoDo where status  = 99")
    int deleteAllByStatusBefore(int status);

    // 状态检查
    Optional<WorkflowInstanceStuatusInfoDo> findByWfInstanceIdAndStatus(Long wfInstanceId, int status);

}
