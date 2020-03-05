package life.good.goodlife.controller;

import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import life.good.goodlife.component.UserHistoryComponent;
import life.good.goodlife.service.bot.UserService;
import life.good.goodlife.service.map.GeoCodeService;

@BotController
public class MapController {
    private final GeoCodeService geoCodeService;
    private final UserHistoryComponent userHistoryComponent;
    private final UserService userService;

    public MapController(GeoCodeService geoCodeService, UserHistoryComponent userHistoryComponent, UserService userService) {
        this.geoCodeService = geoCodeService;
        this.userHistoryComponent = userHistoryComponent;
        this.userService = userService;
    }

    @BotRequest("/search_places **")
    BaseRequest getSearchPlace(Long chatId, String text) {
        userHistoryComponent.createUserHistory(userService.findByChatId(chatId).getId(), "/search_places");
        StringBuilder place = new StringBuilder();
        for (int i = 1; i < text.length(); i++) {
            place.append(text.charAt(i)).append(" ");
        }
        return new SendMessage(chatId, geoCodeService.getInfoPlace(place.toString()));
    }
}
