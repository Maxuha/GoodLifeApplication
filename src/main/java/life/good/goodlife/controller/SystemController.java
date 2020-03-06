package life.good.goodlife.controller;

import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.MessageType;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import life.good.goodlife.component.MainMenuComponent;
import life.good.goodlife.component.TelegramBotExecuteComponent;
import life.good.goodlife.component.UserHistoryComponent;
import life.good.goodlife.model.bot.User;
import life.good.goodlife.model.bot.UserHistory;
import life.good.goodlife.model.map.NearbyMain;
import life.good.goodlife.service.bot.UserService;
import life.good.goodlife.service.map.NearbyService;
import life.good.goodlife.service.weather.WeatherService;
import org.springframework.core.env.Environment;

@BotController
public class SystemController {
    private final UserHistoryComponent userHistoryComponent;
    private final MainMenuComponent mainMenuComponent;
    private final UserService userService;
    private final Environment environment;
    private final WeatherService weatherService;
    private final NearbyService nearbyService;
    private final TelegramBotExecuteComponent telegramBotExecuteComponent;

    public SystemController(UserHistoryComponent userHistoryComponent, MainMenuComponent mainMenuComponent, UserService userService, Environment environment, WeatherService weatherService, NearbyService nearbyService, TelegramBotExecuteComponent telegramBotExecuteComponent) {
        this.userHistoryComponent = userHistoryComponent;
        this.mainMenuComponent = mainMenuComponent;
        this.userService = userService;
        this.environment = environment;
        this.weatherService = weatherService;
        this.nearbyService = nearbyService;
        this.telegramBotExecuteComponent = telegramBotExecuteComponent;
    }

    @BotRequest("Главное меню")
    BaseRequest mainMenu(Long chatId) {
        userHistoryComponent.createUserHistory(userService.findByChatId(chatId).getId(), "Главное меню");
        return mainMenuComponent.showMainMenu(chatId, "", null);
    }


    @BotRequest(messageType = MessageType.LOCATION)
    BaseRequest location(Long chatId, Message message) {
        UserHistory userHistory = userHistoryComponent.findLastUserHistoryByUserId(userService.findByChatId(chatId).getId());
        userHistoryComponent.createUserHistory(userService.findByChatId(chatId).getId(), "/set_location");
        Location location = message.location();
        System.out.println(location.latitude() + " " + location.longitude());
        String response = "Локация не пригодилась.";
        if (userHistory.getCommandsId() == 10) {
            response = weatherService.weather(location);
        } else if (userHistory.getCommandsId() == 22) {
            NearbyMain nearbyMain = nearbyService.getNearbyPlaces(location, 500);
            String[] data = nearbyMain.toString().split("::");
            life.good.goodlife.model.map.Location locationPlace;
            for (int i = 0; i < nearbyMain.getResults().length; i++) {
                telegramBotExecuteComponent.sendMessage(chatId, data[i]);
                locationPlace = nearbyMain.getResults()[i].getGeometry().getLocation();
                telegramBotExecuteComponent.sendLocation(chatId, locationPlace.getLat(), locationPlace.getLng());
                response = "";
            }
        }

        return mainMenuComponent.showMainMenu(chatId, response, null);
    }

    @BotRequest(messageType = MessageType.CONTACT)
    BaseRequest setNumberPhone(Long chatId, Message message) {
        User user = userService.findByChatId(chatId);
        user.setPhone(message.contact().phoneNumber());
        userService.createUser(user);
        userHistoryComponent.createUserHistory(user.getId(), "/set_phone");
        String msg = "Введите команду /set_email и через пробел введите ваш email. Если не хотите предоставлять почту отправьте пустую команду /set_email.";
        SendMessage sendMessage = new SendMessage(chatId, msg);
        sendMessage.replyMarkup(new ReplyKeyboardRemove());
        return sendMessage;
    }
}
