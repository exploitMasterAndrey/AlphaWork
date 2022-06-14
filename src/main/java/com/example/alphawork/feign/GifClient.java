package com.example.alphawork.feign;


import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Map;

@FeignClient(name = "gif")
public interface GifClient {
    @RequestLine("GET")
    ResponseEntity<Map> getGif(URI baseUri);
}
