package me.nghlong3004.lifeos.telegram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Isolated unit tests for {@link TelegramBotLifecycle}.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/12/2026
 */
class TelegramBotLifecycleTest {

  private TelegramEchoConsumer updateConsumer;
  private TelegramBotsLongPollingApplication pollingApplication;
  private BotSession mockSession;
  private TelegramAdmissionPolicy defaultPolicy;

  @BeforeEach
  void setUp() {
    updateConsumer = mock(TelegramEchoConsumer.class);
    pollingApplication = mock(TelegramBotsLongPollingApplication.class);
    mockSession = mock(BotSession.class);
    defaultPolicy = TelegramAdmissionPolicy.from("PUBLIC", null);
  }

  @Test
  @DisplayName("start fails fast and does not register bot session when token is blank or null")
  void start_withBlankOrNullToken_failsFastWithoutAttemptingRegistration() throws Exception {
    for (String invalidToken : new String[] {null, "", "   ", "\t"}) {
      TelegramProperties properties = new TelegramProperties(invalidToken, "PUBLIC", null);
      TelegramBotLifecycle lifecycle =
          new TelegramBotLifecycle(properties, defaultPolicy, updateConsumer, pollingApplication);

      assertThatThrownBy(lifecycle::start)
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Telegram bot token is not configured");

      assertThat(lifecycle.isRunning()).isFalse();
      verify(pollingApplication, never()).registerBot(anyString(), any());
    }
  }

  @Test
  @DisplayName("Policy construction fails closed on invalid access mode before any registration attempt")
  void policyConstruction_withInvalidAccessMode_failsBeforeRegistration() throws Exception {
    TelegramProperties properties =
        new TelegramProperties("synthetic-test-token", "INVALID_MODE", "1001");

    assertThatThrownBy(() -> TelegramAdmissionPolicy.from(properties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Invalid configuration for lifeos.telegram.access-mode");

    verify(pollingApplication, never()).registerBot(anyString(), any());
  }

  @Test
  @DisplayName("Policy construction fails closed on invalid allowed user IDs before any registration attempt")
  void policyConstruction_withInvalidAllowedUserIds_failsBeforeRegistration() throws Exception {
    TelegramProperties properties =
        new TelegramProperties("synthetic-test-token", "WHITELIST", "");

    assertThatThrownBy(() -> TelegramAdmissionPolicy.from(properties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Invalid configuration for lifeos.telegram.allowed-user-ids");

    verify(pollingApplication, never()).registerBot(anyString(), any());
  }

  @Test
  @DisplayName("start registers bot session exactly once when valid token is configured")
  void start_withValidToken_registersSessionOnce() throws Exception {
    TelegramProperties properties =
        new TelegramProperties("synthetic-test-token", "PUBLIC", null);
    when(pollingApplication.registerBot("synthetic-test-token", updateConsumer))
        .thenReturn(mockSession);
    when(mockSession.isRunning()).thenReturn(true);

    TelegramBotLifecycle lifecycle =
        new TelegramBotLifecycle(properties, defaultPolicy, updateConsumer, pollingApplication);

    lifecycle.start();

    assertThat(lifecycle.isRunning()).isTrue();
    verify(pollingApplication, times(1)).registerBot("synthetic-test-token", updateConsumer);

    // Redundant start call must be idempotent
    lifecycle.start();
    verify(pollingApplication, times(1)).registerBot("synthetic-test-token", updateConsumer);
    verify(mockSession, never()).start();
    assertThat(lifecycle.isRunning()).isTrue();
  }

  @Test
  @DisplayName("stop stops active session when running and is idempotent when not running")
  void stop_stopsActiveSessionAndIsIdempotent() throws Exception {
    TelegramProperties properties =
        new TelegramProperties("synthetic-test-token", "PUBLIC", null);
    when(pollingApplication.registerBot("synthetic-test-token", updateConsumer))
        .thenReturn(mockSession);
    when(mockSession.isRunning()).thenReturn(true);

    TelegramBotLifecycle lifecycle =
        new TelegramBotLifecycle(properties, defaultPolicy, updateConsumer, pollingApplication);

    // Stop before start is safe no-op
    lifecycle.stop();
    verify(mockSession, never()).stop();

    lifecycle.start();
    assertThat(lifecycle.isRunning()).isTrue();

    lifecycle.stop();
    verify(mockSession, times(1)).stop();

    // Redundant stop call is safe no-op
    when(mockSession.isRunning()).thenReturn(false);
    lifecycle.stop();
    verify(mockSession, times(1)).stop();
  }

  @Test
  @DisplayName("restart after stop invokes start on existing registered session without re-registering")
  void restart_afterStop_invokesSessionStart() throws Exception {
    TelegramProperties properties =
        new TelegramProperties("synthetic-test-token", "PUBLIC", null);
    when(pollingApplication.registerBot("synthetic-test-token", updateConsumer))
        .thenReturn(mockSession);
    when(mockSession.isRunning()).thenReturn(true);

    TelegramBotLifecycle lifecycle =
        new TelegramBotLifecycle(properties, defaultPolicy, updateConsumer, pollingApplication);

    lifecycle.start();
    verify(pollingApplication, times(1)).registerBot("synthetic-test-token", updateConsumer);

    lifecycle.stop();
    when(mockSession.isRunning()).thenReturn(false);

    lifecycle.start();
    verify(mockSession, times(1)).start();
    verify(pollingApplication, times(1)).registerBot("synthetic-test-token", updateConsumer);
  }

  @Test
  @DisplayName("start cleans up partial registration and fails startup upon registration failure")
  void start_whenRegistrationFails_cleansUpPartialRegistrationAndFailsStartup() throws Exception {
    TelegramProperties properties =
        new TelegramProperties("synthetic-test-token", "PUBLIC", null);
    when(pollingApplication.registerBot("synthetic-test-token", updateConsumer))
        .thenThrow(new TelegramApiException("Simulated registration failure"));

    TelegramBotLifecycle lifecycle =
        new TelegramBotLifecycle(properties, defaultPolicy, updateConsumer, pollingApplication);

    assertThatThrownBy(lifecycle::start)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Failed to start Telegram bot");

    assertThat(lifecycle.isRunning()).isFalse();
    verify(pollingApplication, times(1)).unregisterBot("synthetic-test-token");
  }

  @Test
  @DisplayName("start cleans up session and partial registration upon restart failure")
  void start_whenRestartFails_cleansUpSessionAndPartialRegistrationAndFailsStartup()
      throws Exception {
    TelegramProperties properties =
        new TelegramProperties("synthetic-test-token", "PUBLIC", null);
    when(pollingApplication.registerBot("synthetic-test-token", updateConsumer))
        .thenReturn(mockSession);
    when(mockSession.isRunning()).thenReturn(true);

    TelegramBotLifecycle lifecycle =
        new TelegramBotLifecycle(properties, defaultPolicy, updateConsumer, pollingApplication);

    lifecycle.start();
    lifecycle.stop();
    when(mockSession.isRunning()).thenReturn(false);

    doThrow(new TelegramApiException("Simulated restart failure")).when(mockSession).start();

    assertThatThrownBy(lifecycle::start)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Failed to start Telegram bot");

    assertThat(lifecycle.isRunning()).isFalse();
    verify(mockSession, times(1)).close();
    verify(pollingApplication, times(1)).unregisterBot("synthetic-test-token");
  }
}
