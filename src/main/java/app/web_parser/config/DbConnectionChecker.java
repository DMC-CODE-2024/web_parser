package app.web_parser.config;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DbConnectionChecker {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public void checkAppDbConnection(@Qualifier("springDataSource") DataSource dataSource) {
        checkDbConnection(dataSource, "app");
    }

    public void checkAuditDbConnection(@Qualifier("auditDataSource") DataSource dataSource) {
        checkDbConnection(dataSource, "aud");
    }

    private void checkDbConnection(DataSource dataSource, String dbName) {
        try (Connection connection = dataSource.getConnection()) {
            logger.info("Database connection for " + dbName + " is successful!");
        } catch (SQLException e) {
            logger.error("alert1601: DB connection failed for " + dbName + " while getting DB configuration value");
            e.printStackTrace();
        }
    }
}