Spring Boot Coding Dojo
---

Welcome to the Spring Boot Coding Dojo!

### Introduction

This is a simple application that requests its data from [OpenWeather](https://openweathermap.org/) and stores the result in a database. The current implementation has quite a few problems making it a non-production ready product.

### The task

As the new engineer leading this project, your first task is to make it production-grade, feel free to refactor any piece
necessary to achieve the goal.

### How to deliver the code

Please send an email containing your solution with a link to a public repository.

>**DO NOT create a Pull Request with your solution** 

### Footnote
It's possible to generate the API key going to the [OpenWeather Sign up](https://openweathermap.org/appid) page.

### Assignment Solution - Instructions for configuration and deployment
The '/weather' REST service uses the public OpenWeather REST API. The application needs
to know the url of the OpenWeather service, as well as the API key assigned by OpenWeather
to the application.

The application.yml file contains properties for the OpenWeather endpoint url and API key. See the following example:

    openweather:
      endpointurl: http://api.openweathermap.org/data/2.5/weather?q={city}&APPID={appid}
      apikey: <openweather api key string>
      
The application uses the default logback logging framework. The configuration for the 
logging is found in the configuration file logback-spring.xml. The logfile is named 'weather-rest-service.log' in the 
directory 'logs'. This is a rolling log file with a maximum size of 10 MB. Previous logs are
stored in the subdirectory 'archived' and are named weather-rest-service-<date>-<sequence nr>.log
