package life.good.goodlife.controller;

import life.good.goodlife.component.MainMenuComponent;
import life.good.goodlife.component.TelegramBotExecuteComponent;
import life.good.goodlife.service.bot.UserService;
import life.good.goodlife.service.monobank.WebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "webhook/")
public class WebhookController {
    private final TelegramBotExecuteComponent telegramBotExecuteComponent;
    private final UserService userService;
    private final WebhookService webhookService;
    private final MainMenuComponent mainMenuComponent;

    public WebhookController(TelegramBotExecuteComponent telegramBotExecuteComponent, UserService userService, WebhookService webhookService, MainMenuComponent mainMenuComponent) {
        this.telegramBotExecuteComponent = telegramBotExecuteComponent;
        this.userService = userService;
        this.webhookService = webhookService;
        this.mainMenuComponent = mainMenuComponent;
    }

    @RequestMapping(path = "test/get", method = RequestMethod.GET)
    public ResponseEntity <?> testGet() {
        return ResponseEntity.ok("get");
    }

    @RequestMapping(path = "test/post", method = RequestMethod.POST)
    public ResponseEntity <?> testPost() {
        return ResponseEntity.ok("post");
    }

    @RequestMapping(path = "monobank", method = RequestMethod.POST)
    public ResponseEntity <?> monobank(@RequestBody String raw, @RequestHeader("Content-Type") String type) {
        String info = webhookService.createOperation(raw);
        Map<String, String> map = new HashMap<>();
        map.put("Monobank", webhookService.getBalance());
        mainMenuComponent.showMainMenu(userService.findById(1).getChatId(), info, null);
        return ResponseEntity.ok("ok");
    }

}
