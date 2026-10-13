package me.nghlong3004.lifeos.telegram;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

/**
 * Immutable admission policy governing Telegram sender authorization.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/13/2026
 */
@Getter
@Accessors(fluent = true)
public final class TelegramAdmissionPolicy {

  private static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

  private final TelegramAccessMode mode;
  private final Set<Long> allowedUserIds;

  private TelegramAdmissionPolicy(TelegramAccessMode mode, Set<Long> allowedUserIds) {
    this.mode = mode;
    this.allowedUserIds = Collections.unmodifiableSet(new HashSet<>(allowedUserIds));
  }

  public static TelegramAdmissionPolicy from(TelegramProperties properties) {
    if (properties == null) {
      throw new IllegalStateException("Invalid configuration for lifeos.telegram.access-mode");
    }
    return from(properties.accessMode(), properties.allowedUserIds());
  }

  public static TelegramAdmissionPolicy from(
      @Nullable String rawMode, @Nullable String rawAllowedUserIds) {
    TelegramAccessMode mode = TelegramAccessMode.parseStrict(rawMode);
    Set<Long> parsedIds = parseAllowedUserIds(mode, rawAllowedUserIds);
    return new TelegramAdmissionPolicy(mode, parsedIds);
  }

  public boolean isAdmitted(@Nullable Long senderId) {
    if (senderId == null || senderId <= 0) {
      return false;
    }
    return switch (mode) {
      case PUBLIC -> true;
      case WHITELIST -> allowedUserIds.contains(senderId);
    };
  }

  private static Set<Long> parseAllowedUserIds(
      TelegramAccessMode mode, @Nullable String rawAllowedUserIds) {
    String trimmed = rawAllowedUserIds != null ? rawAllowedUserIds.trim() : "";
    if (trimmed.isEmpty()) {
      if (mode == TelegramAccessMode.WHITELIST) {
        throw new IllegalStateException(
            "Invalid configuration for lifeos.telegram.allowed-user-ids");
      }
      return Collections.emptySet();
    }

    if (trimmed.startsWith(",") || trimmed.endsWith(",")) {
      throw new IllegalStateException(
          "Invalid configuration for lifeos.telegram.allowed-user-ids");
    }

    String[] entries = trimmed.split(",", -1);
    Set<Long> ids = new HashSet<>();
    for (String entry : entries) {
      String entryTrimmed = entry.trim();
      if (entryTrimmed.isEmpty()) {
        throw new IllegalStateException(
            "Invalid configuration for lifeos.telegram.allowed-user-ids");
      }
      if (!entryTrimmed.matches("^[0-9]+$")) {
        throw new IllegalStateException(
            "Invalid configuration for lifeos.telegram.allowed-user-ids");
      }
      if (entryTrimmed.matches("^0+$")) {
        throw new IllegalStateException(
            "Invalid configuration for lifeos.telegram.allowed-user-ids");
      }
      BigInteger value = new BigInteger(entryTrimmed);
      if (value.compareTo(MAX_LONG) > 0) {
        throw new IllegalStateException(
            "Invalid configuration for lifeos.telegram.allowed-user-ids");
      }
      ids.add(value.longValue());
    }

    if (mode == TelegramAccessMode.WHITELIST && ids.isEmpty()) {
      throw new IllegalStateException(
          "Invalid configuration for lifeos.telegram.allowed-user-ids");
    }

    return ids;
  }
}
