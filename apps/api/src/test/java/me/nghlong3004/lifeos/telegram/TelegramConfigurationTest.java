package me.nghlong3004.lifeos.telegram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Isolated unit tests for {@link TelegramConfiguration} verifying bean creation, mapper and transport
 * reuse identity, admission policy creation, executor thread properties, and Spring constructor selection.
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
    TelegramProperties properties =
        new TelegramProperties("synthetic-test-token", "PUBLIC", null);
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

  @Test
  @DisplayName("telegramHttpClient creates disposable client that shuts down dispatcher on close")
  void telegramHttpClient_isDisposableAndShutsDownOnClose() throws Exception {
    TelegramConfiguration configuration = new TelegramConfiguration();
    OkHttpClient client = configuration.telegramHttpClient();

    assertThat(client).isInstanceOf(TelegramHttpClient.class);
    assertThat(client).isInstanceOf(AutoCloseable.class);

    ((AutoCloseable) client).close();

    assertThat(client.dispatcher().executorService().isShutdown()).isTrue();
  }

  @Test
  @DisplayName("telegramAdmissionPolicy bean constructs valid policy and fails closed when properties are invalid")
  void telegramAdmissionPolicy_createsPolicyOrFailsClosed() {
    TelegramConfiguration configuration = new TelegramConfiguration();

    TelegramProperties validProperties =
        new TelegramProperties("synthetic-test-token", "WHITELIST", "1001, 1002");
    TelegramAdmissionPolicy policy = configuration.telegramAdmissionPolicy(validProperties);
    assertThat(policy).isNotNull();
    assertThat(policy.mode()).isEqualTo(TelegramAccessMode.WHITELIST);
    assertThat(policy.allowedUserIds()).containsExactlyInAnyOrder(1001L, 1002L);

    TelegramProperties invalidProperties =
        new TelegramProperties("synthetic-test-token", "INVALID_MODE", null);
    assertThatThrownBy(() -> configuration.telegramAdmissionPolicy(invalidProperties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Invalid configuration for lifeos.telegram.access-mode");
  }

  @Test
  @DisplayName("Single unambiguous constructor DI path: AutowiredAnnotationBeanPostProcessor determines exactly one candidate")
  void springConstructorSelection_determinesUnambiguousCandidateConstructors() {
    AutowiredAnnotationBeanPostProcessor processor = new AutowiredAnnotationBeanPostProcessor();

    // Verify declared constructors are singular (no legacy compatibility constructors)
    assertThat(TelegramProperties.class.getDeclaredConstructors()).hasSize(1);
    assertThat(TelegramEchoConsumer.class.getDeclaredConstructors()).hasSize(1);
    assertThat(TelegramBotLifecycle.class.getDeclaredConstructors()).hasSize(1);

    // Verify Spring candidate constructor selection finds the unambiguous constructor
    Constructor<?>[] consumerCandidates =
        processor.determineCandidateConstructors(TelegramEchoConsumer.class, "telegramEchoConsumer");
    assertThat(consumerCandidates).isNotNull().hasSize(1);

    Constructor<?>[] lifecycleCandidates =
        processor.determineCandidateConstructors(TelegramBotLifecycle.class, "telegramBotLifecycle");
    assertThat(lifecycleCandidates).isNotNull().hasSize(1);
  }

  @Test
  @DisplayName("Offline Spring bean wiring: DefaultListableBeanFactory resolves components cleanly with mocked dependencies")
  void springBeanWiring_resolvesComponentsCleanlyWithMocks() {
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    TelegramProperties properties =
        new TelegramProperties("synthetic-test-token", "WHITELIST", "1001, 1002");
    TelegramAdmissionPolicy policy = TelegramAdmissionPolicy.from(properties);
    TelegramClient mockClient = mock(TelegramClient.class);
    TelegramBotsLongPollingApplication mockApp = mock(TelegramBotsLongPollingApplication.class);

    beanFactory.registerSingleton("telegramProperties", properties);
    beanFactory.registerSingleton("telegramAdmissionPolicy", policy);
    beanFactory.registerSingleton("telegramClient", mockClient);
    beanFactory.registerSingleton("telegramBotsLongPollingApplication", mockApp);

    AutowiredAnnotationBeanPostProcessor processor = new AutowiredAnnotationBeanPostProcessor();
    processor.setBeanFactory(beanFactory);
    beanFactory.addBeanPostProcessor(processor);

    beanFactory.registerBeanDefinition(
        "telegramEchoConsumer", new RootBeanDefinition(TelegramEchoConsumer.class));
    beanFactory.registerBeanDefinition(
        "telegramBotLifecycle", new RootBeanDefinition(TelegramBotLifecycle.class));

    TelegramEchoConsumer resolvedConsumer = beanFactory.getBean(TelegramEchoConsumer.class);
    assertThat(resolvedConsumer).isNotNull();

    TelegramBotLifecycle resolvedLifecycle = beanFactory.getBean(TelegramBotLifecycle.class);
    assertThat(resolvedLifecycle).isNotNull();
    assertThat(resolvedLifecycle.isRunning()).isFalse();
  }
}
