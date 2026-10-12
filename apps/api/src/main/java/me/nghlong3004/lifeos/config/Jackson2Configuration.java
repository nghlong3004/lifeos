package me.nghlong3004.lifeos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared configuration providing a Jackson 2 {@link ObjectMapper} bean.
 *
 * <p>Spring Boot 4.x native web auto-configuration targets Jackson 3
 * ({@code tools.jackson.databind.ObjectMapper}). External third-party integrations,
 * such as TelegramBots 10.3.0, require Jackson 2 ({@code com.fasterxml.jackson.databind.ObjectMapper}).
 * This configuration supplies a shared, reusable Jackson 2 mapper for libraries requiring it,
 * without replacing or interfering with Boot's Jackson 3 HTTP auto-configuration.</p>
 *
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/12/2026
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ObjectMapper.class)
public class Jackson2Configuration {

  @Bean
  @ConditionalOnMissingBean(ObjectMapper.class)
  public ObjectMapper jackson2ObjectMapper() {
    return new ObjectMapper();
  }
}
