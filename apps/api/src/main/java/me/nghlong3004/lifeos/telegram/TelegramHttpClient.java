package me.nghlong3004.lifeos.telegram;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

/**
 * Extension of {@link OkHttpClient} providing explicit Spring bean destruction and resource cleanup.
 *
 * <p>Cancels pending dispatcher calls, shuts down the non-daemon dispatcher executor service,
 * evicts pooled idle connections, and closes any allocated response cache upon application context
 * shutdown or startup failure.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/13/2026
 */
@Slf4j
public class TelegramHttpClient extends OkHttpClient implements AutoCloseable {

  private final AtomicBoolean closed = new AtomicBoolean(false);

  public TelegramHttpClient() {
    super();
  }

  public TelegramHttpClient(Builder builder) {
    super(builder);
  }

  @Override
  public void close() {
    if (!closed.compareAndSet(false, true)) {
      return;
    }
    try {
      dispatcher().cancelAll();
      dispatcher().executorService().shutdown();
      connectionPool().evictAll();
      if (cache() != null) {
        cache().close();
      }
    } catch (IOException e) {
      log.warn("Error closing Telegram HTTP client cache: {}", e.getClass().getSimpleName());
    }
  }
}
