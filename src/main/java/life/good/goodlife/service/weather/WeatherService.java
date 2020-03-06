package life.good.goodlife.service.weather;

import com.google.gson.Gson;
import com.pengrad.telegrambot.model.Location;
import life.good.goodlife.component.LocationComponent;
import life.good.goodlife.model.map.LocationType;
import life.good.goodlife.model.weather.CurrentWeather;
import life.good.goodlife.model.weather.ForecastWeather;
import life.good.goodlife.repos.map.LocationRepository;
import life.good.goodlife.statics.Request;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {
    private String token;
    private final LocationComponent locationComponent;
    public WeatherService(LocationComponent locationComponent) {
        this.locationComponent = locationComponent;
        token = System.getenv().get("weather_openweather_token");
    }

    public String weatherByCity(String city) {
        String data = Request.get("http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + token + "&lang=ru");
        Gson gson = new Gson();
        CurrentWeather currentWeather = gson.fromJson(data, CurrentWeather.class);
        return currentWeather.toString();
    }

    public String weather(Location location, Long userId) {
        String data = Request.get("http://api.openweathermap.org/data/2.5/weather?lat=" + location.latitude() + "&lon=" + location.longitude() + "&appid=" + token + "&lang=ru");
        locationComponent.createNewLocation(userId, location.latitude(), location.longitude(), LocationType.CURRENT);
        Gson gson = new Gson();
        CurrentWeather currentWeather = gson.fromJson(data, CurrentWeather.class);
        return currentWeather.toString();
    }

    public String[] weatherFiveByCity(String city) {
        String data = Request.get("http://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + token + "&lang=ru");
        Gson gson = new Gson();
        ForecastWeather forecastWeather = gson.fromJson(data, ForecastWeather.class);
        data = forecastWeather.toString();
        return data.split("::");
    }
}
