package me.nghlong3004.lifeos.telegram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Isolated unit tests for {@link TelegramAdmissionPolicy} and {@link TelegramAccessMode}.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/13/2026
 */
class TelegramAdmissionPolicyTest {

  @Test
  @DisplayName("Parses valid access modes with surrounding whitespace trimming")
  void parseMode_validValues_parsedCorrectly() {
    assertThat(TelegramAccessMode.parseStrict("WHITELIST")).isEqualTo(TelegramAccessMode.WHITELIST);
    assertThat(TelegramAccessMode.parseStrict("  WHITELIST  "))
        .isEqualTo(TelegramAccessMode.WHITELIST);
    assertThat(TelegramAccessMode.parseStrict("PUBLIC")).isEqualTo(TelegramAccessMode.PUBLIC);
    assertThat(TelegramAccessMode.parseStrict("\tPUBLIC\n")).isEqualTo(TelegramAccessMode.PUBLIC);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "   ",
        "whitelist",
        "public",
        "Whitelist",
        "Public",
        "UNKNOWN",
        "ADMIN",
        "WHITELIST extra"
      })
  @DisplayName("Rejects invalid, lowercase, and unknown access modes with sanitized diagnostics")
  void parseMode_invalidValues_throwsSanitizedException(String invalidMode) {
    assertThatThrownBy(() -> TelegramAccessMode.parseStrict(invalidMode))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Invalid configuration for lifeos.telegram.access-mode");
  }

  @Test
  @DisplayName("Rejects null access mode with sanitized diagnostics and no fallback")
  void parseMode_nullValue_throwsSanitizedException() {
    assertThatThrownBy(() -> TelegramAccessMode.parseStrict(null))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Invalid configuration for lifeos.telegram.access-mode");
  }

  @Test
  @DisplayName("Parses valid allowed user IDs in WHITELIST mode, trimming entries and collapsing duplicates")
  void from_whitelistMode_validIds() {
    TelegramAdmissionPolicy policy =
        TelegramAdmissionPolicy.from("WHITELIST", " 1001 , 1002 , 1001 , 9223372036854775807 ");

    assertThat(policy.mode()).isEqualTo(TelegramAccessMode.WHITELIST);
    assertThat(policy.allowedUserIds())
        .containsExactlyInAnyOrder(1001L, 1002L, 9223372036854775807L);
    assertThat(policy.isAdmitted(1001L)).isTrue();
    assertThat(policy.isAdmitted(1002L)).isTrue();
    assertThat(policy.isAdmitted(9223372036854775807L)).isTrue();
    assertThat(policy.isAdmitted(9999L)).isFalse();

    assertThatThrownBy(() -> policy.allowedUserIds().add(9999L))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThat(policy.isAdmitted(9999L)).isFalse();
  }

  @Test
  @DisplayName("Accepts multiline CSV with newline whitespace between entries")
  void from_whitelistMode_multilineValidIds() {
    TelegramAdmissionPolicy policy =
        TelegramAdmissionPolicy.from("WHITELIST", "1001,\n1002,\r\n1003");

    assertThat(policy.allowedUserIds()).containsExactlyInAnyOrder(1001L, 1002L, 1003L);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "   ",
        ",1001",
        "1001,",
        "1001,\n",
        "\n,1001",
        "1001,,1002",
        "1001,   ,1002",
        "+1001",
        "-1001",
        "0",
        "00",
        "abc",
        "12a",
        "12.34",
        "9223372036854775808",
        "10000000000000000000",
        "1001\n1002",
        "1001\ninvalid",
        "1001,1002\ninvalid"
      })
  @DisplayName("Rejects malformed, signed, zero, overflowing, or empty elements in WHITELIST mode")
  void from_whitelistMode_invalidIds_throwsSanitizedException(String invalidList) {
    assertThatThrownBy(() -> TelegramAdmissionPolicy.from("WHITELIST", invalidList))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Invalid configuration for lifeos.telegram.allowed-user-ids");
  }

  @Test
  @DisplayName("Rejects null allowed user IDs in WHITELIST mode")
  void from_whitelistMode_nullList_throwsSanitizedException() {
    assertThatThrownBy(() -> TelegramAdmissionPolicy.from("WHITELIST", null))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Invalid configuration for lifeos.telegram.allowed-user-ids");
  }

  @Test
  @DisplayName("PUBLIC mode permits absent or empty allowed user IDs and admits any identified sender")
  void from_publicMode_absentOrEmptyList_admitsAnyIdentifiedSender() {
    for (String emptyList : new String[] {null, "", "   "}) {
      TelegramAdmissionPolicy policy = TelegramAdmissionPolicy.from("PUBLIC", emptyList);

      assertThat(policy.mode()).isEqualTo(TelegramAccessMode.PUBLIC);
      assertThat(policy.allowedUserIds()).isEmpty();
      assertThat(policy.isAdmitted(12345L)).isTrue();
      assertThat(policy.isAdmitted(999999L)).isTrue();
    }
  }

  @Test
  @DisplayName("PUBLIC mode validates syntax of nonempty supplied list but does not restrict admission")
  void from_publicMode_validNonemptyList_doesNotRestrictAdmission() {
    TelegramAdmissionPolicy policy = TelegramAdmissionPolicy.from("PUBLIC", "1001, 1002");

    assertThat(policy.mode()).isEqualTo(TelegramAccessMode.PUBLIC);
    assertThat(policy.allowedUserIds()).containsExactlyInAnyOrder(1001L, 1002L);
    assertThat(policy.isAdmitted(1001L)).isTrue();
    assertThat(policy.isAdmitted(9999L)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        ",1001",
        "1001,",
        "1001,,1002",
        "+1001",
        "-1001",
        "0",
        "abc",
        "9223372036854775808",
        "1001\ninvalid"
      })
  @DisplayName("PUBLIC mode fails closed when supplied list has invalid syntax")
  void from_publicMode_invalidSuppliedList_failsClosed(String invalidList) {
    assertThatThrownBy(() -> TelegramAdmissionPolicy.from("PUBLIC", invalidList))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Invalid configuration for lifeos.telegram.allowed-user-ids");
  }

  @Test
  @DisplayName("Denies null and nonpositive sender IDs in both WHITELIST and PUBLIC modes")
  void isAdmitted_missingOrNonpositiveSender_deniedInBothModes() {
    TelegramAdmissionPolicy whitelistPolicy = TelegramAdmissionPolicy.from("WHITELIST", "1001");
    TelegramAdmissionPolicy publicPolicy = TelegramAdmissionPolicy.from("PUBLIC", null);

    for (Long invalidId : new Long[] {null, 0L, -1L, -9999L}) {
      assertThat(whitelistPolicy.isAdmitted(invalidId)).isFalse();
      assertThat(publicPolicy.isAdmitted(invalidId)).isFalse();
    }
  }

  @Test
  @DisplayName("TelegramProperties toString protects botToken and allowedUserIds")
  void telegramProperties_toString_masksSensitiveFields() {
    TelegramProperties properties =
        new TelegramProperties("secret-bot-token-xyz", "WHITELIST", "123456789");

    String rendered = properties.toString();
    assertThat(rendered).doesNotContain("secret-bot-token-xyz");
    assertThat(rendered).doesNotContain("123456789");
    assertThat(rendered).contains("[PROTECTED]");
  }
}
