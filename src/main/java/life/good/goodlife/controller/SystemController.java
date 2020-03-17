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
import life.good.goodlife.component.MonoBankComponent;
import life.good.goodlife.component.TelegramBotExecuteComponent;
import life.good.goodlife.model.bot.User;
import life.good.goodlife.model.bot.UserHistory;
import life.good.goodlife.model.map.NearbyMain;
import life.good.goodlife.service.bot.CommandService;
import life.good.goodlife.service.bot.UserHistoryService;
import life.good.goodlife.service.bot.UserService;
import life.good.goodlife.service.map.NearbyServiceImpl;
import life.good.goodlife.service.weather.WeatherServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@BotController
public class SystemController {
    private static Logger logger = LoggerFactory.getLogger(SystemController.class);
    private final UserHistoryService userHistoryService;
    private final MainMenuComponent mainMenuComponent;
    private final UserService userService;
    private final WeatherServiceImpl weatherService;
    private final NearbyServiceImpl nearbyService;
    private final CommandService commandService;
    private final TelegramBotExecuteComponent telegramBotExecuteComponent;
    private final MonoBankComponent monoBankComponent;

    public SystemController(UserHistoryService userHistoryService, MainMenuComponent mainMenuComponent,
                            UserService userService, WeatherServiceImpl weatherService, NearbyServiceImpl nearbyService,
                            CommandService commandService, TelegramBotExecuteComponent telegramBotExecuteComponent, MonoBankComponent monoBankComponent) {
        this.userHistoryService = userHistoryService;
        this.mainMenuComponent = mainMenuComponent;
        this.userService = userService;
        this.weatherService = weatherService;
        this.nearbyService = nearbyService;
        this.commandService = commandService;
        this.telegramBotExecuteComponent = telegramBotExecuteComponent;
        this.monoBankComponent = monoBankComponent;
    }

    @BotRequest("Главное меню")
    BaseRequest mainMenu(Long chatId) {
        userHistoryService.createUserHistory(userService.findByChatId(chatId).getId(), "Главное меню", "");
        return mainMenuComponent.showMainMenu(chatId, "", null);
    }

    @BotRequest("Назад")
    BaseRequest back(Long chatId) {
        logger.info("Finding user by chatId: {}", chatId);
        User user = userService.findByChatId(chatId);
        logger.info("Finding last history by user: {}", chatId);
        UserHistory userHistory = userHistoryService.findLastUserHistoryByUserId(user.getId());
        SendMessage sendMessage = null;
        if (userHistory.getCommandsId() == 28) {
            sendMessage = monoBankComponent.showMonoBankMenu(chatId);
        }
        userHistoryService.createUserHistory(userService.findByChatId(chatId).getId(), "Назад", "");
        return sendMessage;
    }


    @BotRequest(messageType = MessageType.LOCATION)
    BaseRequest location(Long chatId, Message message) {
        logger.info("Finding user by chatId: {}", chatId);
        User user = userService.findByChatId(chatId);
        logger.info("Finding last history by user: {}", chatId);
        UserHistory userHistory = userHistoryService.findLastUserHistoryByUserId(user.getId());
        userHistoryService.createUserHistory(user.getId(), "/set_phone", "");
        Location location = message.location();
        String response = "Локация не пригодилась.";
        if (userHistory.getCommandsId() == 10) {
            logger.info("Get weather");
            response = weatherService.weather(location.latitude(), location.longitude(), user.getId());
        } else if (userHistory.getCommandsId() == 22) {
            logger.info("Get nearby places");
            NearbyMain nearbyMain = nearbyService.getNearbyPlaces(location, 500, user.getId());
            String[] data = nearbyMain.toString().split("::");
            life.good.goodlife.model.map.Location locationPlace;
            for (int i = 0; i < nearbyMain.getResults().length; i++) {
                telegramBotExecuteComponent.sendMessage(new SendMessage(chatId, data[i]));
                locationPlace = nearbyMain.getResults()[i].getGeometry().getLocation();
                telegramBotExecuteComponent.sendLocation(chatId, locationPlace.getLat(), locationPlace.getLng());
                response = "";
            }
        }
        return mainMenuComponent.showMainMenu(chatId, response, null);
    }

    @BotRequest(messageType = MessageType.CONTACT)
    BaseRequest setNumberPhone(Long chatId, Message message) {
        logger.info("Creating user {} (part: /set_phone)...", chatId);
        User user = userService.findByChatId(chatId);
        user.setPhone(message.contact().phoneNumber());
        userService.createUser(user);
        UserHistory userHistory = userHistoryService.findLastUserHistoryByUserId(user.getId());
        userHistoryService.createUserHistory(user.getId(), "/set_phone", "");
        String msg = "Введите команду /set_email и через пробел введите ваш email. Если не хотите предоставлять почту " +
                "отправьте пустую команду /set_email.";
        SendMessage sendMessage = new SendMessage(chatId, msg);
        sendMessage.replyMarkup(new ReplyKeyboardRemove());
        logger.info("Sending message: {}", msg);
        return sendMessage;
    }
}
