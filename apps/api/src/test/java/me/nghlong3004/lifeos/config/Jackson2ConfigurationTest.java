package me.nghlong3004.lifeos.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Isolated unit tests for {@link Jackson2Configuration}.
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/12/2026
 */
class Jackson2ConfigurationTest {

  @Test
  @DisplayName("jackson2ObjectMapper provides a functional Jackson 2 mapper capable of JSON serialization")
  void jackson2ObjectMapper_serializesJsonSuccessfully() throws Exception {
    Jackson2Configuration configuration = new Jackson2Configuration();
    ObjectMapper mapper = configuration.jackson2ObjectMapper();

    assertThat(mapper).isNotNull();
    String json = mapper.writeValueAsString(Map.of("status", "ok"));
    assertThat(json).isEqualTo("{\"status\":\"ok\"}");

    @SuppressWarnings("unchecked")
    Map<String, Object> read = mapper.readValue(json, Map.class);
    assertThat(read).containsEntry("status", "ok");
  }
}
