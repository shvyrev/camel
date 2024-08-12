package com.example.demo.model;

import com.example.demo.model.dto.Dto;
import com.example.demo.model.dto.SessionDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.example.demo.utils.TimeUtil.toLocalDateTime;

@NamedQuery(name = "findSessionById", query = "SELECT s FROM Session s WHERE s.id = :id")
@Entity
@Table(name = "sessions")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Session {
    @Id
    private UUID id;
    @Column(name = "getting_at", nullable = false)
    private LocalDateTime gettingAt;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "sending_at")
    private LocalDateTime sendingAt;

    @Transient
    public static Session of(SessionDto sessionDto) {
        Session session = new Session();
        session.setId(UUID.fromString(sessionDto.getId()));
        session.setGettingAt(toLocalDateTime(sessionDto.getGettingAt()));
        session.setIpAddress(sessionDto.getIpAddress());
        session.setSendingAt(toLocalDateTime(sessionDto.getSendingAt()));
        return session;
    }

    public static Session of(Dto.SessionDto sessionDto) {
        Session session = new Session();
        session.setId(UUID.fromString(sessionDto.getId()));
        session.setGettingAt(toLocalDateTime(sessionDto.getGettingAt()));
        session.setIpAddress(sessionDto.getIpAddress());
        session.setSendingAt(toLocalDateTime(sessionDto.getSendingAt()));
        return session;
    }
}
