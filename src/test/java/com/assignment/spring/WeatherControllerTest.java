package com.assignment.spring;

import com.assignment.spring.api.Main;
import com.assignment.spring.api.Sys;
import com.assignment.spring.api.WeatherResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@RunWith(SpringRunner.class)
@WebMvcTest(WeatherController.class)
public class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherRepository weatherRepository;
    @MockBean
    private RestTemplate restTemplate;

    @Mock
    ResponseEntity mockResponseEntity;

    @Mock
    WeatherResponse mockWeatherResponse;

    // TODO expand
    @Test
    public void testWeatherHappyFlow() throws Exception {

        when(restTemplate.getForEntity(anyString(), eq(WeatherResponse.class))).thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(mockWeatherResponse);
        when(mockWeatherResponse.getSys()).thenReturn(mock(Sys.class));
        when(mockWeatherResponse.getMain()).thenReturn(mock(Main.class));
        when(mockWeatherResponse.getMain().getTemp()).thenReturn(22d);

        this.mockMvc.perform(get("/weather?city=London,uk")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void testWeatherReturnsErrorResponse() throws Exception {
        RestClientException restClientException = mock(RestClientException.class);
        when(restTemplate.getForEntity(anyString(), eq(WeatherResponse.class))).thenThrow(restClientException);

        this.mockMvc.perform(get("/weather?city=London,uk")).andDo(print()).andExpect(status().isInternalServerError());
    }
}
