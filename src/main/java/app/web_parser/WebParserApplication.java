package app.web_parser;

import app.web_parser.config.AppConfig;
import app.web_parser.controller.MainController;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableEncryptableProperties
//@EnableScheduling
public class WebParserApplication implements CommandLineRunner {

    @Autowired
    MainController mainController;

    public static void main(String[] args) throws InterruptedException {
        SpringApplication app = new SpringApplication(WebParserApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        /*      app.setRegisterShutdownHook(false);*/
        ApplicationContext context = app.run(args);

        MainController mainController = (MainController) context.getBean("mainController");
        AppConfig appConfig = (AppConfig) context.getBean("appConfig");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        long period = appConfig.getScheduledExecutorServiceDelay();

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                mainController.listPendingProcessTask();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 0, period, TimeUnit.SECONDS);
    }

    @Override
    public void run(String... args) throws Exception {
//		ApplicationContext context =	SpringApplication.run(WebParserApplication.class, args);

    }
}
