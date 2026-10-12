package me.nghlong3004.lifeos.telegram;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Telegram bot integration.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/12/2026
 */
@ConfigurationProperties(prefix = "lifeos.telegram")
public record TelegramProperties(String botToken) {

  /**
   * Returns whether a non-blank bot token is configured.
   *
   * @return {@code true} if the token is non-null and not blank, {@code false} otherwise
   */
  public boolean hasValidToken() {
    return botToken != null && !botToken.isBlank();
  }

  @Override
  public String toString() {
    return "TelegramProperties[botToken=" + (hasValidToken() ? "[PROTECTED]" : "[EMPTY]") + "]";
  }
}
