package me.nghlong3004.lifeos.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.TelegramOkHttpClientFactory;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Spring configuration providing the complete inspectable bean graph for Telegram integration.
 * Injects and reuses the shared Jackson 2 {@link ObjectMapper} bean for both outbound client
 * and long-polling updates consumer paths.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/12/2026
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TelegramProperties.class)
public class TelegramConfiguration {

  @Bean
  @Profile("telegram-echo")
  @ConditionalOnMissingBean(OkHttpClient.class)
  public OkHttpClient telegramHttpClient() {
    return new OkHttpClient();
  }

  @Bean
  @Profile("telegram-echo")
  @ConditionalOnMissingBean(TelegramClient.class)
  public TelegramClient telegramClient(
      TelegramProperties properties,
      ObjectMapper objectMapper,
      OkHttpClient telegramHttpClient) {
    String token = properties.hasValidToken() ? properties.botToken() : "";
    return new OkHttpTelegramClient(
        objectMapper,
        telegramHttpClient,
        token,
        TelegramUrl.DEFAULT_URL);
  }

  @Bean(destroyMethod = "shutdown")
  @Profile("telegram-echo")
  @ConditionalOnMissingBean(name = "telegramPollingExecutor")
  public ScheduledExecutorService telegramPollingExecutor() {
    return Executors.newSingleThreadScheduledExecutor(
        runnable -> {
          Thread thread = new Thread(runnable, "telegram-polling");
          thread.setDaemon(true);
          return thread;
        });
  }

  @Bean(destroyMethod = "close")
  @Profile("telegram-echo")
  @ConditionalOnMissingBean(TelegramBotsLongPollingApplication.class)
  public TelegramBotsLongPollingApplication telegramBotsLongPollingApplication(
      ObjectMapper objectMapper,
      ScheduledExecutorService telegramPollingExecutor) {
    return new TelegramBotsLongPollingApplication(
        () -> objectMapper,
        new TelegramOkHttpClientFactory.DefaultOkHttpClientCreator(),
        () -> telegramPollingExecutor);
  }
}
