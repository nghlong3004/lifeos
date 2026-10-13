package me.nghlong3004.lifeos.telegram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Deterministic isolated unit tests for {@link TelegramEchoConsumer}. Uses mocked Telegram client
 * and synthetic test data without network transport.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/12/2026
 */
class TelegramEchoConsumerTest {

  private TelegramClient telegramClient;
  private TelegramAdmissionPolicy defaultPublicPolicy;
  private TelegramEchoConsumer consumer;

  @BeforeEach
  void setUp() {
    telegramClient = mock(TelegramClient.class);
    defaultPublicPolicy = TelegramAdmissionPolicy.from("PUBLIC", null);
    consumer = new TelegramEchoConsumer(telegramClient, defaultPublicPolicy);
  }

  private Update createTextUpdate(Long chatId, String text, Integer threadId) {
    return createTextUpdate(chatId, chatId, text, threadId);
  }

  private Update createTextUpdate(Long chatId, Long senderId, String text, Integer threadId) {
    Chat chat = Chat.builder().id(chatId).type("private").build();
    Message.MessageBuilder<?, ?> messageBuilder = Message.builder().chat(chat).text(text);
    if (senderId != null) {
      User sender = User.builder().id(senderId).isBot(false).firstName("TestUser").build();
      messageBuilder.from(sender);
    }
    if (threadId != null) {
      messageBuilder.messageThreadId(threadId);
    }
    Update update = new Update();
    update.setMessage(messageBuilder.build());
    return update;
  }

  @Test
  @DisplayName("Echoes exact literal 'A' to the originating chat")
  void consume_literalA_sendsExactEcho() throws Exception {
    Update update = createTextUpdate(123456L, "A", null);

    consumer.consume(List.of(update));

    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
    verify(telegramClient).execute(captor.capture());

    SendMessage sent = captor.getValue();
    assertThat(sent.getChatId()).isEqualTo("123456");
    assertThat(sent.getText()).isEqualTo("A");
    assertThat(sent.getMessageThreadId()).isNull();
    assertThat(sent.getParseMode()).isNull();
  }

  @Test
  @DisplayName("Preserves Unicode characters including emojis, accents, and multilingual scripts")
  void consume_unicodeCharacters_preservesExactCharacters() throws Exception {
    String unicodeText =
        "Xin chào LifeOS! 🚀🔥 Tiếng Việt có dấu: ă, â, đ, ê, ô, ơ, ư. Math: ∑(x) = 42. CJK: 你好世界.";
    Update update = createTextUpdate(789L, unicodeText, null);

    consumer.consume(List.of(update));

    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
    verify(telegramClient).execute(captor.capture());

    SendMessage sent = captor.getValue();
    assertThat(sent.getChatId()).isEqualTo("789");
    assertThat(sent.getText()).isEqualTo(unicodeText);
    assertThat(sent.getParseMode()).isNull();
  }

  @Test
  @DisplayName("Preserves leading/trailing whitespaces, tabs, and line breaks without modification")
  void consume_whitespaceAndNewlines_preservesExactFormatting() throws Exception {
    String formattedText =
        "  First line with leading space\n\nSecond line\r\n\tTabbed line   trailing spaces   ";
    Update update = createTextUpdate(1001L, formattedText, null);

    consumer.consume(List.of(update));

    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
    verify(telegramClient).execute(captor.capture());

    SendMessage sent = captor.getValue();
    assertThat(sent.getChatId()).isEqualTo("1001");
    assertThat(sent.getText()).isEqualTo(formattedText);
    assertThat(sent.getParseMode()).isNull();
  }

  @Test
  @DisplayName("Targets forum message thread / topic when thread id is provided")
  void consume_withMessageThreadId_forwardsThreadId() throws Exception {
    Update update = createTextUpdate(9999L, "Topic message", 42);

    consumer.consume(List.of(update));

    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
    verify(telegramClient).execute(captor.capture());

    SendMessage sent = captor.getValue();
    assertThat(sent.getChatId()).isEqualTo("9999");
    assertThat(sent.getText()).isEqualTo("Topic message");
    assertThat(sent.getMessageThreadId()).isEqualTo(42);
    assertThat(sent.getParseMode()).isNull();
  }

