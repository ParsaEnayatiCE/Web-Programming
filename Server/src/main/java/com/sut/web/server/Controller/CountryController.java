package com.sut.web.server.Controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.sut.web.server.Model.AllCountriesRequest;
import com.sut.web.server.Model.AllCountriesResponse;
import com.sut.web.server.Model.CountryRequest;
import com.sut.web.server.Model.CountryResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/countries")
public class CountryController {



    @Value("${x.api.key}")
    private String xApiKey;

    private boolean checkAuthorization(String tokenCookie){
        ResponseEntity<String> resp = restTemplate.postForEntity("http://localhost:8080",tokenCookie, String.class);
        return resp.getStatusCode() == HttpStatus.OK;
    }

    private final RestTemplate restTemplate;
    @Autowired
    public CountryController(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Cacheable(value = "countries")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<AllCountriesResponse> getList(@CookieValue(name = "token") String token) {
        if (!checkAuthorization(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        //TODO check token expiry time
        ResponseEntity<AllCountriesRequest> res = restTemplate.getForEntity("https://countriesnow.space/api/v0.1/countries", AllCountriesRequest.class);
        AllCountriesRequest allCountriesRequest = res.getBody();
        AllCountriesResponse allCountriesResponse = new AllCountriesResponse();
        allCountriesResponse.setCountries(new ArrayList<>());
        for (CountryRequest countryRequest : allCountriesRequest.getData()) {
            CountryResponse countryResponse = new CountryResponse(countryRequest.getName());
            allCountriesResponse.getCountries().add(countryResponse);
        }
        allCountriesResponse.setCount(allCountriesRequest.getData().size());
        return ResponseEntity.ok(allCountriesResponse);
    }

    @Cacheable(value = "countryInfo", key = "#name")
    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getCountryInfo(@PathVariable(value="name") String name, @CookieValue(name = "token") String token) throws JsonProcessingException {
        if (!checkAuthorization(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        //TODO check token expiry time
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("X-Api-Key", xApiKey);
        ResponseEntity<String> res = restTemplate.exchange(
                "https://api.api-ninjas.com/v1/country?name="+name, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);
        JSONArray jsonArray = new JSONArray(res.getBody());
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        JSONObject result = new JSONObject();
        result.put("name", jsonObject.getString("name"));
        result.put("capital", jsonObject.getString("capital"));
        result.put("iso2", jsonObject.getString("iso2"));
        result.put("population", jsonObject.getDouble("population"));
        result.put("pop_growth", jsonObject.getDouble("pop_growth"));
        result.put("currency", jsonObject.getJSONObject("currency"));
        return ResponseEntity.ok(result.toMap());
    }


    @Cacheable(value = "weatherInfo", key = "#name")
    @RequestMapping(value = "/{name}/weather",method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getCountryWeatherInfo(@PathVariable(value="name") String name, @CookieValue(name = "token") String token) throws JsonProcessingException {
        if (!checkAuthorization(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        //TODO check token expiry time
        String getCapital = getCountryInfo(name, token).getBody().get("capital").toString();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("X-Api-Key", xApiKey);
        ResponseEntity<String> res = restTemplate.exchange(
                "https://api.api-ninjas.com/v1/weather?city="+getCapital, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);
        JSONObject jsonObject = new JSONObject(res.getBody());
        JSONObject result = new JSONObject();
        result.put("country_name", name);
        result.put("capital", getCapital);
        result.put("wind_speed", jsonObject.getDouble("wind_speed"));
        result.put("wind_degrees", jsonObject.getInt("wind_degrees"));
        result.put("temp", jsonObject.getInt("temp"));
        result.put("humidity", jsonObject.getInt("humidity"));
        return ResponseEntity.ok(result.toMap());
    }




}
