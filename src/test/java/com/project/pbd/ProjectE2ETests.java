package com.project.pbd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Real HTTP + Spring + Oracle, enabled only against an isolated, empty test schema. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "PBD_E2E_URL", matches = ".+")
class ProjectE2ETests {
    private static final String SALE = "/api/achizitii/achizitieNoua";
    private static final String REPORTS = "/api/rapoarte/";
    private static final String TEST_CNP = "9000000000001";

    @Value("${local.server.port}") private int port;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactions;
    private RestClient http;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) throws Exception {
        String url = System.getenv("PBD_E2E_URL");
        String user = System.getenv("PBD_E2E_USER");
        String password = System.getenv("PBD_E2E_PASSWORD");
        if (user == null || !user.startsWith("PBD_E2E_")) {
            throw new IllegalStateException("E2E requires a new PBD_E2E_* schema; never use the application schema");
        }
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            runScript(connection, "01_schema.sql");
            runScript(connection, "triggers/calculate_acquisition_payment.sql");
            runScript(connection, "03_sale_procedure.sql");
            runScript(connection, "04_left_to_pay_function.sql");
            try (Statement statement = connection.createStatement();
                 var errors = statement.executeQuery("SELECT COUNT(*) FROM user_errors WHERE attribute = 'ERROR'")) {
                errors.next();
                assertEquals(0, errors.getInt(1), "Oracle trigger, procedure and function must compile");
            }
        }
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", () -> user);
        registry.add("spring.datasource.password", () -> password);
        registry.add("spring.jpa.open-in-view", () -> "false");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    private static void runScript(Connection connection, String file) throws Exception {
        try (var input = ProjectE2ETests.class.getResourceAsStream("/db/" + file)) {
            assertNotNull(input, file);
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            boolean block = sql.contains("CREATE OR REPLACE");
            StringBuilder command = new StringBuilder();
            for (String line : sql.lines().toList()) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
                if (block && trimmed.equals("/")) {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(command.toString());
                    }
                    command.setLength(0);
                } else {
                    command.append(line).append('\n');
                    if (!block && trimmed.endsWith(";")) {
                        String text = command.toString().trim();
                        try (Statement statement = connection.createStatement()) {
                            statement.execute(text.substring(0, text.length() - 1));
                        }
                        command.setLength(0);
                    }
                }
            }
        }
    }

    @BeforeEach
    void resetTestSchema() throws Exception {
        jdbc.update("DELETE FROM achizitii");
        jdbc.update("DELETE FROM clienti");
        try (Connection connection = jdbc.getDataSource().getConnection()) {
            runScript(connection, "02_seed.sql");
        }
        http = RestClient.builder().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void seedDataAndAllExistingReportsMatchTheAssignment() {
        assertEquals(4, rows("/api/clienti").size());
        assertEquals(5, rows("/api/achizitii").size());
        assertEquals(2, rows(REPORTS + "achizitiAchitate").size());
        List<Map<String, Object>> detail = rows(REPORTS + "achizitiiDetalite");
        assertEquals(List.of("Siemens", "Bosch", "LG", "Sony", "Indesit"),
                detail.stream().map(row -> row.get("product")).toList());
        assertMoney("100", detail.get(1).get("remainingAmount"));
        assertMoney("6", detail.get(3).get("remainingAmount"));
        assertTrue(rows(REPORTS + "clientiMajori").isEmpty());
        List<Map<String, Object>> worst = rows(REPORTS + "clientiCuCeleMaiMulteRestante");
        assertEquals(3, worst.size());
        assertEquals(List.of("Dima", "Duma", "Ion"), worst.stream().map(row -> row.get("name")).toList());
        assertMoney("91.89", worst.get(2).get("paymentRate"));
        BigDecimal left = http.get().uri("/api/clienti/restDePlata?cnp=1234567890123&product=Sony")
                .retrieve().body(BigDecimal.class);
        assertMoney("6", left);
        assertEquals(3, jdbc.queryForObject("SELECT COUNT(*) FROM user_objects WHERE status = 'VALID' "
                + "AND object_name IN ('SELL_PRODUCT', 'LEFT_TO_PAY', 'TRG_ACQUISITION_PAYMENT')", Integer.class));
    }

    @Test
    void saleProcedureAndTriggerApplySurplusCreditOnlyAndPartialPaymentThroughHttp() {
        client(TEST_CNP, "Sale", "Test");
        sale(TEST_CNP, "Surplus", "2025-01-01", "100", "150");
        assertMoney("50", balance(TEST_CNP));
        assertMoney("100", paid("Surplus"));
        sale(TEST_CNP, "CreditOnly", "2025-01-02", "30", "0");
        assertMoney("20", balance(TEST_CNP));
        assertMoney("30", paid("CreditOnly"));
        sale(TEST_CNP, "Partial", "2025-01-03", "100", "30");
        assertMoney("0", balance(TEST_CNP));
        assertMoney("50", paid("Partial"));
        assertMoney("50", http.get().uri("/api/clienti/restDePlata?cnp=" + TEST_CNP + "&product=Partial")
                .retrieve().body(BigDecimal.class));
    }

    @Test
    void invalidHttpRequestsAreRejectedWithoutWritingAnything() {
        client(TEST_CNP, "Invalid", "Test");
        List<Map<String, Object>> requests = new ArrayList<>();
        for (Map.Entry<String, Object> invalid : Map.<String, Object>of(
                "cnp", "short", "product", " ", "price", -1, "collectedAmount", -1,
                "acquisitionDate", "invalid-date").entrySet()) {
            Map<String, Object> request = new java.util.HashMap<>(payload(TEST_CNP, "Invalid", "2025-01-01", "100", "10"));
            request.put(invalid.getKey(), invalid.getValue());
            requests.add(request);
        }
        requests.add(payload(TEST_CNP, "Invalid", "2025-01-01", "0", "10"));
        requests.add(payload(TEST_CNP, "Invalid", "2025-01-01", "100", "1.234"));
        requests.add(payload(TEST_CNP, "X".repeat(256), "2025-01-01", "100", "1"));
        Map<String, Object> missing = new java.util.HashMap<>(payload(TEST_CNP, "Invalid", "2025-01-01", "100", "10"));
        missing.remove("price");
        requests.add(missing);
        for (Map<String, Object> request : requests) assertEquals(400, post(request));
        assertEquals(404, post(payload("9999999999999", "Unknown", "2025-01-01", "100", "10")));
        int missingStatus = http.get().uri("/api/clienti/restDePlata?cnp=" + TEST_CNP + "&product=Missing")
                .exchange((request, response) -> response.getStatusCode().value());
        assertEquals(404, missingStatus);
        assertEquals(5, jdbc.queryForObject("SELECT COUNT(*) FROM achizitii", Integer.class));
        assertMoney("0", balance(TEST_CNP));
    }

    @Test
    void oracleFunctionDirectTriggerAndRollbackWorkOutsideJpa() {
        client(TEST_CNP, "Oracle", "Test");
        jdbc.update("UPDATE clienti SET disponibil = 30 WHERE cnp = ?", TEST_CNP);
        jdbc.update("INSERT INTO achizitii(client_id, produs, data_achizitie, pret, suma_incasata) "
                + "SELECT id, 'Direct', DATE '2025-01-01', 100, 50 FROM clienti WHERE cnp = ?", TEST_CNP);
        assertMoney("80", paid("Direct"));
        assertMoney("0", balance(TEST_CNP));
        assertMoney("20", jdbc.queryForObject("SELECT left_to_pay(?, 'Direct') FROM dual", BigDecimal.class, TEST_CNP));
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            jdbc.update("CALL sell_product(?, 'Rollback', DATE '2025-01-01', 100, 150)", TEST_CNP);
            assertMoney("50", balance(TEST_CNP));
            status.setRollbackOnly();
        });
        assertMoney("0", balance(TEST_CNP));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM achizitii WHERE produs = 'Rollback'", Integer.class));
        jdbc.update("CALL sell_product(?, 'Direct', DATE '2025-01-02', 100, 20)", TEST_CNP);
        assertMoney("100", jdbc.queryForObject("SELECT left_to_pay(?, 'Direct') FROM dual", BigDecimal.class, TEST_CNP));
        assertThrows(DataAccessException.class, () -> jdbc.queryForObject(
                "SELECT left_to_pay(?, 'Missing') FROM dual", BigDecimal.class, TEST_CNP));
        assertThrows(DataAccessException.class, () -> jdbc.update(
                "CALL sell_product('9999999999999', 'Missing', SYSDATE, 100, 20)"));
    }

    @Test
    void concurrentSalesCannotSpendTheSameClientCreditTwice() throws Exception {
        client(TEST_CNP, "Concurrent", "Test");
        jdbc.update("UPDATE clienti SET disponibil = 100 WHERE cnp = ?", TEST_CNP);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> post(payload(TEST_CNP, "ConcurrentA", "2025-01-01", "100", "0")));
            var second = pool.submit(() -> post(payload(TEST_CNP, "ConcurrentB", "2025-01-01", "100", "0")));
            assertEquals(200, first.get(30, TimeUnit.SECONDS));
            assertEquals(200, second.get(30, TimeUnit.SECONDS));
        }
        assertMoney("100", jdbc.queryForObject("SELECT SUM(suma_incasata) FROM achizitii "
                + "WHERE produs IN ('ConcurrentA', 'ConcurrentB')", BigDecimal.class));
        assertMoney("0", balance(TEST_CNP));
    }

    @Test
    void majorClientsUseConsecutiveCalendarYearsAndExcludeAnyPartialPurchase() {
        String four = "9000000000002", expensive = "9000000000003", partial = "9000000000004";
        String distant = "9000000000005", threshold = "9000000000006";
        for (String cnp : List.of(four, expensive, partial, distant, threshold)) client(cnp, "Major", cnp.substring(9));
        sale(four, "A", "2024-01-01", "100", "100");
        sale(four, "B", "2024-12-31", "100", "100");
        sale(four, "C", "2025-01-01", "100", "100");
        sale(four, "D", "2025-12-31", "100", "100");
        sale(expensive, "E", "2025-01-01", "1000.01", "1000.01");
        sale(partial, "F", "2025-01-01", "1500", "1500");
        sale(partial, "G", "2020-01-01", "100", "1");
        sale(distant, "H", "2024-12-31", "600", "600");
        sale(distant, "I", "2026-01-01", "600", "600");
        sale(threshold, "J", "2025-01-01", "1000", "1000");
        List<Map<String, Object>> result = rows(REPORTS + "clientiMajori");
        assertEquals(List.of(four, expensive), result.stream().map(row -> row.get("cnp")).toList());
        assertMoney("400", result.getFirst().get("totalCollectedAmount"));
    }

    @Test
    void worstClientsIncludeTiesAndPaymentRateIsWeightedOverAllPurchases() {
        String first = "9000000000007", second = "9000000000008";
        client(first, "WorstA", "Test");
        client(second, "WorstB", "Test");
        sale(first, "W1", "2025-01-01", "100", "0");
        sale(first, "W2", "2025-01-01", "200", "100");
        sale(first, "W3", "2025-01-01", "100", "100");
        sale(second, "W4", "2025-01-01", "100", "0");
        sale(second, "W5", "2025-01-01", "200", "100");
        List<Map<String, Object>> result = rows(REPORTS + "clientiCuCeleMaiMulteRestante");
        assertEquals(List.of(first, second), result.stream().map(row -> row.get("cnp")).toList());
        assertEquals(2, ((Number) result.getFirst().get("unpaidProductCount")).intValue());
        assertMoney("50", result.getFirst().get("paymentRate"));
        assertMoney("33.33", result.get(1).get("paymentRate"));
    }

    @Test
    void schemaRejectsInvalidClientDetailsAndNegativeBalances() {
        for (String cnp : List.of("123", "abcdefghijklm")) {
            assertThrows(DataAccessException.class, () -> client(cnp, "Invalid", "Test"));
        }
        assertThrows(DataAccessException.class, () -> client(TEST_CNP, "X".repeat(16), "Test"));
        assertThrows(DataAccessException.class, () -> client(TEST_CNP, "Test", "X".repeat(21)));
        assertThrows(DataAccessException.class, () -> jdbc.update(
                "UPDATE clienti SET telefon = '12345678X' WHERE cnp = '1234567890123'"));
        assertThrows(DataAccessException.class, () -> jdbc.update(
                "UPDATE clienti SET disponibil = -1 WHERE cnp = '1234567890123'"));
        assertThrows(DataAccessException.class, () -> jdbc.update(
                "UPDATE clienti SET cnp = '1234567890124' WHERE cnp = '1234567890123'"));
    }

    private void client(String cnp, String name, String surname) {
        jdbc.update("INSERT INTO clienti(nume, prenume, cnp, telefon, disponibil) VALUES (?, ?, ?, ?, 0)",
                name, surname, cnp, cnp.length() >= 9 ? cnp.substring(cnp.length() - 9) : "000000000");
    }

    private List<Map<String, Object>> rows(String path) {
        return http.get().uri(path).retrieve().body(new ParameterizedTypeReference<>() {});
    }

    private int post(Map<String, Object> body) {
        return http.post().uri(SALE).contentType(MediaType.APPLICATION_JSON).body(body)
                .exchange((request, response) -> response.getStatusCode().value());
    }

    private void sale(String cnp, String product, String date, String price, String collected) {
        assertEquals(200, post(payload(cnp, product, date, price, collected)), product);
    }

    private Map<String, Object> payload(String cnp, String product, String date, String price, String collected) {
        return Map.of("cnp", cnp, "product", product, "acquisitionDate", date,
                "price", new BigDecimal(price), "collectedAmount", new BigDecimal(collected));
    }

    private BigDecimal balance(String cnp) {
        return jdbc.queryForObject("SELECT disponibil FROM clienti WHERE cnp = ?", BigDecimal.class, cnp);
    }

    private BigDecimal paid(String product) {
        return jdbc.queryForObject("SELECT suma_incasata FROM achizitii WHERE produs = ?", BigDecimal.class, product);
    }

    private void assertMoney(String expected, Object actual) {
        assertNotNull(actual);
        assertEquals(0, new BigDecimal(expected).compareTo(new BigDecimal(actual.toString())));
    }
}
