package com.sky.subscription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class SkySubscriptionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkySubscriptionApplication.class, args);
    }

    @Component
    static class StartupLogger {

        private static final Logger log = LoggerFactory.getLogger(SkySubscriptionApplication.class);

        @EventListener
        public void onWebServerReady(WebServerInitializedEvent event) throws UnknownHostException {
            int port = event.getWebServer().getPort();
            String host = InetAddress.getLocalHost().getHostAddress();
            log.info("----------------------------------------------------------");
            log.info("Application is running!");
            log.info("Local:    http://localhost:{}", port);
            log.info("Network:  http://{}:{}", host, port);
            log.info("----------------------------------------------------------");
        }
    }
}
