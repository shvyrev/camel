package com.example.demo.service;

import com.example.demo.model.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.apache.camel.LoggingLevel.INFO;

@Slf4j
@Service
public class TrainsService extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct::trains")
                .log(INFO, "Get trains", "${body} and ${headers}")
                .process(this::mapFindTrainsParameters)
                .to("jpa:com.example.demo.model.Trains?namedQuery=findTrains")
                .split().body().threads(5)
                .log(INFO, "Trains found", "${body}")
                .to("direct:upTrainName")
                .end()
                .to("mock:result");
    }

    private void mapFindTrainsParameters(Exchange exchange) {
        var session = (Session)exchange.getIn().getBody();
        exchange.getIn().setHeader("CamelJpaParameters", Map.of("start", session.getSendingAt()));
    }
}
