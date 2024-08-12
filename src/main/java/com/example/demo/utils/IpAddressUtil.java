package com.example.demo.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.camel.Exchange;

import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;

public class IpAddressUtil {

    public static String getIpAddress(Exchange e) {
        return getForwarderHeader(e)
                .orElseGet(() -> getRemoteAddress(e));
    }

    private static String getRemoteAddress(Exchange e) {
        return ofNullable(e)
                .map(Exchange::getIn)
                .map(message -> message.getBody(HttpServletRequest.class))
                .map(HttpServletRequest::getRemoteAddr)
                .orElse("");
    }

    private static Optional<String> getForwarderHeader(Exchange e) {
        Predicate<String> headerPredicate = header -> header.startsWith("X-Forwarded-For")
                || header.startsWith("X-Forwarded-Host")
                || header.startsWith("X-Forwarded-Proto")
                || header.startsWith("X-Client-IP")
                || header.startsWith("X-Real-IP");

        return ofNullable(e)
                .map(Exchange::getIn)
                .flatMap(message -> message.getHeaders().keySet().stream().filter(headerPredicate).findFirst()
                        .map(header -> message.getHeader(header, String.class)));
    }
}
