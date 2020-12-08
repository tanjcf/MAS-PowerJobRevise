package com.github.kfcfans.powerjob.server.persistence.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "workflow_instance_stuatus")
public class WorkflowInstanceStuatusInfoDo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    // workflowInstanceId（任务实例表都使用单独的ID作为主键以支持潜在的分表需求）
    private Long wfInstanceId;

    // workflow 状态（WorkflowInstanceStatus）
    private Integer status;


}
