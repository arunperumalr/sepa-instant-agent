package com.arunpr.sepa.txn;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "reject_code_ref")
@Getter
public class RejectCodeRef {

    @Id
    @Column(name = "code", length = 10)
    private String code;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "common_cause", nullable = false, length = 500)
    private String commonCause;

    @Column(name = "action_needed", nullable = false, length = 500)
    private String actionNeeded;

    @Column(name = "action_owner", nullable = false, length = 10)
    private String actionOwner;

    protected RejectCodeRef() { }   // required by JPA
}