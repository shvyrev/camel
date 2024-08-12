package com.example.demo.service;

import com.example.demo.exceptions.NoSessionException;
import com.example.demo.model.Session;
import com.example.demo.model.Trains;
import com.example.demo.model.dto.CreateSessionDto;
import com.example.demo.model.dto.Dto;
import com.example.demo.model.dto.SessionDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.protobuf.ProtobufDataFormat;
import org.apache.camel.model.dataformat.ProtobufLibrary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
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
        String serializerClass = "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer";
        String keySerializerClass = "org.apache.kafka.common.serialization.StringSerializer";

        ProtobufDataFormat format = new ProtobufDataFormat(Dto.SessionDto.getDefaultInstance());

        from("direct:createSession")
                .log(INFO, "Create session", "${body} and ${headers}")
                .process(this::mapToKafkaMessage)
                .log(INFO, "dto to kafka", "${body} and ${headers}")
                .marshal(format)
                .to("kafka:" + topic + "?"
                        + "keySerializer=" + keySerializerClass + "&"
                        + "valueSerializer=" + serializerClass + "&"
                        + "additionalProperties.schema.registry.url=http://localhost:8811")
                .process(this::toRestResponse)
        ;

        from("kafka:sessions")
                .log(INFO, "Session received", "${body} and ${headers}")
                .unmarshal().protobuf(ProtobufLibrary.GoogleProtobuf, Dto.SessionDto.class)
//                .unmarshal().json(JsonLibrary.Jackson, SessionDto.class)
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

        from("direct:dropStreamRoute")
//                .to("callOtherService")
                .log("RESPONSE: ${body}")
                .process(exchange -> {
                    InputStream in = exchange.getIn().getBody(InputStream.class);
                    in.reset();
                });
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
        var sessionDto = (Dto.SessionDto) exchange.getIn().getBody();
//        SessionDto sessionDto = (SessionDto) exchange.getIn().getBody();
        var session = Session.of(sessionDto);
        exchange.getIn().setBody(session);
    }

    private void toRestResponse(Exchange exchange) {
        log.info("$ toRestResponse() called with: exchange = [{}]", exchange.getIn().getHeaders());

        InputStream in = exchange.getIn().getBody(InputStream.class);
        try {
            in.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        exchange.getIn().setBody(exchange.getIn().getHeader("rest-response").toString());
    }

    private void mapToKafkaMessage(Exchange exchange) {
        var dto = (CreateSessionDto)exchange.getIn().getBody();
        var ipAddress = getIpAddress(exchange);
        var gettingDate = nowMillis();
        var sendAt = toMillis(dto.time());
        var id = UUID.randomUUID().toString();
//        SessionDto sessionDto = new SessionDto(id, gettingDate, ipAddress, sendAt);

        exchange.getIn().setHeader("rest-response", id);

        Dto.SessionDto sessionDto = Dto.SessionDto.newBuilder()
                .setId(id)
                .setGettingAt(gettingDate)
                .setIpAddress(ipAddress)
                .setSendingAt(sendAt)
                .build();
        exchange.getIn().setBody(sessionDto);
    }
}
