package com.glocks.web_parser;

import com.glocks.web_parser.controller.MainController;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableEncryptableProperties
@EnableScheduling
public class WebParserApplication implements CommandLineRunner {

    @Autowired
    MainController mainController;

    public static void main(String[] args) throws InterruptedException {
        SpringApplication app = new SpringApplication(WebParserApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setRegisterShutdownHook(false);
        ApplicationContext context = app.run(args);
        MainController mainController = (MainController) context.getBean("mainController");
        mainController.listPendingProcessTask();
    }

    @Override
    public void run(String... args) throws Exception {
//		ApplicationContext context =	SpringApplication.run(WebParserApplication.class, args);

    }
}
