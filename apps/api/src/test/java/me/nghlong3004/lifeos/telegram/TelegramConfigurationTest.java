package me.nghlong3004.lifeos.telegram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Isolated unit tests for {@link TelegramConfiguration} verifying bean creation, mapper and transport
 * reuse identity, and executor thread properties.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/12/2026
 */
class TelegramConfigurationTest {

  @Test
  @DisplayName("Reuses the shared Jackson 2 ObjectMapper and OkHttpClient instances across client and polling application")
  void configuration_reusesSharedObjectMapperAndHttpClient() throws Exception {
    TelegramConfiguration configuration = new TelegramConfiguration();
    ObjectMapper sharedMapper = new ObjectMapper();
    OkHttpClient sharedHttpClient = configuration.telegramHttpClient();
    TelegramProperties properties = new TelegramProperties("synthetic-test-token");
    ScheduledExecutorService mockExecutor = mock(ScheduledExecutorService.class);

    TelegramClient client = configuration.telegramClient(properties, sharedMapper, sharedHttpClient);
    assertThat(client).isInstanceOf(OkHttpTelegramClient.class);

    // Verify referential identity of objectMapper in OkHttpTelegramClient via reflection
    Field clientMapperField = OkHttpTelegramClient.class.getDeclaredField("objectMapper");
    clientMapperField.setAccessible(true);
    ObjectMapper clientMapper = (ObjectMapper) clientMapperField.get(client);
    assertThat(clientMapper).isSameAs(sharedMapper);

    // Verify referential identity of OkHttpClient transport in OkHttpTelegramClient via reflection
    Field clientHttpField = OkHttpTelegramClient.class.getDeclaredField("client");
    clientHttpField.setAccessible(true);
    OkHttpClient internalClient = (OkHttpClient) clientHttpField.get(client);
    assertThat(internalClient).isSameAs(sharedHttpClient);

    TelegramBotsLongPollingApplication pollingApp =
        configuration.telegramBotsLongPollingApplication(sharedMapper, mockExecutor);
    assertThat(pollingApp).isNotNull();

    // Verify referential identity returned by objectMapperSupplier in TelegramBotsLongPollingApplication
    Field appSupplierField =
        TelegramBotsLongPollingApplication.class.getDeclaredField("objectMapperSupplier");
    appSupplierField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Supplier<ObjectMapper> appSupplier =
        (Supplier<ObjectMapper>) appSupplierField.get(pollingApp);
    assertThat(appSupplier).isNotNull();
    assertThat(appSupplier.get()).isSameAs(sharedMapper);
  }

  @Test
  @DisplayName("Creates dedicated polling executor that executes tasks on a named daemon platform thread")
  void telegramPollingExecutor_runsTasksOnNamedDaemonPlatformThread() throws Exception {
    TelegramConfiguration configuration = new TelegramConfiguration();
    ScheduledExecutorService executor = configuration.telegramPollingExecutor();

    AtomicReference<Thread> threadRef = new AtomicReference<>();
    Future<?> future = executor.submit(() -> threadRef.set(Thread.currentThread()));
    future.get(5, TimeUnit.SECONDS);
    executor.shutdown();

    Thread thread = threadRef.get();
    assertThat(thread).isNotNull();
    assertThat(thread.getName()).isEqualTo("telegram-polling");
    assertThat(thread.isDaemon()).isTrue();
    assertThat(thread.isVirtual()).isFalse();
  }
}
