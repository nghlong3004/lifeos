package me.nghlong3004.lifeos.telegram;

/**
 * Access control modes for Telegram sender admission.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/13/2026
 */
public enum TelegramAccessMode {
  WHITELIST,
  PUBLIC;

  /**
   * Parses the mode string strictly. Case-sensitive after trimming surrounding whitespace.
   *
   * @param rawMode the raw mode string
   * @return the resolved {@link TelegramAccessMode}
   * @throws IllegalStateException if null, blank, or unknown
   */
  public static TelegramAccessMode parseStrict(String rawMode) {
    if (rawMode == null) {
      throw new IllegalStateException("Invalid configuration for lifeos.telegram.access-mode");
    }
    String trimmed = rawMode.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalStateException("Invalid configuration for lifeos.telegram.access-mode");
    }
    if ("WHITELIST".equals(trimmed)) {
      return WHITELIST;
    }
    if ("PUBLIC".equals(trimmed)) {
      return PUBLIC;
    }
    throw new IllegalStateException("Invalid configuration for lifeos.telegram.access-mode");
  }
}
