package com.modusbox.client.router;

import com.modusbox.client.exception.CamelErrorProcessor;
import com.modusbox.client.exception.RouteExceptionHandlingConfigurer;
import org.apache.camel.builder.RouteBuilder;

import javax.annotation.Generated;

/**
 * Generated from OpenApi specification by Camel REST DSL generator.
 */
@Generated("org.apache.camel.generator.openapi.PathGenerator")
public final class CoreConnectorAPI extends RouteBuilder {

    private final RouteExceptionHandlingConfigurer exceptionHandlingConfigurer = new RouteExceptionHandlingConfigurer();

    public void configure() {

        // Add our global exception handling strategy
        exceptionHandlingConfigurer.configureExceptionHandling(this);

        // In this case the GET parties will return the loan account with client details
        from("cxfrs:bean:api-rs-server?bindingStyle=SimpleConsumer")
                .to("bean-validator://x")
                .toD("direct:${header.operationName}");
    }
}
