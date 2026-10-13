package me.nghlong3004.lifeos.telegram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Isolated unit tests for {@link TelegramHttpClient}.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/13/2026
 */
class TelegramHttpClientTest {

  @Test
  @DisplayName(
      "close cancels queued calls, shuts down dispatcher executor, and evicts connection pool idempotently")
  void close_cancelsCallsShutsDownDispatcherAndEvictsPoolIdempotently() {
    Dispatcher mockDispatcher = mock(Dispatcher.class);
    ExecutorService mockExecutor = mock(ExecutorService.class);
    when(mockDispatcher.executorService()).thenReturn(mockExecutor);
    ConnectionPool mockPool = mock(ConnectionPool.class);

    OkHttpClient.Builder builder =
        new OkHttpClient.Builder().dispatcher(mockDispatcher).connectionPool(mockPool);

    TelegramHttpClient client = new TelegramHttpClient(builder);

    // Initial close
    client.close();

    verify(mockDispatcher, times(1)).cancelAll();
    verify(mockExecutor, times(1)).shutdown();
    verify(mockPool, times(1)).evictAll();

    // Redundant close is idempotent
    client.close();

    verify(mockDispatcher, times(1)).cancelAll();
    verify(mockExecutor, times(1)).shutdown();
    verify(mockPool, times(1)).evictAll();
  }

  @Test
  @DisplayName("Default constructor creates functional OkHttpClient instance")
  void defaultConstructor_createsFunctionalClient() {
    TelegramHttpClient client = new TelegramHttpClient();
    assertThat(client).isNotNull();
    assertThat(client.dispatcher()).isNotNull();
    assertThat(client.connectionPool()).isNotNull();

    client.close();
    assertThat(client.dispatcher().executorService().isShutdown()).isTrue();
  }
}
