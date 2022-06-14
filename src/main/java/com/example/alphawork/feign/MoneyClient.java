package com.example.alphawork.feign;

import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Map;

@FeignClient(name = "money")
public interface MoneyClient {
    @RequestLine("GET")
    ResponseEntity<Map> getMoneyInfo(URI baseUri);
}
