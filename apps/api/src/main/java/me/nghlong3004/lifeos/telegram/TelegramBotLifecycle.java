package me.nghlong3004.lifeos.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

/**
 * Manages the lifecycle of the Telegram long-polling bot session.
 *
 * <p>Starts polling when the application starts and stops polling during shutdown. Shared
 * infrastructure resources are managed by their respective Spring beans.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/12/2026
 */
@Slf4j
@Component
@Profile("telegram-echo")
@RequiredArgsConstructor
public class TelegramBotLifecycle implements SmartLifecycle {

  private final TelegramProperties properties;
  private final TelegramAdmissionPolicy admissionPolicy;
  private final TelegramEchoConsumer updateConsumer;
  private final TelegramBotsLongPollingApplication pollingApplication;

  private BotSession registeredSession;

  @Override
  public synchronized void start() {
    if (isRunning()) {
      return;
    }

    if (!properties.hasValidToken()) {
      throw new IllegalStateException("Telegram bot token is not configured");
    }

    try {
      if (registeredSession == null) {
        registeredSession = pollingApplication.registerBot(properties.botToken(), updateConsumer);
      } else {
        registeredSession.start();
      }

      log.info("Telegram long-polling session started");
    } catch (Exception e) {
      log.error("Failed to start Telegram bot: {}", e.getClass().getSimpleName());
      if (registeredSession != null) {
        try {
          registeredSession.close();
        } catch (Exception ignored) {
        }
        registeredSession = null;
      }
      try {
        pollingApplication.unregisterBot(properties.botToken());
      } catch (Exception ignored) {
      }
      throw new IllegalStateException("Failed to start Telegram bot");
    }
  }

  @Override
  public synchronized void stop() {
    if (!isRunning()) {
      return;
    }

    registeredSession.stop();
    log.info("Telegram long-polling session stopped");
  }

  @Override
  public synchronized boolean isRunning() {
    return registeredSession != null && registeredSession.isRunning();
  }
}