  @Test
  @DisplayName("Ignores non-text messages and updates without creating client requests")
  void consume_nonTextUpdates_ignored() throws Exception {
    // Null and empty batches
    consumer.consume((List<Update>) null);
    consumer.consume(List.of());
    consumer.consume(Collections.singletonList(null));

    // Update with null message
    Update updateWithoutMessage = new Update();
    consumer.consume(List.of(updateWithoutMessage));

    // Message with null text
    Chat chat = Chat.builder().id(123L).type("private").build();
    User sender = User.builder().id(123L).isBot(false).firstName("TestUser").build();
    Message nonTextMessage = Message.builder().chat(chat).from(sender).build();
    Update updateWithNonTextMessage = new Update();
    updateWithNonTextMessage.setMessage(nonTextMessage);
    consumer.consume(List.of(updateWithNonTextMessage));

    // Message with empty text
    Message emptyTextMessage = Message.builder().chat(chat).from(sender).text("").build();
    Update updateWithEmptyText = new Update();
    updateWithEmptyText.setMessage(emptyTextMessage);
    consumer.consume(List.of(updateWithEmptyText));

    // Message with null chat
    Message noChatMessage = Message.builder().from(sender).text("orphaned").build();
    Update updateWithNoChat = new Update();
    updateWithNoChat.setMessage(noChatMessage);
    consumer.consume(List.of(updateWithNoChat));

    // Message with chat having null ID
    Chat chatWithNullId = mock(Chat.class);
    when(chatWithNullId.getId()).thenReturn(null);
    Message messageWithNullChatId =
        Message.builder().chat(chatWithNullId).from(sender).text("valid text").build();
    Update updateWithNullChatId = new Update();
    updateWithNullChatId.setMessage(messageWithNullChatId);
    consumer.consume(List.of(updateWithNullChatId));

    verify(telegramClient, never()).execute(any(SendMessage.class));
  }

  @Test
  @DisplayName("Catches TelegramApiException gracefully without throwing or leaking")
  void consume_telegramApiException_caughtGracefully() throws Exception {
    doThrow(new TelegramApiException("Simulated network timeout"))
        .when(telegramClient)
        .execute(any(SendMessage.class));

    Update update = createTextUpdate(555L, "Should not crash", null);

    // Must not throw
    consumer.consume(List.of(update));

    verify(telegramClient).execute(any(SendMessage.class));
  }

  @Test
  @DisplayName(
      "Catches RuntimeException gracefully and continues processing remaining updates in batch")
  void consume_batchWithFailure_continuesProcessing() throws Exception {
    Update failingUpdate = createTextUpdate(1L, "Fail message", null);
    Update succeedingUpdate = createTextUpdate(2L, "Success message", null);

    doThrow(new RuntimeException("Simulated unexpected error"))
        .doReturn(null)
        .when(telegramClient)
        .execute(any(SendMessage.class));

    consumer.consume(List.of(failingUpdate, succeedingUpdate));

    verify(telegramClient, times(2)).execute(any(SendMessage.class));
  }

  @Test
  @DisplayName("PUBLIC mode admits identified sender and sends echo message")
  void consume_publicMode_identifiedSender_sendsEcho() throws Exception {
    TelegramAdmissionPolicy publicPolicy = TelegramAdmissionPolicy.from("PUBLIC", null);
    TelegramEchoConsumer publicConsumer = new TelegramEchoConsumer(telegramClient, publicPolicy);

    Update update = createTextUpdate(200L, 54321L, "Public greeting", null);
    publicConsumer.consume(List.of(update));

    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
    verify(telegramClient).execute(captor.capture());
    assertThat(captor.getValue().getChatId()).isEqualTo("200");
    assertThat(captor.getValue().getText()).isEqualTo("Public greeting");
  }

  @Test
  @DisplayName("WHITELIST mode admits allowed sender and sends echo message")
  void consume_whitelistMode_allowedSender_sendsEcho() throws Exception {
    TelegramAdmissionPolicy policy = TelegramAdmissionPolicy.from("WHITELIST", "8888, 9999");
    TelegramEchoConsumer whitelistConsumer = new TelegramEchoConsumer(telegramClient, policy);

    Update update = createTextUpdate(100L, 8888L, "Allowed message", null);
    whitelistConsumer.consume(List.of(update));

    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
    verify(telegramClient).execute(captor.capture());
    assertThat(captor.getValue().getChatId()).isEqualTo("100");
    assertThat(captor.getValue().getText()).isEqualTo("Allowed message");
  }

  @Test
  @DisplayName("WHITELIST mode denies unlisted sender and does not reach client send")
  void consume_whitelistMode_unlistedSender_deniedWithoutSending() throws Exception {
    TelegramAdmissionPolicy policy = TelegramAdmissionPolicy.from("WHITELIST", "8888");
    TelegramEchoConsumer whitelistConsumer = new TelegramEchoConsumer(telegramClient, policy);

    Update update = createTextUpdate(100L, 7777L, "Unlisted message", null);
    whitelistConsumer.consume(List.of(update));

    verify(telegramClient, never()).execute(any(SendMessage.class));
  }

