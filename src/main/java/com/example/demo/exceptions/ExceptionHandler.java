package com.example.demo.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExceptionHandler {

    public ResponseEntity<String> onNoSessionException(NoSessionException exc) {
        log.info("$ onNoSessionException() called with: exc = [{}]", exc);

        return new ResponseEntity<String>("No session found", null, 404);
    }

    public ResponseEntity<String> onRestException(Exception exc) {
        log.info("$ onRestException() called with: exc = [{}]", exc);

        return new ResponseEntity<String>(exc.getMessage(), null, 400);
    }
}
