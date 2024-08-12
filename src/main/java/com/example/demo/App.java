package com.example.demo;

import com.example.demo.base.TrainRepo;
import com.example.demo.model.Trains;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class App {

    @Autowired
    TrainRepo trainRepo;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() throws Exception {

//        Dto.SessionDto sessionDto = Dto.SessionDto.newBuilder()
//                .setId("1")
//                .setGettingAt(1L)
//                .setIpAddress("127.0.0.1")
//                .setSendingAt(2L)
//                .build();
//
//        log.info("$ dto {}", sessionDto);

        IntStream.range(0, 5).forEach(i -> {
            Trains train = new Trains();
            train.setId(i);
            train.setStart(LocalDateTime.now().plusMinutes(i));
            train.setStation(i);
            train.setName("Train " + i);
            trainRepo.save(train);
        });
    }


}