  @Test
  @DisplayName("Denies update when from is present but sender ID is null in BOTH WHITELIST and PUBLIC modes")
  void consume_fromPresentButIdNull_deniedInBothModes() throws Exception {
    TelegramAdmissionPolicy whitelistPolicy = TelegramAdmissionPolicy.from("WHITELIST", "1001");
    TelegramAdmissionPolicy publicPolicy = TelegramAdmissionPolicy.from("PUBLIC", null);

    Chat chat = Chat.builder().id(500L).type("private").build();
    User fromWithNullId = mock(User.class);
    when(fromWithNullId.getId()).thenReturn(null);
    Message message = Message.builder().chat(chat).from(fromWithNullId).text("Null ID test").build();
    Update update = new Update();
    update.setMessage(message);

    new TelegramEchoConsumer(telegramClient, whitelistPolicy).consume(List.of(update));
    new TelegramEchoConsumer(telegramClient, publicPolicy).consume(List.of(update));

    verify(telegramClient, never()).execute(any(SendMessage.class));
  }

  @Test
  @DisplayName("Denies update when sender (from) is missing in both modes")
  void consume_missingFrom_deniedInBothModes() throws Exception {
    TelegramAdmissionPolicy whitelistPolicy = TelegramAdmissionPolicy.from("WHITELIST", "1001");
    TelegramAdmissionPolicy publicPolicy = TelegramAdmissionPolicy.from("PUBLIC", null);

    Update updateWithoutFrom = createTextUpdate(1001L, null, "No from", null);

    new TelegramEchoConsumer(telegramClient, whitelistPolicy).consume(List.of(updateWithoutFrom));
    new TelegramEchoConsumer(telegramClient, publicPolicy).consume(List.of(updateWithoutFrom));

    verify(telegramClient, never()).execute(any(SendMessage.class));
  }

  @Test
  @DisplayName("Denies update when sender ID is nonpositive in both modes")
  void consume_nonpositiveSenderId_deniedInBothModes() throws Exception {
    TelegramAdmissionPolicy whitelistPolicy = TelegramAdmissionPolicy.from("WHITELIST", "1001");
    TelegramAdmissionPolicy publicPolicy = TelegramAdmissionPolicy.from("PUBLIC", null);

    for (Long invalidId : new Long[] {0L, -1L, -500L}) {
      Update update = createTextUpdate(1001L, invalidId, "Invalid ID", null);

      new TelegramEchoConsumer(telegramClient, whitelistPolicy).consume(List.of(update));
      new TelegramEchoConsumer(telegramClient, publicPolicy).consume(List.of(update));
    }

    verify(telegramClient, never()).execute(any(SendMessage.class));
  }

  @Test
  @DisplayName("Rejects chat ID vs sender ID confusion: validates strictly by sender message.from.id")
  void consume_chatIdVsSenderIdConfusion_authorizedOnlyBySenderId() throws Exception {
    TelegramAdmissionPolicy policy = TelegramAdmissionPolicy.from("WHITELIST", "1111");
    TelegramEchoConsumer whitelistConsumer = new TelegramEchoConsumer(telegramClient, policy);

    // Case 1: Chat ID is in whitelist (1111), but sender ID is NOT (2222) -> DENIED
    Update deniedUpdate = createTextUpdate(1111L, 2222L, "Attacker spoof", null);
    whitelistConsumer.consume(List.of(deniedUpdate));
    verify(telegramClient, never()).execute(any(SendMessage.class));

    // Case 2: Chat ID is NOT in whitelist (3333), but sender ID IS (1111) -> ALLOWED
    Update allowedUpdate = createTextUpdate(3333L, 1111L, "Legitimate in group", null);
    whitelistConsumer.consume(List.of(allowedUpdate));

    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
    verify(telegramClient).execute(captor.capture());
    assertThat(captor.getValue().getChatId()).isEqualTo("3333");
    assertThat(captor.getValue().getText()).isEqualTo("Legitimate in group");
  }

  @Test
  @DisplayName("Never authorizes using username or message text")
  void consume_neverAuthorizesUsingUsernameOrText() throws Exception {
    TelegramAdmissionPolicy policy = TelegramAdmissionPolicy.from("WHITELIST", "5555");
    TelegramEchoConsumer whitelistConsumer = new TelegramEchoConsumer(telegramClient, policy);

    // Sender ID is 9999 (unauthorized), but username is "5555" and text contains "5555"
    Chat chat = Chat.builder().id(100L).type("private").build();
    User sender =
        User.builder()
            .id(9999L)
            .userName("5555")
            .firstName("Impostor")
            .isBot(false)
            .build();
    Message message = Message.builder().chat(chat).from(sender).text("5555").build();
    Update update = new Update();
    update.setMessage(message);

    whitelistConsumer.consume(List.of(update));

    verify(telegramClient, never()).execute(any(SendMessage.class));
  }
}
