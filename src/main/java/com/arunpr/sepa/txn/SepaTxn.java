package com.arunpr.sepa.txn;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sepa_txn")
@Getter
@Setter
public class SepaTxn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "txn_id")
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "txn_ref", nullable = false, length = 20, updatable = false)
    private String txnRef;

    @Column(name = "idempotency_key", nullable = false, length = 64, updatable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "scheme", nullable = false, length = 10, updatable = false)
    private Scheme scheme;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "direction", nullable = false, length = 12, updatable = false)
    private Direction direction;

    @Column(name = "wallet_id", nullable = false, length = 32)
    private String walletId;

    @Column(name = "beneficiary_hash", nullable = false, length = 16)
    private String beneficiaryHash;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "EUR";

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "wallet_debit_status", nullable = false, length = 10)
    private StageStatus walletDebitStatus = StageStatus.PEND;

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "wallet_credit_status", length = 10)
    private StageStatus walletCreditStatus;

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "bank_debit_status", length = 10)
    private StageStatus bankDebitStatus;

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "bank_credit_status", length = 10)
    private StageStatus bankCreditStatus;

    @Column(name = "reject_code", length = 10)
    private String rejectCode;

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "postpay_debit_status", length = 10)
    private StageStatus postpayDebitStatus;

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "postpay_credit_status", length = 10)
    private StageStatus postpayCreditStatus;

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "audit_status", length = 10)
    private StageStatus auditStatus;

    @Column(name = "last_publish_error", length = 100)
    private String lastPublishError;

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "recall_status", length = 10)
    private StageStatus recallStatus;

    @Column(name = "recall_requested_at")
    private LocalDateTime recallRequestedAt;

    @Column(name = "recall_reason", length = 40)
    private String recallReason;

    @Column(name = "recall_updated_at")
    private LocalDateTime recallUpdatedAt;

    // Filled by MySQL defaults, never by the application
    @Column(name = "created_at", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    @Setter(AccessLevel.NONE)
    private int version;

    protected SepaTxn() { }   // required by JPA

    public SepaTxn(String txnRef, String idempotencyKey, Scheme scheme, Direction direction,
                   String walletId, String beneficiaryHash, BigDecimal amount) {
        this.txnRef = txnRef;
        this.idempotencyKey = idempotencyKey;
        this.scheme = scheme;
        this.direction = direction;
        this.walletId = walletId;
        this.beneficiaryHash = beneficiaryHash;
        this.amount = amount;
    }

    public StageStatus statusOf(Stage stage) {
        return switch (stage) {
            case WALLET_DEBIT -> walletDebitStatus;
            case WALLET_CREDIT -> walletCreditStatus;
            case BANK_DEBIT -> bankDebitStatus;
            case BANK_CREDIT -> bankCreditStatus;
            case POSTPAY_DEBIT -> postpayDebitStatus;
            case POSTPAY_CREDIT -> postpayCreditStatus;
            case AUDIT -> auditStatus;
            case RECALL -> recallStatus;
        };
    }

    public void applyStatus(Stage stage, StageStatus status) {
        switch (stage) {
            case WALLET_DEBIT -> walletDebitStatus = status;
            case WALLET_CREDIT -> walletCreditStatus = status;
            case BANK_DEBIT -> bankDebitStatus = status;
            case BANK_CREDIT -> bankCreditStatus = status;
            case POSTPAY_DEBIT -> postpayDebitStatus = status;
            case POSTPAY_CREDIT -> postpayCreditStatus = status;
            case AUDIT -> auditStatus = status;
            case RECALL -> recallStatus = status;
        }
    }
}