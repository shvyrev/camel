package com.example.demo.controller;

import com.example.demo.exceptions.NoSessionException;
import com.example.demo.model.dto.CreateSessionDto;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RestController extends RouteBuilder {

    @Value("${camel.servlet.mapping.context-path:/api/*}")
    private String contextPath;

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .handled(true)
                .to("bean:exceptionHandler?method=onRestException");

        onException(NoSessionException.class)
                .handled(true)
                .to("bean:exceptionHandler?method=onNoSessionException");

        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.auto)
                .dataFormatProperty("moduleClassNames", "com.fasterxml.jackson.datatype.jsr310.JavaTimeModule")
                .dataFormatProperty("disableFeatures", "WRITE_DATES_AS_TIMESTAMPS")
                .dataFormatProperty("enableFeatures","ACCEPT_CASE_INSENSITIVE_PROPERTIES")
                .dataFormatProperty("prettyPrint", "true")
                .enableCORS(true)
                .port(8080)
                .contextPath(contextPath.substring(0, contextPath.length() - 2))
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "User API")
                .apiProperty("api.version", "1.0.0");


        rest("/request")
                .post()
                .description("На вход получает Dto c датой")
                .type(CreateSessionDto.class)
                .to("direct:createSession");

        rest("/session")
                .get("{session}")
                .description("на вход должен принимать guid (результат выполнения первого сервиса")
                .to("direct:getSession");
    }
}
