package life.good.goodlife.controller;

import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import life.good.goodlife.component.TelegramBotExecuteComponent;
import life.good.goodlife.model.bot.User;
import life.good.goodlife.model.buttons.Buttons;
import life.good.goodlife.model.monobonk.Account;
import life.good.goodlife.model.monobonk.Balance;
import life.good.goodlife.model.monobonk.UserInfo;
import life.good.goodlife.model.monobonk.UserMonobank;
import life.good.goodlife.service.bot.*;
import life.good.goodlife.service.monobank.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@BotController
public class MonoBankController {
    private static Logger logger = LoggerFactory.getLogger(MonoBankController.class);
    private final UserHistoryService userHistoryService;
    private final UserService userService;
    private final CurrencyService currencyService;
    private final BalanceService balanceService;
    private final CommandService commandService;
    private final LoginService loginService;
    private final TelegramBotExecuteComponent telegramBotExecuteComponent;
    private final StatementService statementService;

    public MonoBankController(UserHistoryService userHistoryService, UserService userService,
                              CurrencyService currencyService, BalanceService balanceService, CommandService commandService,
                              LoginService loginService, TelegramBotExecuteComponent telegramBotExecuteComponent, StatementService statementService) {
        this.userHistoryService = userHistoryService;
        this.userService = userService;
        this.currencyService = currencyService;
        this.balanceService = balanceService;
        this.commandService = commandService;
        this.loginService = loginService;
        this.telegramBotExecuteComponent = telegramBotExecuteComponent;
        this.statementService = statementService;
    }

    @BotRequest("/currency")
    BaseRequest getCurrency(Long chatId) {
        return showCurrency(chatId);
    }

    @BotRequest("Курс валют")
    BaseRequest getCurrencyBtn(Long chatId) {
        return showCurrency(chatId);
    }

    @BotRequest("/balance")
    BaseRequest getBalance(Long chatId) {
        return showChooseCart(chatId);
    }

    @BotRequest("Мой баланс")
    BaseRequest getBalanceBtn(Long chatId) {
        return showChooseCart(chatId);
    }

    @BotRequest("Банкинг")
    BaseRequest bankBtn(Long chatId) {
        User user = userService.findByChatId(chatId);
        userHistoryService.createUserHistory(user.getId(), "Банкинг", "");
        String token = loginService.getToken(user.getId());
        String msg = "Перейдите по адресу https://api.monobank.ua/ и скопируйте токен, вставьте его командой /set_mono_token {token}";
        SendMessage sendMessage = new SendMessage(chatId, msg);
        if (token == null) {
            return sendMessage;
        }
        return showMonoBankMenu(chatId);
    }

    @BotRequest("/set_mono_token **")
    BaseRequest setToken(Long chatId, String text) {
        userHistoryService.createUserHistory(userService.findByChatId(chatId).getId(), "/set_mono_token", "");
        String token = text.split(" ")[1];
        logger.info("Creating user monobank");
        UserInfo userInfo = loginService.getUserInfo(token);
        UserMonobank userMonobank = new UserMonobank();
        userMonobank.setUserId(userService.findByChatId(chatId).getId());
        userMonobank.setToken(token);
        userMonobank.setClientId(userInfo.getClientId());
        userMonobank.setName(userInfo.getName());
        loginService.createUser(userMonobank);
        Account[] accounts = userInfo.getAccounts();
        for (Account account : accounts) {
            account.setClientId(userInfo.getClientId());
            loginService.createAccount(account);
        }
        return showMonoBankMenu(chatId);
    }
    @BotRequest("Синхроннизация выписки")
    BaseRequest synchronizeBtn(Long chatId) {
        User user = userService.findByChatId(chatId);
        userHistoryService.createUserHistory(user.getId(), "Синхроннизация выписки", "");
        String token = loginService.getToken(user.getId());
        String msg = "Синхроннизация...";
        telegramBotExecuteComponent.sendMessage(new SendMessage(chatId, msg));
        String accountId = "cF0-POVN4umkmK1vtoPXzw";
        Long seconds = statementService.getLastTimeByAccountId(accountId);
        if (seconds == null || seconds == 0) {
            seconds = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        }
        statementService.createStatementList(token, accountId, seconds);
        msg = "Синхроннизация успешна";
        return new SendMessage(chatId, msg);
    }

    @BotRequest("Карта **")
    BaseRequest chooseCartBtn(Long chatId, String text) {
        StringBuffer cart = new StringBuffer();
        String[] result = text.split(" ");
        for (int i = 3; i < 7; i++) {
            cart.append(result[i]);
        }
        cart.delete(6, 11);
        System.out.println(cart.toString());
        return showBalance(chatId, cart.toString());
    }

    private SendMessage showMonoBankMenu(Long chatId) {
        logger.info("Opening monobank menu");
        String msg = commandService.findCommandsByName("Банкинг").getFullDescription();
        SendMessage sendMessage = new SendMessage(chatId, msg);
        Keyboard replayKeyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[] {
                        new KeyboardButton("Мой баланс"),
                        new KeyboardButton("Курс валют"),
                        new KeyboardButton("Выписка"),
                        new KeyboardButton("Контроль расходами"),
                        new KeyboardButton("Синхроннизация выписки"),
                        new KeyboardButton("Главное меню")
                }
        );
        sendMessage.replyMarkup(replayKeyboard);
        return sendMessage;
    }

    private SendMessage showChooseCart(Long chatId) {
        User user = userService.findByChatId(chatId);
        UserMonobank userMonobank = loginService.getByUserId(user.getId());
        List<Account> accounts = loginService.getAllAccountByClientId(userMonobank.getClientId());
        String[][] accountButtons = new String[accounts.size()][1];
        int index = 0;
        StringBuffer cart;
        for (Account account : accounts) {
            for (int i = 0; i < account.getMaskedPan().length; i++) {
                cart = new StringBuffer(account.getMaskedPan()[i]);
                cart.replace(6, 6, "*****");
                cart.insert(4, " ");
                cart.insert(9, " ");
                cart.insert(14, " ");
                accountButtons[index][0] = "Карта " + account.getType() + " " + account.getCurrencyCode() + " " + cart.toString();
                index++;
            }
        }
        Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(accountButtons).resizeKeyboard(true);
        SendMessage sendMessage = new SendMessage(chatId, "Выбери карту");
        sendMessage.replyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }

    private SendMessage showBalance(Long chatId, String cart) {
        User user = userService.findByChatId(chatId);
        userHistoryService.createUserHistory(user.getId(), "/balance", "");
        Account account = balanceService.getBalance(new String[] {cart});
        String result = "<b>Мой баланс: </b>\n\n" + "Карта: " + cart + "\n" +
                "Тип: " + account.getType() + "\n" +
                "Баланс: " + Balance.getBalanceFactory(account.getBalance(), account.getCurrencyCode()) + "\n" +
                "Кредитный лимит: " + Balance.getBalanceFactory(account.getCreditLimit(), account.getCurrencyCode()) + "\n";
        return new SendMessage(chatId, result).parseMode(ParseMode.HTML).disableWebPagePreview(true);
    }

    private SendMessage showCurrency(Long chatId) {
        User user = userService.findByChatId(chatId);
        userHistoryService.createUserHistory(user.getId(), "/currency", "");
        String msg = currencyService.currency(loginService.getToken(user.getId()));
        return new SendMessage(chatId, msg).parseMode(ParseMode.HTML).disableWebPagePreview(true);
    }
}
