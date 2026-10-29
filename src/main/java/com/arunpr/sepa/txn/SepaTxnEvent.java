package com.arunpr.sepa.txn;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "sepa_txn_event")
@Getter
public class SepaTxnEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "txn_id", nullable = false, updatable = false)
    private Long txnId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "stage", nullable = false, length = 20, updatable = false)
    private Stage stage;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "old_status", length = 10, updatable = false)
    private StageStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "new_status", nullable = false, length = 10, updatable = false)
    private StageStatus newStatus;

    @Column(name = "detail_code", length = 40, updatable = false)
    private String detailCode;

    @Column(name = "event_ts", insertable = false, updatable = false)
    private LocalDateTime eventTs;

    protected SepaTxnEvent() { }   // required by JPA

    public SepaTxnEvent(Long txnId, Stage stage, StageStatus oldStatus,
                        StageStatus newStatus, String detailCode) {
        this.txnId = txnId;
        this.stage = stage;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.detailCode = detailCode;
    }
}