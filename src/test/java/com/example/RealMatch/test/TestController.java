package com.example.RealMatch.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/success")
    public String success() {
        return "KAKAO LOGIN SUCCESS";
    }
}
