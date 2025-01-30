package app.web_parser.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"app.web_parser.repository.rep"}, //
        entityManagerFactoryRef = "reportEntityManagerFactory",
        transactionManagerRef = "reportTransactionManager")

@EntityScan("app.web_parser.model.rep")

public class ReportDbConfig {

    @Bean(name = "reportEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean reportEntityManagerFactory(
            @Qualifier("reportDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource)
                .packages("app.web_parser.model.rep") //
                .persistenceUnit("rep") //
                .properties(jpaProperties())
                .build();
    }

    @Bean(name = "reportDataSource")
    @ConfigurationProperties(prefix = "report.datasource")               //
    public DataSource reportDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "reportTransactionManager")
    public PlatformTransactionManager reportTransactionManager(
            @Qualifier("reportEntityManagerFactory") LocalContainerEntityManagerFactoryBean reportEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(reportEntityManagerFactory.getObject()));
    }

    protected Map<String, Object> jpaProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
        return props;
    }

}
