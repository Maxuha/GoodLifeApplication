package life.good.goodlife;

import com.github.telegram.mvc.api.EnableTelegram;
import com.github.telegram.mvc.config.TelegramBotBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.github.telegram.mvc.config.TelegramMvcConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableTelegram
public class GoodLifeApplication implements TelegramMvcConfiguration {
    private static Logger logger = LoggerFactory.getLogger(GoodLifeApplication.class);

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        try {
            SpringApplication.run(GoodLifeApplication.class, args);
        } catch (Exception e) {
            logger.error("Run application problem: " + e.getMessage());
        }

    }

    @Override
    public void configuration(TelegramBotBuilder telegramBotBuilder) {
        telegramBotBuilder.token(System.getenv().get("telegram_bot_token")).alias("BotBean");
        //telegramBotBuilder.token(environment.getProperty("telegram.bot.token")).alias("BotBean");
    }
}
