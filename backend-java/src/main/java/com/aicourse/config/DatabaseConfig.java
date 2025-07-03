package com.aicourse.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 数据库配置类
 */
@Configuration
public class DatabaseConfig {

    @Autowired
    private DataSource dataSource;

    /**
     * 在应用启动时设置数据库连接参数 (MySQL版本)
     */
    @Bean
    public ApplicationRunner setDatabaseParameters() {
        return args -> {
            try {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                
                // 设置MySQL会话级别参数
                jdbcTemplate.execute("SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO'");
                jdbcTemplate.execute("SET SESSION innodb_lock_wait_timeout = 50");
                jdbcTemplate.execute("SET SESSION autocommit = 1");
                
                System.out.println("MySQL session parameters set successfully");
            } catch (Exception e) {
                System.err.println("Failed to set MySQL parameters: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
} 