package com.example.demo.clients.serde;

import com.example.demo.model.dto.Dto;
import com.example.demo.model.dto.SessionDto;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.common.serialization.Deserializer;

public class SessionDtoDeserializer implements Deserializer<SessionDto> {
    @Override
    public SessionDto deserialize(String topic, byte[] bytes) {
        try {
            Dto.SessionDto sessionDto = Dto.SessionDto.parseFrom(bytes);
            return new SessionDto(sessionDto.getId()
                    , sessionDto.getGettingAt()
                    , sessionDto.getIpAddress()
                    , sessionDto.getSendingAt()
            );
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
