package com.assignment.spring;

import com.assignment.spring.api.WeatherResponse;
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

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WeatherRepository weatherRepository;

    @RequestMapping("/weather")
    public WeatherEntity weather(HttpServletRequest request) {
        String city = request.getParameter("city");

        if (city == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mandatory parameter \'city\' is missing.");
        }

        String url = Constants.WEATHER_API_URL.replace("{city}", city).replace("{appid}", Constants.APP_ID);

        try {
            ResponseEntity<WeatherResponse> response = restTemplate.getForEntity(url, WeatherResponse.class);

            if (response == null ) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Response from OpenWeather service was empty");
            }

            return mapper(response.getBody());
        } catch (RestClientException restClientException) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Call to OpenWeather service returned error", restClientException);
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
