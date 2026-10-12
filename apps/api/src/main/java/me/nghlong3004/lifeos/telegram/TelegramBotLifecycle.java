package me.nghlong3004.lifeos.telegram;

import java.util.concurrent.ScheduledExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

/**
 * Lifecycle manager owning the Telegram long-polling bot registration, native application,
 * background scheduler, and outbound HTTP client transport. Ensures at most one registered session,
 * idempotent start/stop, sanitized startup failure reporting, and complete resource cleanup on shutdown.
 * Uses Lombok {@link RequiredArgsConstructor} for constructor injection.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/12/2026
 */
@Component
@Profile("telegram-echo")
@RequiredArgsConstructor
@Slf4j
public class TelegramBotLifecycle implements SmartLifecycle, DisposableBean {

  private final TelegramProperties properties;
  private final TelegramEchoConsumer updateConsumer;
  private final TelegramBotsLongPollingApplication pollingApplication;
  private final ScheduledExecutorService pollingExecutor;
  private final OkHttpClient httpClient;

  private boolean running = false;
  private BotSession registeredSession;

  @Override
  public synchronized void start() {
    if (running) {
      log.info("Telegram bot lifecycle is already running; skipping redundant registration");
      return;
    }

    if (!properties.hasValidToken()) {
      log.info("Telegram bot token is not configured or blank; registration skipped");
      return;
    }

    try {
      log.info("Registering Telegram long-polling bot session");
      this.registeredSession =
          pollingApplication.registerBot(properties.botToken(), updateConsumer);
      this.running = true;
    } catch (Exception e) {
      log.error("Failed to register Telegram bot: {}", e.getClass().getSimpleName());
      cleanupResourcesSafely();
      throw new IllegalStateException(
          "Failed to register Telegram bot: " + e.getClass().getSimpleName());
    }
  }

  @Override
  public synchronized void stop() {
    if (!running && registeredSession == null) {
      return;
    }
    log.info("Stopping Telegram bot session and requesting resource shutdown");
    cleanupResourcesSafely();
    this.running = false;
  }

  @Override
  public void destroy() {
    stop();
  }

  @Override
  public synchronized boolean isRunning() {
    return running;
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public int getPhase() {
    return SmartLifecycle.DEFAULT_PHASE;
  }

  private void cleanupResourcesSafely() {
    if (registeredSession != null) {
      try {
        registeredSession.close();
      } catch (Exception e) {
        log.warn("Error closing Telegram bot session: {}", e.getClass().getSimpleName());
      } finally {
        registeredSession = null;
      }
    }
    try {
      pollingApplication.close();
    } catch (Exception e) {
      log.warn("Error closing Telegram polling application: {}", e.getClass().getSimpleName());
    }
    if (pollingExecutor != null && !pollingExecutor.isShutdown()) {
      try {
        pollingExecutor.shutdown();
      } catch (Exception e) {
        log.warn("Error shutting down polling executor: {}", e.getClass().getSimpleName());
      }
    }
    if (httpClient != null) {
      try {
        httpClient.dispatcher().cancelAll();
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
        if (httpClient.cache() != null) {
          httpClient.cache().close();
        }
      } catch (Exception e) {
        log.warn("Error releasing Telegram HTTP client transport: {}", e.getClass().getSimpleName());
      }
    }
  }
}
