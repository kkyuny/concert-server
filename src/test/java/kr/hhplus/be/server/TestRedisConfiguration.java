package kr.hhplus.be.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@TestConfiguration
@EnableCaching
public class TestRedisConfiguration {

	@Primary
	@Bean
	public RedisCacheManager redisCacheManager(
			RedisConnectionFactory factory
	) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());

		Jackson2JsonRedisSerializer<Object> serializer =
				new Jackson2JsonRedisSerializer<>(
						mapper,
						Object.class
				);

		RedisCacheConfiguration config =
				RedisCacheConfiguration.defaultCacheConfig()
						.serializeValuesWith(
								RedisSerializationContext
										.SerializationPair
										.fromSerializer(serializer)
						);

		return RedisCacheManager.builder(factory)
				.cacheDefaults(config)
				.build();
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(
			RedisConnectionFactory factory
	) {

		RedisTemplate<String, Object> redisTemplate =
				new RedisTemplate<>();

		redisTemplate.setConnectionFactory(factory);
		redisTemplate.afterPropertiesSet();

		return redisTemplate;
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate(
			RedisConnectionFactory factory
	) {
		return new StringRedisTemplate(factory);
	}
}