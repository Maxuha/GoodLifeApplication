package life.good.goodlife.controller;

import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import life.good.goodlife.component.MainMenuComponent;
import life.good.goodlife.model.bot.Command;
import life.good.goodlife.model.bot.User;
import life.good.goodlife.model.bot.UserHistory;
import life.good.goodlife.service.bot.CommandService;
import life.good.goodlife.service.bot.UserHistoryService;
import life.good.goodlife.service.bot.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@BotController
public class CreateUserController {
    private static final Logger logger = LoggerFactory.getLogger(CreateUserController.class);
    private final UserService userService;
    private final UserHistoryService userHistoryService;
    private final MainMenuComponent mainMenuComponent;
    private final CommandService commandService;

    public CreateUserController(UserService userService, UserHistoryService userHistoryService,
                                MainMenuComponent mainMenuComponent, CommandService commandService) {
        this.userService = userService;
        this.userHistoryService = userHistoryService;
        this.mainMenuComponent = mainMenuComponent;
        this.commandService = commandService;
    }

    @BotRequest("/start")
    BaseRequest register(Long chatId) {
        logger.info("Finding user by chatId: {}", chatId);
        User user = userService.findByChatId(chatId);
        if (user == null) {
            logger.info("User {} creating (Part 'chatId'): ", chatId);
            user = new User();
            user.setChatId(chatId);
            user.setFullName("anonymous");
            user.setDate(LocalDateTime.now());
            userService.createUser(user);
        }
        userHistoryService.createUserHistory(user.getId(), "/start", "");
        String msg = "Привет! Выберете способ регистрации";
        SendMessage sendMessage = new SendMessage(chatId, msg);
        Keyboard replayKeyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[] {
                        new KeyboardButton("Телеграм").requestContact(true),
                        new KeyboardButton("Быстрая регистрация"),
                        new KeyboardButton("Анонимность")
                }
        );
        sendMessage.replyMarkup(replayKeyboard);
        logger.info("Sending message: {}", msg);
        return sendMessage;
    }

    @BotRequest("Телеграм")
    BaseRequest telegramRegister(Long chatId, Message message) {
        logger.info("Finding user by chatId: {}", chatId);
        User user = userService.findByChatId(chatId);
        userHistoryService.createUserHistory(user.getId(), "Телеграм", "");
        logger.info("User {} creating: ", chatId);
        user.setDate(LocalDateTime.now());
        user.setPhone(message.contact().phoneNumber());
        user.setFullName(message.contact().firstName() + " " + message.contact().lastName());
        user.setChatId(chatId);
        userService.createUser(user);
        return lastStepRegister(chatId);
    }

    @BotRequest("Анонимность")
    BaseRequest anonymousRegister(Long chatId) {
        logger.info("Finding user by chatId: {}", chatId);
        User user = userService.findByChatId(chatId);
        userHistoryService.createUserHistory(user.getId(), "Анонимность", "");
        String msg = "Вы уверенны, что хотите быть анонимом?\n" +
                "В любом случае вы всегда сможете зарегистрироваться, посмотрев команды /help";
        SendMessage sendMessage = new SendMessage(chatId, msg);
        Keyboard replayKeyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[] {
                        new KeyboardButton("Продолжить"),
                        new KeyboardButton("Отмена")
                }
        );
        sendMessage.replyMarkup(replayKeyboard);
        logger.info("Sending message: {}", msg);
        return sendMessage;
    }

    @BotRequest("Быстрая регистрация")
    BaseRequest quickRegister(Long chatId) {
        userHistoryService.createUserHistory(userService.findByChatId(chatId).getId(), "Быстрая регистрация", "");
        String msg = "Введите команду /set_name и через пробел введите ваше имя. ";
        SendMessage sendMessage = new SendMessage(chatId, msg);
        sendMessage.replyMarkup(new ReplyKeyboardRemove());
        logger.info("Sending message: {}", msg);
        return sendMessage;
    }

    @BotRequest(path = "/set_name **")
    BaseRequest setName(String text, Long chatId) {
        userHistoryService.createUserHistory(userService.findByChatId(chatId).getId(), "/set_name", "");
        StringBuilder name = new StringBuilder();
        String[] results = text.split(" ");
        for (int i = 1; i < results.length; i++) {
            name.append(results[i]).append(" ");
        }
        String nameResult = name.toString().trim();
        logger.info("Finding user by chatId: {}", chatId);
        User user = userService.findByChatId(chatId);
        logger.info("User {} creating (Part 'name'): ", chatId);
        user.setFullName(nameResult);
        userService.createUser(user);
        String msg = nameResult + ", введите команду /set_phone и через пробел введите ваш номер телефона.";
        msg += "\nИли нажмите на 'Предоставить номер'. Если не хотите предоставлять номер отправьте пустую команду /set_phone.";
        SendMessage sendMessage = new SendMessage(chatId, msg);
        Keyboard replayKeyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[] {
                        new KeyboardButton("Предоставить номер").requestContact(true),
                        new KeyboardButton("Отмена")
                }
        );
        sendMessage.replyMarkup(replayKeyboard);
        logger.info("Sending message: {}", msg);
        return sendMessage;
    }

    @BotRequest("/set_phone **")
    BaseRequest setNumberPhone(Long chatId, String text) {
        logger.info("Finding user by chatId: {}", chatId);
        User user = userService.findByChatId(chatId);
        user.setPhone(text.split(" ")[1]);
        userHistoryService.createUserHistory(user.getId(), "/set_phone", "");
        String msg = "Введите команду /set_email и через пробел введите ваш email. Если не хотите предоставлять почту" +
                " отправьте пустую команду /set_email.";
        SendMessage sendMessage = new SendMessage(chatId, msg);
        sendMessage.replyMarkup(new ReplyKeyboardRemove());
        logger.info("Sending message: {}", msg);
        return sendMessage;
    }

    @BotRequest("/set_phone")
    BaseRequest setNumberPhoneEmpty(Long chatId) {
        logger.info("Canceled /set_phone");
        logger.info("Finding user by chatId: {}", chatId);
        User user = userService.findByChatId(chatId);
        userHistoryService.createUserHistory(user.getId(), "/set_phone", "");
        String msg = "Введите команду /set_email и через пробел введите ваш email. Если не хотите предоставлять почту " +
                "отправьте пустую команду /set_email.";
        SendMessage sendMessage = new SendMessage(chatId, msg);
        sendMessage.replyMarkup(new ReplyKeyboardRemove());
        logger.info("Sending message: {}", msg);
        return sendMessage;
    }

    @BotRequest("Отмена")
    BaseRequest cancel(Long chatId) {
        logger.info("Finding user by chatId: {}", chatId);
        User user = userService.findByChatId(chatId);
        logger.info("Finding last history by user: {}", chatId);
        UserHistory userHistory = userHistoryService.findLastUserHistoryByUserId(user.getId());
        String msg = "";
        if (userHistory.getCommandsId() == 3) {
            msg = "Введите команду /set_email и через пробел введите ваш email. Если не хотите предоставлять почту " +
                    "отправьте пустую команду /set_email.";
        } else if (userHistory.getCommandsId() == 7) {
            return register(chatId);
        }
        userHistoryService.createUserHistory(user.getId(), "Отмена", "");
        SendMessage sendMessage = new SendMessage(chatId, msg);
        sendMessage.replyMarkup(new ReplyKeyboardRemove());
        logger.info("Sending message: {}", msg);
        return sendMessage;
    }

    @BotRequest("Продолжить")
    BaseRequest next(Long chatId) {
        logger.info("Finding user by chatId: {}", chatId);
        User user = userService.findByChatId(chatId);
        logger.info("Finding last history by user: {}", chatId);
        UserHistory userHistory = userHistoryService.findLastUserHistoryByUserId(user.getId());
        String msg = "";
        SendMessage sendMessage = new SendMessage(chatId, msg);
        sendMessage.replyMarkup(new ReplyKeyboardRemove());
        if (userHistory.getCommandsId() == 7) {
            sendMessage = lastStepRegister(chatId);
        }
        userHistoryService.createUserHistory(user.getId(), "Продолжить", "");
        return sendMessage;
    }

    @BotRequest("/set_email **")
    BaseRequest setEmail(Long chatId, String text) {
        logger.info("Finding user by chatId: {}", chatId);
        User user = userService.findByChatId(chatId);
        logger.info("Finding last history by user: {}", chatId);
        UserHistory userHistory = userHistoryService.findLastUserHistoryByUserId(user.getId());
        userHistoryService.createUserHistory(user.getId(), "/set_email", "");
        if (text.split(" ").length == 2) {
            user.setEmail(text.split(" ")[1]);
            userService.createUser(user);
        }
        if (userHistory.getCommandsId() == 4) {
            return lastStepRegister(chatId);
        } else {
            return mainMenuComponent.showMainMenu(chatId, "", null);
        }
    }

    @BotRequest("/set_email")
    BaseRequest setEmailEmpty(Long chatId) {
        logger.info("Canceled /set_email");
        logger.info("Finding user by chatId: {}", chatId);
        User user = userService.findByChatId(chatId);
        logger.info("Finding last history by user: {}", chatId);
        UserHistory userHistory = userHistoryService.findLastUserHistoryByUserId(user.getId());
        userHistoryService.createUserHistory(user.getId(), "/set_email", "");
        if (userHistory.getCommandsId() == 4) {
            return lastStepRegister(chatId);
        } else {
            return mainMenuComponent.showMainMenu(chatId, "", null);
        }
    }

    SendMessage lastStepRegister(Long chatId) {
        logger.info("Opening main menu");
        return mainMenuComponent.showMainMenu(chatId, "Спасибо за регистрацию. Начнём.", null);
    }
}
