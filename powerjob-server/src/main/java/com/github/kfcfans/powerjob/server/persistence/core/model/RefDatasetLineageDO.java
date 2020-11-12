package com.github.kfcfans.powerjob.server.persistence.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ref_dataset_lineage")
public class RefDatasetLineageDO {
        @Id

        private Long jobInfoId;
        private String anotherName;
        private String tableName;
        private String lineField;
        private String lineWhereField;
        private String lineTableWhere;
        private String flowInfoTableId;
}
