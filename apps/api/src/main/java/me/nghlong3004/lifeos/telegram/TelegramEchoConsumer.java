package me.nghlong3004.lifeos.telegram;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Telegram update consumer for the minimal plain-text echo integration.
 * Receives incoming updates, checks for regular text messages, and echoes back
 * the exact incoming text to the originating chat and message thread/topic.
 * Uses Lombok {@link RequiredArgsConstructor} for constructor injection.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/12/2026
 */
@Component
@Profile("telegram-echo")
@RequiredArgsConstructor
@Slf4j
public class TelegramEchoConsumer implements LongPollingUpdateConsumer {

  private final TelegramClient telegramClient;

  @Override
  public void consume(List<Update> updates) {
    if (updates == null || updates.isEmpty()) {
      return;
    }
    for (Update update : updates) {
      consume(update);
    }
  }

  /**
   * Consumes a single update, extracting text and echoing it back if valid.
   *
   * @param update the incoming Telegram update
   */
  public void consume(Update update) {
    createEchoMessage(update).ifPresent(this::sendEchoMessage);
  }

  private Optional<SendMessage> createEchoMessage(Update update) {
    if (update == null || !update.hasMessage()) {
      return Optional.empty();
    }

    Message message = update.getMessage();
    if (message == null
        || !message.hasText()
        || message.getChat() == null
        || message.getChat().getId() == null) {
      return Optional.empty();
    }

    String text = message.getText();
    if (text == null || text.isEmpty()) {
      return Optional.empty();
    }

    Long chatId = message.getChat().getId();

    SendMessage.SendMessageBuilder<?, ?> builder =
        SendMessage.builder()
            .chatId(chatId.toString())
            .text(text);

    Integer messageThreadId = message.getMessageThreadId();
    if (messageThreadId != null) {
      builder.messageThreadId(messageThreadId);
    }

    return Optional.of(builder.build());
  }

  private void sendEchoMessage(SendMessage echoMessage) {
    try {
      telegramClient.execute(echoMessage);
    } catch (Exception e) {
      // Handle send failures without leaking bot token or message contents
      log.error("Failed to send Telegram echo message: {}", e.getClass().getSimpleName());
    }
  }
}
