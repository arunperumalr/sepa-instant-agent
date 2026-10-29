package com.arunpr.sepa.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    // ---- Writer (loader_rw): Flyway, JPA and the seeder use this one ----

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties writerDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource writerDataSource(DataSourceProperties writerDataSourceProperties) {
        return writerDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    @Primary
    public JdbcClient writerJdbcClient(DataSource writerDataSource) {
        return JdbcClient.create(writerDataSource);
    }

    // ---- Agent (agent_ro): read-only lookups only ----

    @Bean
    @ConfigurationProperties("agent.datasource")
    public DataSourceProperties agentDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource agentDataSource(
            @Qualifier("agentDataSourceProperties") DataSourceProperties agentDataSourceProperties) {
        return agentDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    public JdbcClient agentJdbcClient(@Qualifier("agentDataSource") DataSource agentDataSource) {
        return JdbcClient.create(agentDataSource);
    }
}