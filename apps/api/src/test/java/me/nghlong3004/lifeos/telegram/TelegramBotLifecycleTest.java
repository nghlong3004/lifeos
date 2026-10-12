package me.nghlong3004.lifeos.telegram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Deterministic isolated unit tests for {@link TelegramBotLifecycle}.
 * Uses mocked native polling application, scheduler, transport, and session with synthetic test data.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/12/2026
 */
class TelegramBotLifecycleTest {

  private TelegramEchoConsumer updateConsumer;
  private TelegramBotsLongPollingApplication pollingApplication;
  private ScheduledExecutorService pollingExecutor;
  private OkHttpClient mockHttpClient;
  private Dispatcher mockDispatcher;
  private ConnectionPool mockConnectionPool;
  private ExecutorService mockDispatcherExecutor;
  private BotSession mockSession;

  @BeforeEach
  void setUp() {
    updateConsumer = mock(TelegramEchoConsumer.class);
    pollingApplication = mock(TelegramBotsLongPollingApplication.class);
    pollingExecutor = mock(ScheduledExecutorService.class);
    mockHttpClient = mock(OkHttpClient.class);
    mockDispatcher = mock(Dispatcher.class);
    mockConnectionPool = mock(ConnectionPool.class);
    mockDispatcherExecutor = mock(ExecutorService.class);
    mockSession = mock(BotSession.class);

    when(mockHttpClient.dispatcher()).thenReturn(mockDispatcher);
    when(mockDispatcher.executorService()).thenReturn(mockDispatcherExecutor);
    when(mockHttpClient.connectionPool()).thenReturn(mockConnectionPool);
  }

  @Test
  @DisplayName("start remains inert and does not register session when token is blank or null")
  void start_withBlankOrNullToken_remainsInert() throws Exception {
    for (String invalidToken : new String[] {null, "", "   ", "\t"}) {
      TelegramProperties properties = new TelegramProperties(invalidToken);
      TelegramBotLifecycle lifecycle =
          new TelegramBotLifecycle(
              properties,
              updateConsumer,
              pollingApplication,
              pollingExecutor,
              mockHttpClient);

      lifecycle.start();

      assertThat(lifecycle.isRunning()).isFalse();
      verify(pollingApplication, never()).registerBot(anyString(), any());
      verify(pollingExecutor, never()).shutdown();
      verify(mockDispatcher, never()).cancelAll();
      verify(mockDispatcherExecutor, never()).shutdown();
      verify(mockConnectionPool, never()).evictAll();
    }
  }

  @Test
  @DisplayName("start registers session exactly once when valid token is configured")
  void start_withValidToken_registersSessionOnce() throws Exception {
    TelegramProperties properties = new TelegramProperties("synthetic-test-token");
    when(pollingApplication.registerBot("synthetic-test-token", updateConsumer))
        .thenReturn(mockSession);

    TelegramBotLifecycle lifecycle =
        new TelegramBotLifecycle(
            properties,
            updateConsumer,
            pollingApplication,
            pollingExecutor,
            mockHttpClient);

    lifecycle.start();

    assertThat(lifecycle.isRunning()).isTrue();
    verify(pollingApplication, times(1)).registerBot("synthetic-test-token", updateConsumer);

    // Redundant start call must be idempotent
    lifecycle.start();
    verify(pollingApplication, times(1)).registerBot("synthetic-test-token", updateConsumer);
    assertThat(lifecycle.isRunning()).isTrue();
  }

  @Test
  @DisplayName("stop closes the active session and releases all owned resources including HTTP transport")
  void stop_closesActiveSessionAndReleasesResources() throws Exception {
    TelegramProperties properties = new TelegramProperties("synthetic-test-token");
    when(pollingApplication.registerBot("synthetic-test-token", updateConsumer))
        .thenReturn(mockSession);

    TelegramBotLifecycle lifecycle =
        new TelegramBotLifecycle(
            properties,
            updateConsumer,
            pollingApplication,
            pollingExecutor,
            mockHttpClient);

    lifecycle.start();
    assertThat(lifecycle.isRunning()).isTrue();

    lifecycle.stop();

    assertThat(lifecycle.isRunning()).isFalse();
    verify(mockSession, times(1)).close();
    verify(pollingApplication, times(1)).close();
    verify(pollingExecutor, times(1)).shutdown();
    verify(mockDispatcher, times(1)).cancelAll();
    verify(mockDispatcherExecutor, times(1)).shutdown();
    verify(mockConnectionPool, times(1)).evictAll();

    // Redundant stop call must be safe
    lifecycle.stop();
  }

  @Test
  @DisplayName("start cleans up owned resources and fails startup with sanitized diagnostics upon registration failure")
  void start_whenRegistrationFails_releasesResourcesAndFailsStartup() throws Exception {
    TelegramProperties properties = new TelegramProperties("synthetic-test-token");
    when(pollingApplication.registerBot("synthetic-test-token", updateConsumer))
        .thenThrow(new TelegramApiException("Simulated registration failure"));

    TelegramBotLifecycle lifecycle =
        new TelegramBotLifecycle(
            properties,
            updateConsumer,
            pollingApplication,
            pollingExecutor,
            mockHttpClient);

    assertThatThrownBy(lifecycle::start)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Failed to register Telegram bot: TelegramApiException")
        .hasNoCause();

    assertThat(lifecycle.isRunning()).isFalse();
    verify(pollingApplication, times(1)).close();
    verify(pollingExecutor, times(1)).shutdown();
    verify(mockDispatcher, times(1)).cancelAll();
    verify(mockDispatcherExecutor, times(1)).shutdown();
    verify(mockConnectionPool, times(1)).evictAll();
  }

  @Test
  @DisplayName("stop handles session close exception safely and proceeds to release executor and HTTP transport")
  void stop_whenSessionCloseThrows_releasesExecutorAndTransportSafely() throws Exception {
    TelegramProperties properties = new TelegramProperties("synthetic-test-token");
    when(pollingApplication.registerBot("synthetic-test-token", updateConsumer))
        .thenReturn(mockSession);
    doThrow(new TelegramApiException("Error during session close")).when(mockSession).close();

    TelegramBotLifecycle lifecycle =
        new TelegramBotLifecycle(
            properties,
            updateConsumer,
            pollingApplication,
            pollingExecutor,
            mockHttpClient);

    lifecycle.start();
    lifecycle.stop();

    assertThat(lifecycle.isRunning()).isFalse();
    verify(mockSession, times(1)).close();
    verify(pollingApplication, times(1)).close();
    verify(pollingExecutor, times(1)).shutdown();
    verify(mockDispatcher, times(1)).cancelAll();
    verify(mockDispatcherExecutor, times(1)).shutdown();
    verify(mockConnectionPool, times(1)).evictAll();
  }

  @Test
  @DisplayName("stop handles HTTP transport release exception safely without re-throwing")
  void stop_whenHttpClientReleaseThrows_completesSafely() throws Exception {
    TelegramProperties properties = new TelegramProperties("synthetic-test-token");
    when(pollingApplication.registerBot("synthetic-test-token", updateConsumer))
        .thenReturn(mockSession);
    doThrow(new RuntimeException("Simulated dispatcher cancel error"))
        .when(mockDispatcher)
        .cancelAll();

    TelegramBotLifecycle lifecycle =
        new TelegramBotLifecycle(
            properties,
            updateConsumer,
            pollingApplication,
            pollingExecutor,
            mockHttpClient);

    lifecycle.start();

    assertThatCode(lifecycle::stop).doesNotThrowAnyException();
    assertThat(lifecycle.isRunning()).isFalse();
  }
}
