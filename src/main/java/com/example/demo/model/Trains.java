package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@NamedQuery(name = "findTrains", query = "SELECT t FROM Trains t WHERE t.start > :start order by t.start DESC")
@Entity
@Table(name = "trains")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Trains {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_train")
    private int id;
    @Column(nullable = false)
    private LocalDateTime start;
    @Column(name = "id_station_start")
    private int station;
    @Column(name = "train_name", nullable = false)
    private String name;
}
