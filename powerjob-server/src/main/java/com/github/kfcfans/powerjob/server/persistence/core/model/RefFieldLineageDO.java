package com.github.kfcfans.powerjob.server.persistence.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ref_field_lineage")
public class RefFieldLineageDO {

    private String tableName;

    private String fileName;
    @Id
    private Long id;
}
