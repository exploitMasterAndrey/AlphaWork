package com.example.alphawork.controller;

import com.example.alphawork.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
public class MainController {
    private MainService mainService;

    @Autowired
    public MainController(MainService mainService) {
        this.mainService = mainService;
    }

    @GetMapping("/{currency}")
    public String getGif(@PathVariable("currency") String currency, Model model) {
        String gifUrl = mainService.process(currency);
        model.addAttribute("url", gifUrl);

        return "index";
    }
}
