package com.example.alphawork.service;

import com.example.alphawork.feign.GifClient;
import com.example.alphawork.feign.MoneyClient;
import feign.Feign;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

@Component
@Import(FeignClientsConfiguration.class)
public class MainService {
    private MoneyClient moneyClient;
    private GifClient gifClient;

    private LocalDate currDate;

    @Value("${feign.client.money}")
    private String moneyBaseURL;

    @Value("${money.api.id}")
    private String moneyApiID;

    @Value("${feign.client.gif}")
    private String gifBaseUrl;

    @Value("${gif.api.key}")
    private String gifApiKey;

    @Autowired
    public MainService(Encoder encoder, Decoder decoder) {
        this.moneyClient = Feign.builder().encoder(encoder).decoder(decoder).target(Target.EmptyTarget.create(MoneyClient.class));
        this.gifClient = Feign.builder().encoder(encoder).decoder(decoder).target(Target.EmptyTarget.create(GifClient.class));
        currDate = LocalDate.now();
    }

    public ResponseEntity<Map> getStatisticsToday(){
        try {
            String date = currDate.toString();
            return moneyClient.getMoneyInfo(new URI(moneyBaseURL + date + ".json?app_id=" + moneyApiID));
        }
        catch (URISyntaxException e){
            return new ResponseEntity<Map>(HttpStatus.NOT_FOUND);
        }

    }

    public ResponseEntity<Map> getStatisticsBefore(){
        try {
            String date = currDate.minusDays(1l).toString();
            return moneyClient.getMoneyInfo(new URI(moneyBaseURL + date + ".json?app_id=" + moneyApiID));
        }
        catch (URISyntaxException e){
            return new ResponseEntity<Map>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<Map> getRichGif(){
        try {
            return gifClient.getGif(new URI(gifBaseUrl + "&api_key=" + gifApiKey + "&q=rich"));
        }
        catch (URISyntaxException e){
            return new ResponseEntity<Map>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<Map> getBrokeGif(){
        try {
            return gifClient.getGif(new URI(gifBaseUrl + "&api_key=" + gifApiKey + "&q=broke"));
        }
        catch (URISyntaxException e){
            return new ResponseEntity<Map>(HttpStatus.NOT_FOUND);
        }
    }

    public String getGifUrlFromParse(ResponseEntity<Map> response){
        Map Body = response.getBody();
        ArrayList<Map> data = (ArrayList<Map>) Body.get("data");
        LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>> gifs = (LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>) data.get(new Random().nextInt(26) - 1);
        LinkedHashMap<String, LinkedHashMap<String, String>> images = (LinkedHashMap<String, LinkedHashMap<String, String>>) gifs.get("images");
        LinkedHashMap<String, String> original = (LinkedHashMap<String, String>) images.get("original");
        return original.get("url");
    }

    public Double getRateFromParse(ResponseEntity<Map> response, String currency){
        Map statisticsTodayResponseBody = response.getBody();
        LinkedHashMap<String, Double> rates = (LinkedHashMap<String, Double>) statisticsTodayResponseBody.get("rates");
        return rates.get(currency);
    }


    public String process(String currency){
        ResponseEntity<Map> statisticsTodayResponse = getStatisticsToday();
        if(statisticsTodayResponse.getStatusCode() == HttpStatus.NOT_FOUND) {return "";}

        ResponseEntity<Map> statisticsBeforeResponse = getStatisticsBefore();
        if(statisticsBeforeResponse.getStatusCode() == HttpStatus.NOT_FOUND) {return "";}

        Double valueToday = getRateFromParse(statisticsTodayResponse, currency);

        Double valueBefore = getRateFromParse(statisticsBeforeResponse, currency); //AED

        String url = "";

        if (valueToday > valueBefore) {
            ResponseEntity<Map> richGifResponse = getRichGif();
            url = getGifUrlFromParse(richGifResponse);
        }

        if(valueToday <= valueBefore){
            ResponseEntity<Map> brokeGifResponse = getBrokeGif();
            url = getGifUrlFromParse(brokeGifResponse);
        }
        return url;
    }




}
