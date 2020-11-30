package com.assignment.spring;

import com.assignment.spring.api.Main;
import com.assignment.spring.api.Sys;
import com.assignment.spring.api.WeatherResponse;
import com.assignment.spring.config.OpenWeatherProperties;
import com.assignment.spring.controller.WeatherController;
import com.assignment.spring.model.WeatherEntity;
import com.assignment.spring.repository.WeatherRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(WeatherController.class)
@AutoConfigureMockMvc
public class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherRepository weatherRepository;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private OpenWeatherProperties openWeatherProperties;

    @Test
    public void testOpenWeatherResponseJSONEmptyResultsInErrorResponse() throws Exception {
        setupOpenWeatherPropertiesMock();
        WeatherResponse weatherResponse = null;

        ResponseEntity<WeatherResponse> responseEntity = new ResponseEntity<>(weatherResponse, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(WeatherResponse.class))).thenReturn(responseEntity);

        this.mockMvc.perform(get("/weather?city=London,uk")).andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testWeatherHappyFlow() throws Exception {
        setupOpenWeatherPropertiesMock();
        WeatherResponse weatherResponse = createWeatherResponseToBeReturnedByOpenWeatherServiceMock();

        /*
         * Because the weatherRepository is a mock, set it up to return
         * a WeatherEntity with the expected values. We will capture the
         * actually created weatherEntity and compare it to this entity later.
         */
        WeatherEntity weatherEntity = createWeatherEntityToBeReturnedByWeatherRepositoryMock();

        // Set up responseEntity and make restTemplate return it
        //@SuppressWarnings({"unchecked", "raw"})
        ResponseEntity<WeatherResponse> responseEntity = new ResponseEntity<>(weatherResponse, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(WeatherResponse.class))).thenReturn(responseEntity);

        // set up weather repository to return the created weatherEntity
        when(weatherRepository.save(any(WeatherEntity.class))).thenReturn(weatherEntity);

        // call controller REST method and verify json result
        this.mockMvc.perform(get("/weather?city=London,uk")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.city", is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.city", is("Almere Stad")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.country", is(notNullValue())))
            .andExpect(MockMvcResultMatchers.jsonPath("$.country", is("NL")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.temperature", is(notNullValue())))
            .andExpect(MockMvcResultMatchers.jsonPath("$.temperature", is(220.3)));

        // Check if the WeatherEntity created in the mapper() method of the controller contains the expected values.
        ArgumentCaptor<WeatherEntity> weatherEntityArgumentCaptor = ArgumentCaptor.forClass(WeatherEntity.class);
        verify(weatherRepository).save(any(WeatherEntity.class));
        verify(weatherRepository).save(weatherEntityArgumentCaptor.capture());
        assertWeatherEntitiesEqual(weatherEntity, weatherEntityArgumentCaptor.getValue());
    }

    private void setupOpenWeatherPropertiesMock() {
        when(openWeatherProperties.getEndpointUrl()).thenReturn("http://dummy.endpoint.com");
        when(openWeatherProperties.getApiKey()).thenReturn("dummy-api-key");
    }

    private WeatherEntity createWeatherEntityToBeReturnedByWeatherRepositoryMock() {
        WeatherEntity weatherEntity = new WeatherEntity();
        weatherEntity.setId(1);
        weatherEntity.setCity("Almere Stad");
        weatherEntity.setCountry("NL");
        weatherEntity.setTemperature(220.3d);
        return weatherEntity;
    }

    private WeatherResponse createWeatherResponseToBeReturnedByOpenWeatherServiceMock() {
        WeatherResponse weatherResponse = new WeatherResponse();
        weatherResponse.setName("Almere Stad");
        weatherResponse.setSys(new Sys());
        weatherResponse.getSys().setCountry("NL");
        weatherResponse.setMain(new Main());
        weatherResponse.getMain().setTemp(220.3d);
        return weatherResponse;
    }

    /**
     * Compare city, country and temperature, but not the id.
     *
     * @param weatherEntityA weather entity
     * @param weatherEntityB weather entity to compare to.
     */
    private void assertWeatherEntitiesEqual(WeatherEntity weatherEntityA, WeatherEntity weatherEntityB) {
        assertEquals(weatherEntityA.getCity(), weatherEntityB.getCity());
        assertEquals(weatherEntityA.getCountry(), weatherEntityB.getCountry());
        assertEquals(weatherEntityA.getTemperature(), weatherEntityB.getTemperature());
    }

    @Test
    public void testRestClientExceptionIsConvertedToErrorResponse() throws Exception {
        setupOpenWeatherPropertiesMock();
        RestClientException restClientException = mock(RestClientException.class);
        when(restTemplate.getForEntity(anyString(), eq(WeatherResponse.class))).thenThrow(restClientException);

        this.mockMvc.perform(get("/weather?city=London,uk")).andDo(print()).andExpect(status().isInternalServerError());
    }

    @Test
    public void testCityParameterMIssingReturnsErrorResponse() throws Exception {
        this.mockMvc.perform(get("/weather")).andExpect(status().isBadRequest());
    }

    @Test
    public void testCityParameterValueMIssingReturnsErrorResponse() throws Exception {
        setupOpenWeatherPropertiesMock();
        this.mockMvc.perform(get("/weather?city=")).andExpect(status().isBadRequest());
    }
}
