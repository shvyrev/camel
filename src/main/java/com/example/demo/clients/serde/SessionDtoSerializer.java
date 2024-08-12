package com.example.demo.clients.serde;

import com.example.demo.model.dto.Dto;
import com.example.demo.model.dto.SessionDto;
import org.apache.kafka.common.serialization.Serializer;

public class SessionDtoSerializer implements Serializer<SessionDto> {

    @Override
    public byte[] serialize(String s, SessionDto value) {
        return Dto.SessionDto.newBuilder()
                .setId(value.getId())
                .setGettingAt(value.getGettingAt())
                .setIpAddress(value.getIpAddress())
                .setSendingAt(value.getSendingAt())
                .build().toByteArray();
    }
}
