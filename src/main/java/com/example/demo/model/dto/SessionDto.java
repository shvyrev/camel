package com.example.demo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionDto {
    private String id;
    private long gettingAt;
    private String ipAddress;
    private long sendingAt;
}
