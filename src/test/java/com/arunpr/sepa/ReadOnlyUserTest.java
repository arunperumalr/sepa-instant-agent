package com.arunpr.sepa;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Proves the agent's database user is read-only.
 * Skipped unless the DB_READONLY_PASSWORD environment variable is set.
 * Run the app once first, so Flyway has created the views and grants.
 */
class  ReadOnlyUserTest {

    private static final String URL =
            "jdbc:mysql://localhost:3306/payments?useSSL=false&allowPublicKeyRetrieval=true";
    private static Connection conn;

    @BeforeAll
    static void connect() throws Exception {
        String pw = System.getenv("DB_READONLY_PASSWORD");
        assumeTrue(pw != null && !pw.isBlank(), "DB_READONLY_PASSWORD not set - skipping");
        conn = DriverManager.getConnection(URL, "agent_ro", pw);
    }

    @AfterAll
    static void close() throws Exception {
        if (conn != null) conn.close();
    }

    private static void run(String sql) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
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