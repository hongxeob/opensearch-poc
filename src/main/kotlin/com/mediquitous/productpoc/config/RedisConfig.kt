package com.mediquitous.productpoc.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * Redis 설정 프로퍼티
 *
 * Go 서버의 redis/config.go를 Spring Boot 스타일로 변환
 */
@ConfigurationProperties(prefix = "redis")
data class RedisProperties(
    val read: RedisNodeProperties = RedisNodeProperties(),
    val write: RedisNodeProperties = RedisNodeProperties(),
)

data class RedisNodeProperties(
    val host: String = "localhost",
    val port: Int = 6379,
    val password: String? = null,
    val database: Int = 0,
    val pool: RedisPoolProperties = RedisPoolProperties(),
    val timeout: RedisTimeoutProperties = RedisTimeoutProperties(),
    val maxRetries: Int = 3,
    val enableTls: Boolean = false,
)

data class RedisPoolProperties(
    val size: Int = 10,
    val minIdle: Int = 2,
    val maxActive: Int = 10,
)

data class RedisTimeoutProperties(
    val dial: Duration = Duration.ofSeconds(5),
    val read: Duration = Duration.ofSeconds(3),
    val write: Duration = Duration.ofSeconds(3),
)

/**
 * Redis Configuration
 *
 * 읽기/쓰기 분리 Redis 클라이언트 설정
 */
@Configuration
@EnableConfigurationProperties(RedisProperties::class)
class RedisConfig(
    private val redisProperties: RedisProperties,
) {
    /**
     * 쓰기용 Redis Connection Factory
     */
    @Bean(name = ["writeRedisConnectionFactory"])
    fun writeRedisConnectionFactory(): RedisConnectionFactory {
        val props = redisProperties.write
        logger.info { "Redis Write 연결 설정: ${props.host}:${props.port}, DB=${props.database}" }

        val standaloneConfig =
            RedisStandaloneConfiguration().apply {
                hostName = props.host
                port = props.port
                database = props.database
                props.password?.takeIf { it.isNotBlank() }?.let { setPassword(it) }
            }

        val clientConfig =
            LettuceClientConfiguration
                .builder()
                .commandTimeout(props.timeout.read)
                .apply {
                    if (props.enableTls) {
                        useSsl()
                    }
                }.build()

        return LettuceConnectionFactory(standaloneConfig, clientConfig)
    }

    /**
     * 읽기용 Redis Connection Factory
     */
    @Bean(name = ["readRedisConnectionFactory"])
    fun readRedisConnectionFactory(): RedisConnectionFactory {
        val props = redisProperties.read
        logger.info { "Redis Read 연결 설정: ${props.host}:${props.port}, DB=${props.database}" }

        val standaloneConfig =
            RedisStandaloneConfiguration().apply {
                hostName = props.host
                port = props.port
                database = props.database
                props.password?.takeIf { it.isNotBlank() }?.let { setPassword(it) }
            }

        val clientConfig =
            LettuceClientConfiguration
                .builder()
                .commandTimeout(props.timeout.read)
                .apply {
                    if (props.enableTls) {
                        useSsl()
                    }
                }.build()

        return LettuceConnectionFactory(standaloneConfig, clientConfig)
    }

    /**
     * 기본 RedisTemplate (쓰기용)
     */
    @Bean
    fun redisTemplate(writeRedisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String> =
        RedisTemplate<String, String>().apply {
            connectionFactory = writeRedisConnectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()
        }

    /**
     * 읽기 전용 RedisTemplate
     */
    @Bean(name = ["readRedisTemplate"])
    fun readRedisTemplate(readRedisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String> =
        RedisTemplate<String, String>().apply {
            connectionFactory = readRedisConnectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()
        }
}
