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
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Consumes incoming Telegram updates, verifies sender admission, and echoes plain-text messages
 * back to their originating chat and message thread.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/12/2026
 */
@Slf4j
@Component
@Profile("telegram-echo")
@RequiredArgsConstructor
public class TelegramEchoConsumer implements LongPollingUpdateConsumer {

  private final TelegramClient telegramClient;
  private final TelegramAdmissionPolicy admissionPolicy;

  @Override
  public void consume(List<Update> updates) {
    if (updates != null) {
      updates.forEach(this::consumeUpdate);
    }
  }

  private void consumeUpdate(Update update) {
    if (!isAuthorized(update)) {
      return;
    }
    createEchoMessage(update).ifPresent(this::sendEchoMessage);
  }

  private boolean isAuthorized(Update update) {
    if (update == null || !update.hasMessage()) {
      return false;
    }

    Message message = update.getMessage();
    if (message == null) {
      return false;
    }

    User from = message.getFrom();
    if (from == null) {
      return false;
    }

    Long senderId = from.getId();
    return admissionPolicy.isAdmitted(senderId);
  }

  private Optional<SendMessage> createEchoMessage(Update update) {
    if (update == null || !update.hasMessage()) {
      return Optional.empty();
    }

    Message message = update.getMessage();
    if (message == null || message.getChat() == null || message.getChat().getId() == null) {
      return Optional.empty();
    }

    String text = message.getText();
    if (text == null || text.isEmpty()) {
      return Optional.empty();
    }

    SendMessage.SendMessageBuilder<?, ?> builder =
        SendMessage.builder().chatId(message.getChat().getId().toString()).text(text);

    Integer threadId = message.getMessageThreadId();
    if (threadId != null) {
      builder.messageThreadId(threadId);
    }

    return Optional.of(builder.build());
  }

  private void sendEchoMessage(SendMessage echoMessage) {
    try {
      telegramClient.execute(echoMessage);
    } catch (Exception e) {
      log.error("Failed to send Telegram echo message: {}", e.getClass().getSimpleName());
    }
  }
}
