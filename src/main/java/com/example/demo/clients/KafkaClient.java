package com.example.demo.clients;

import com.example.demo.model.dto.Dto;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.protobuf.ProtobufDataFormat;
import org.apache.camel.model.dataformat.ProtobufLibrary;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaClient extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        ProtobufDataFormat format = new ProtobufDataFormat(Dto.SessionDto.getDefaultInstance());

        String topic = "sessions";
        String serializerClass = "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer";
        String keySerializerClass = "org.apache.kafka.common.serialization.StringSerializer";

        from("timer://foo?period=1000")
                .setBody(constant(sessionDto()))
                .marshal().protobuf(ProtobufLibrary.GoogleProtobuf, Dto.SessionDto.class)
                .to("kafka:" + topic + "?"
                        + "keySerializer=" + keySerializerClass + "&"
                        + "valueSerializer=" + serializerClass + "&"
                        + "additionalProperties.schema.registry.url=http://localhost:8811");

    }


    private Dto.SessionDto sessionDto() {
        return Dto.SessionDto.newBuilder()
                .setId("id")
                .setGettingAt(12L)
                .setIpAddress("14")
                .setSendingAt(15L)
                .build();
    }
}
