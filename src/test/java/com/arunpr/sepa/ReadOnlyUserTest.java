package com.arunpr.sepa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Proves the agent's configured database user is read-only.
 * Credentials come from agent.datasource.* in application-local.properties.
 */
@SpringBootTest
@ActiveProfiles("local")
class ReadOnlyUserTest {

    @Value("${agent.datasource.url}")
    private String url;

    @Value("${agent.datasource.username}")
    private String username;

    @Value("${agent.datasource.password:}")
    private String password;

    private Connection conn;

    @BeforeEach
    void connect() throws Exception {
        assumeTrue(!password.isBlank(), "agent.datasource.password not set - skipping");
        conn = DriverManager.getConnection(url, username, password);
    }

    @AfterEach
    void close() throws Exception {
        if (conn != null) conn.close();
    }

    private void run(String sql) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    @Test
    void agentUserIsNotTheWriterUser() {
        assertThat(username).isEqualTo("agent_ro");
    }

    @Test
    void canSelectFromStatusView() throws Exception {
        run("SELECT txn_ref, wallet_masked, bank_credit_status, reject_code FROM v_txn_status LIMIT 1");
    }

    @Test
    void canSelectFromEventView() throws Exception {
        run("SELECT txn_ref, stage, new_status FROM v_txn_event LIMIT 1");
    }

    @Test
    void canSelectFromRejectCodeTable() throws Exception {
        run("SELECT code, title FROM reject_code_ref LIMIT 1");
    }

    @Test
    void cannotReadBaseTableDirectly() {
        SQLException e = assertThrows(SQLException.class,
                () -> run("SELECT wallet_id FROM sepa_txn LIMIT 1"));
        assertTrue(e.getMessage().toLowerCase().contains("denied"), e.getMessage());
    }

    @Test
    void cannotInsert() {
        SQLException e = assertThrows(SQLException.class,
                () -> run("INSERT INTO reject_code_ref (code, title, description, common_cause, action_needed, action_owner) "
                        + "VALUES ('HACK','x','x','x','x','BANK')"));
        assertTrue(e.getMessage().toLowerCase().contains("denied"), e.getMessage());
    }

    @Test
    void cannotUpdateOrDelete() {
        assertThrows(SQLException.class, () -> run("UPDATE reject_code_ref SET title = 'x'"));
        assertThrows(SQLException.class, () -> run("DELETE FROM reject_code_ref"));
    }
}