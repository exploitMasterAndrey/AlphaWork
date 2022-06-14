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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

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

    public ResponseEntity<Map> getStatisticsToday() throws URISyntaxException{
        String date = currDate.toString();
        return moneyClient.getMoneyInfo(new URI(moneyBaseURL + date + ".json?app_id=" + moneyApiID));
    }

    public ResponseEntity<Map> getStatisticsBefore() throws URISyntaxException{
        String date = currDate.minusDays(1l).toString();
        return moneyClient.getMoneyInfo(new URI(moneyBaseURL + date + ".json?app_id=" + moneyApiID));
    }

    public String process(String currency) throws URISyntaxException{
        ResponseEntity<Map> statisticsTodayResponse = getStatisticsToday();
        ResponseEntity<Map> statisticsBeforeResponse = getStatisticsBefore();

        Map statisticsTodayResponseBody = statisticsTodayResponse.getBody();
        LinkedHashMap<String, Double> linkedHashMapToday = (LinkedHashMap<String, Double>) statisticsTodayResponseBody.get("rates");
        Double valueToday = linkedHashMapToday.get(currency);

        Map statisticsBeforeResponseBody = statisticsBeforeResponse.getBody();
        LinkedHashMap<String, Double> linkedHashMapBefore = (LinkedHashMap<String, Double>) statisticsBeforeResponseBody.get("rates");
        Double valueBefore = (Double) linkedHashMapBefore.get(currency); //AED

        String url = "";

        if (valueToday > valueBefore) {
            ResponseEntity<Map> brokeGifResponse = gifClient.getGif(new URI(gifBaseUrl + "&api_key=" + gifApiKey + "&q=rich"));
            Map brokeGifBody = brokeGifResponse.getBody();
            ArrayList<Map> data = (ArrayList<Map>) brokeGifBody.get("data");
            LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>> linkedHashMapRichGif = (LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>) data.get(0);
            LinkedHashMap<String, LinkedHashMap<String, String>> images = (LinkedHashMap<String, LinkedHashMap<String, String>>) linkedHashMapRichGif.get("images");
            LinkedHashMap<String, String> original = (LinkedHashMap<String, String>) images.get("original");

            url = original.get("url");

        }

        if(valueToday <= valueBefore){
            ResponseEntity<Map> brokeGifResponse = gifClient.getGif(new URI(gifBaseUrl + "&api_key=" + gifApiKey + "&q=broke"));
            Map brokeGifBody = brokeGifResponse.getBody();
            ArrayList<Map> data = (ArrayList<Map>) brokeGifBody.get("data");
            LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>> linkedHashMapBrokeGif = (LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>) data.get(0);
            LinkedHashMap<String, LinkedHashMap<String, String>> images = (LinkedHashMap<String, LinkedHashMap<String, String>>) linkedHashMapBrokeGif.get("images");
            LinkedHashMap<String, String> original = (LinkedHashMap<String, String>) images.get("original");

            url = original.get("url");
        }
        return url;
    }




}
