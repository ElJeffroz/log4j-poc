package com.eljeffroz.pocs.log4jpoc;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {
    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase","true");
        logger.info("Starting server...");

        Undertow server = Undertow.builder()
                .addHttpListener(1337, "0.0.0.0")
                .setHandler(exchange -> {
                    logger.debug(String.format("Incoming request from %s", exchange.getRequestHeaders().getFirst("User-Agent")));

                    exchange.getResponseHeaders()
                            .put(Headers.CONTENT_TYPE, "text/plain");

                    exchange.getResponseSender()
                            .send("I log your User-Agent, so feel free to abuse it for a free shell, maybe I even run as root!");
                }).build();

        server.start();
    }
}

