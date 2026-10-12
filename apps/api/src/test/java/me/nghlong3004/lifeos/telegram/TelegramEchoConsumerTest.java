package me.nghlong3004.lifeos.telegram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Deterministic isolated unit tests for {@link TelegramEchoConsumer}.
 * Uses mocked Telegram client and synthetic test data without network transport.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/12/2026
 */
class TelegramEchoConsumerTest {

  private TelegramClient telegramClient;
  private TelegramEchoConsumer consumer;

  @BeforeEach
  void setUp() {
    telegramClient = mock(TelegramClient.class);
    consumer = new TelegramEchoConsumer(telegramClient);
  }

  private Update createTextUpdate(Long chatId, String text, Integer threadId) {
    Chat chat = Chat.builder().id(chatId).type("private").build();
    Message.MessageBuilder<?, ?> messageBuilder = Message.builder().chat(chat).text(text);
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

    consumer.consume(update);

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

    consumer.consume(update);

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

    consumer.consume(update);

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

    consumer.consume(update);

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
    // Null update
    consumer.consume((Update) null);
    consumer.consume((List<Update>) null);
    consumer.consume(List.of());

    // Update with null message
    Update updateWithoutMessage = new Update();
    consumer.consume(updateWithoutMessage);

    // Message with null text
    Chat chat = Chat.builder().id(123L).type("private").build();
    Message nonTextMessage = Message.builder().chat(chat).build();
    Update updateWithNonTextMessage = new Update();
    updateWithNonTextMessage.setMessage(nonTextMessage);
    consumer.consume(updateWithNonTextMessage);

    // Message with empty text
    Message emptyTextMessage = Message.builder().chat(chat).text("").build();
    Update updateWithEmptyText = new Update();
    updateWithEmptyText.setMessage(emptyTextMessage);
    consumer.consume(updateWithEmptyText);

    // Message with null chat
    Message noChatMessage = Message.builder().text("orphaned").build();
    Update updateWithNoChat = new Update();
    updateWithNoChat.setMessage(noChatMessage);
    consumer.consume(updateWithNoChat);

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
    consumer.consume(update);

    verify(telegramClient).execute(any(SendMessage.class));
  }

  @Test
  @DisplayName("Catches RuntimeException gracefully and continues processing remaining updates in batch")
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
}
