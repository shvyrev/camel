package com.example.demo.service;

import com.example.demo.exceptions.NoSessionException;
import com.example.demo.model.Session;
import com.example.demo.model.Trains;
import com.example.demo.model.dto.CreateSessionDto;
import com.example.demo.model.dto.SessionDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.demo.utils.IpAddressUtil.getIpAddress;
import static com.example.demo.utils.TimeUtil.nowMillis;
import static com.example.demo.utils.TimeUtil.toMillis;
import static org.apache.camel.LoggingLevel.INFO;

@Slf4j
@Service
public class SessionService extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        String brokers = "localhost:9092";
        String topic = "sessions";
        String keySerializerClass = "org.apache.kafka.common.serialization.StringSerializer";
        String serializerClass = "com.example.demo.clients.serde.SessionDtoSerializer";
        String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
        String valueDeserializer = "com.example.demo.clients.serde.SessionDtoDeserializer";

        from("direct:createSession")
                .log(INFO, "Create session", "${body} and ${headers}")
                .process(this::mapToKafkaMessage)
                .to("kafka:" + topic + "?"
                        + "keySerializer=" + keySerializerClass + "&"
                        + "valueSerializer=" + serializerClass
                )
                .process(this::toRestResponse);

        from("kafka:" + topic + "?"
                + "keyDeserializer=" + keyDeserializer + "&"
                + "valueDeserializer=" + valueDeserializer)
                .log(INFO, "Session received", "${body} and ${headers}")
                .process(this::mapToSessionEntity)
                .log(INFO, "Session saved", "${body} and ${headers}")
                .to("jpa:com.example.demo.model.Session?persistenceUnit=postgresql&flushOnSend=true");

        from("direct:getSession")
                .log(INFO, "Getting session", "${body} and ${headers}")
                .setBody(header("session"))
                .process(this::mapFindSessionParameters)
//                .setHeader("CamelJpaParameters", constant(Map.of("id", UUID.fromString("${body}"))))
                .to("jpa:com.example.demo.model.Session?namedQuery=findSessionById")
                .doTry()
                    .process(this::getFirstSession)
                    .to("direct::trains")
                .doCatch(NoSessionException.class)
                    .process(this::noSessionFound)
                .end();

        from("direct:upTrainName")
                .process(this::upTrainName);
    }
    private void getFirstSession(Exchange exchange) {
        List<Session> values= (List<Session>) exchange.getIn().getBody();
        var session= values.stream().findFirst()
                .orElseThrow(NoSessionException::new);
        exchange.getIn().setBody(session);
    }

    private void noSessionFound(Exchange exchange) {
        exchange.getIn().setBody(null);
    }

    private void upTrainName(Exchange exchange) {
        var train = (Trains)exchange.getIn().getBody();
        train.setName(train.getName().toUpperCase());
        exchange.getIn().setBody(train);
    }

    private void mapFindSessionParameters(Exchange exchange) {
        var id = exchange.getIn().getHeader("session").toString();
        exchange.getIn().setHeader("CamelJpaParameters", Map.of("id", UUID.fromString(id)));
    }

    private void mapToSessionEntity(Exchange exchange) {
        var sessionDto = (SessionDto) exchange.getIn().getBody();
//        SessionDto sessionDto = (SessionDto) exchange.getIn().getBody();
        var session = Session.of(sessionDto);
        exchange.getIn().setBody(session);
    }

    private void toRestResponse(Exchange exchange) {
        log.info("$ toRestResponse() called with: exchange = [{}]", exchange.getIn().getHeaders());
        exchange.getIn().setBody(exchange.getIn().getHeader("rest-response").toString());
    }

    private void mapToKafkaMessage(Exchange exchange) {
        var dto = (CreateSessionDto)exchange.getIn().getBody();
        var ipAddress = getIpAddress(exchange);
        var gettingDate = nowMillis();
        var sendAt = toMillis(dto.time());
        var id = UUID.randomUUID().toString();

        SessionDto sessionDto = new SessionDto(id, gettingDate, ipAddress, sendAt);
        exchange.getIn().setBody(sessionDto);
        exchange.getIn().setHeader("rest-response", id);
    }
}
