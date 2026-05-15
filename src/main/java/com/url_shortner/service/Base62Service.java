package com.url_shortner.service;

import org.springframework.stereotype.Service;

@Service
public class Base62Service {
    public static final String CHARACTERS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final long OFFSET = 100_000L;

    public String encode(Long id){
        long value = id + OFFSET;
        StringBuilder result = new StringBuilder();
        while(value > 0){
            result.append(CHARACTERS.charAt((int)(id % 62)));
            value /= 62;
        }
        return result.reverse().toString();
    }
}
