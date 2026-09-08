package com.pecucore.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatusController {

    @GetMapping("/")
    public String status() {
        return "status";
    }
}
