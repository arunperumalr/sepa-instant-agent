package com.arunpr.sepa.txn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("local")
class RejectCodeRefRepositoryTest {

    @Autowired
    private RejectCodeRefRepository repository;

    @Test
    void findsSeededCodeAc01() {
        RejectCodeRef ref = repository.findById("AC01").orElseThrow();

        assertThat(ref.getTitle()).isEqualTo("Invalid account number (IBAN)");
        assertThat(ref.getActionOwner()).isEqualTo("CUSTOMER");
    }

    @Test
    void unknownCodeIsNotFound() {
        assertThat(repository.findById("ZZ99")).isEmpty();
    }
}