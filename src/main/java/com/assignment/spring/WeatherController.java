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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;

@RestController
public class WeatherController {

    private static final Log LOG = LogFactory.getLog(WeatherController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WeatherRepository weatherRepository;

    @RequestMapping("/weather")
    public WeatherEntity weather(HttpServletRequest request) {
        LOG.debug("entering weather REST method");

        String city = request.getParameter("city");

        if (city == null || city.isEmpty()) {
            LOG.error("Parameter \'city\' was missing from request.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mandatory parameter \'city\' is missing.");
        }

        String url = Constants.WEATHER_API_URL.replace("{city}", city).replace("{appid}", Constants.APP_ID);

        try {
            ResponseEntity<WeatherResponse> response = restTemplate.getForEntity(url, WeatherResponse.class);

            if (response == null ) {
                LOG.error("Response from OpenWeather service was null.");
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Response from OpenWeather service was empty");
            }

            return mapper(response.getBody());
        } catch (RestClientException restClientException) {
            LOG.error("OpenWeather service returned failure.");
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
