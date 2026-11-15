package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class NameSplitMigrationLoadTest {

    private static final int READERS = 4;
    private static final int SEEDED_CUSTOMERS = 200;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("name_split")
            .withUsername("bookstore")
            .withPassword("bookstore");

    @Test
    void expandKeepsOldReadersWorkingAndBothColumnSetsInSync() throws Exception {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUser(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        flyway(dataSource, "2").migrate();
        for (int i = 0; i < SEEDED_CUSTOMERS; i++) {
            jdbc.update("INSERT INTO customers (full_name) VALUES (?)", "Name" + i + " Surname" + i);
        }

        List<Exception> errors = new CopyOnWriteArrayList<>();
        AtomicBoolean keepReading = new AtomicBoolean(true);
        AtomicInteger reads = new AtomicInteger();
        CountDownLatch started = new CountDownLatch(READERS);
        ExecutorService pool = Executors.newFixedThreadPool(READERS);
        for (int i = 0; i < READERS; i++) {
            pool.submit(() -> {
                started.countDown();
                while (keepReading.get()) {
                    try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
                        st.executeQuery("SELECT full_name FROM customers");
                        reads.incrementAndGet();
                    } catch (SQLException e) {
                        errors.add(e);
                    }
                }
            });
        }
        started.await(5, TimeUnit.SECONDS);

        flyway(dataSource, "4").migrate();

        keepReading.set(false);
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(errors).isEmpty();
        assertThat(reads.get()).isPositive();
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM customers WHERE first_name IS NULL OR last_name IS NULL", Integer.class))
                .isZero();
        Map<String, Object> seeded = jdbc.queryForMap(
                "SELECT first_name, last_name FROM customers WHERE full_name = 'Name0 Surname0'");
        assertThat(seeded).containsEntry("first_name", "Name0").containsEntry("last_name", "Surname0");

        jdbc.update("INSERT INTO customers (full_name) VALUES ('Old Writer')");
        Map<String, Object> fromOldWriter = jdbc.queryForMap(
                "SELECT first_name, last_name FROM customers WHERE full_name = 'Old Writer'");
        assertThat(fromOldWriter).containsEntry("first_name", "Old").containsEntry("last_name", "Writer");

        jdbc.update("INSERT INTO customers (first_name, last_name) VALUES ('New', 'Writer')");
        assertThat(jdbc.queryForObject(
                        "SELECT full_name FROM customers WHERE first_name = 'New'", String.class))
                .isEqualTo("New Writer");

        flyway(dataSource, null).migrate();

        assertThat(jdbc.queryForList(
                        "SELECT column_name FROM information_schema.columns"
                                + " WHERE table_schema = 'public' AND table_name = 'customers'",
                        String.class))
                .doesNotContain("full_name")
                .contains("first_name", "last_name");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM customers", Integer.class))
                .isEqualTo(SEEDED_CUSTOMERS + 2);
    }

    private static Flyway flyway(PGSimpleDataSource dataSource, String target) {
        var config = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration");
        if (target != null) {
            config.target(target);
        }
        return config.load();
    }
}
