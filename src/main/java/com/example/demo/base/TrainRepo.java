package com.example.demo.base;

import com.example.demo.model.Trains;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainRepo extends JpaRepository<Trains, Integer> {
}

