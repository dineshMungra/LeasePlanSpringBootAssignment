package com.assignment.spring.controller;

import com.assignment.spring.api.WeatherResponse;
import com.assignment.spring.config.OpenWeatherProperties;
import com.assignment.spring.model.WeatherEntity;
import com.assignment.spring.repository.WeatherRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@RestController
public class WeatherController {

    private static final Log LOG = LogFactory.getLog(WeatherController.class);

    @Autowired
    private OpenWeatherProperties openWeatherProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WeatherRepository weatherRepository;

    @RequestMapping("/weather")
    public WeatherEntity weather(HttpServletRequest request) {
        String city = getCityFromRequest(request);

        String url = openWeatherProperties.getEndpointUrl()
                .replace("{city}", city)
                .replace("{appid}", openWeatherProperties.getApiKey());

        ResponseEntity<WeatherResponse> response = performCallToWeatherServiceAndVerifyResponse(url);
        return mapper(response.getBody());
    }

    private String getCityFromRequest(HttpServletRequest request) {
        String city = request.getParameter("city");
        verifyThatCityParameterValueIsNotNullOrEmpty(city);
        return city;
    }

    private void verifyThatCityParameterValueIsNotNullOrEmpty(String city) {
        if (city == null || city.isEmpty()) {
            LOG.error("Parameter 'city' was missing from request.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mandatory parameter 'city' is missing.");
        }
    }

    private ResponseEntity<WeatherResponse> performCallToWeatherServiceAndVerifyResponse(String url) {
        ResponseEntity<WeatherResponse> response;
        response = performCallToOpenWeatherService(url);
        verifyResponseBodyNotEmpty(response);
        return response;
    }

    private ResponseEntity<WeatherResponse> performCallToOpenWeatherService(String url) {
        ResponseEntity<WeatherResponse> response;
        try {
            response = restTemplate.getForEntity(url, WeatherResponse.class);
        } catch (RestClientException restClientException) {
            LOG.error("OpenWeather service returned failure.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Call to OpenWeather service returned error", restClientException);
        }
        return response;
    }

    private void verifyResponseBodyNotEmpty(ResponseEntity<WeatherResponse> response) {
        if (response.getBody() == null) {
            LOG.error("Response from OpenWeather service contains empty JSON.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Response from OpenWeather service contains empty JSON");
        }
    }

    private WeatherEntity mapper(WeatherResponse response) {
        WeatherEntity entity = new WeatherEntity();
        entity.setCity(response.getName());
        entity.setCountry(response.getSys().getCountry());
        entity.setTemperature(response.getMain().getTemp());

        return weatherRepository.save(entity);
    }
}
