package com.mediquitous.productpoc.config

import com.mediquitous.productpoc.service.event.ProductUpdatedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import java.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * Kafka 설정 프로퍼티
 *
 */
@ConfigurationProperties(prefix = "kafka")
data class KafkaProperties(
    val brokers: List<String> = listOf("localhost:9092"),
    val groupName: String = "product-search-service",
    val topicPrefix: String = "",
    val security: KafkaSecurityProperties = KafkaSecurityProperties(),
    val producer: KafkaProducerProperties = KafkaProducerProperties(),
    val consumer: KafkaConsumerProperties = KafkaConsumerProperties(),
)

/**
 * 보안 프로토콜
 */
enum class SecurityProtocol {
    PLAINTEXT,
    TLS,
    SASL_SCRAM,
    SASL_PLAINTEXT,
    SASL_IAM,
}

data class KafkaSecurityProperties(
    val protocol: SecurityProtocol = SecurityProtocol.PLAINTEXT,
    val mechanism: String = "SCRAM-SHA-512",
    val username: String? = null,
    val password: String? = null,
    val awsRegion: String? = null,
)

data class KafkaProducerProperties(
    val maxRetry: Int = 3,
    val retryBackoff: Duration = Duration.ofMillis(100),
    val acks: String = "all",
    val batchSize: Int = 16384,
    val lingerMs: Int = 5,
    val bufferMemory: Long = 33554432,
)

data class KafkaConsumerProperties(
    val maxWaitTime: Duration = Duration.ofMillis(500),
    val retryBackoff: Duration = Duration.ofMillis(100),
    val sessionTimeout: Duration = Duration.ofSeconds(30),
    val heartbeatInterval: Duration = Duration.ofSeconds(10),
    val semaphoreLimit: Int = 10,
    val autoOffsetReset: String = "earliest",
    val enableAutoCommit: Boolean = false,
)

/**
 * Kafka Configuration
 *
 * Producer/Consumer 설정 및 보안 프로토콜 지원
 */
@Configuration
@EnableConfigurationProperties(KafkaProperties::class)
@ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class KafkaConfig(
    private val kafkaProperties: KafkaProperties,
) {
    // =====================================================
    // Producer Configuration
    // =====================================================

    @Bean
    fun producerFactory(): ProducerFactory<String, ProductUpdatedEvent> {
        val configProps = buildCommonConfig().toMutableMap()

        configProps.apply {
            // Producer 기본 설정
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer::class.java)

            // Producer 성능 설정
            put(ProducerConfig.ACKS_CONFIG, kafkaProperties.producer.acks)
            put(ProducerConfig.RETRIES_CONFIG, kafkaProperties.producer.maxRetry)
            put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, kafkaProperties.producer.retryBackoff.toMillis())
            put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaProperties.producer.batchSize)
            put(ProducerConfig.LINGER_MS_CONFIG, kafkaProperties.producer.lingerMs)
            put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafkaProperties.producer.bufferMemory)

            // 멱등성 보장
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true)
        }

        logger.info { "Kafka Producer 설정 완료: brokers=${kafkaProperties.brokers}" }
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, ProductUpdatedEvent>): KafkaTemplate<String, ProductUpdatedEvent> =
        KafkaTemplate(producerFactory)

    // =====================================================
    // Consumer Configuration
    // =====================================================

    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val configProps = buildCommonConfig().toMutableMap()

        configProps.apply {
            // Consumer 기본 설정
            put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.groupName)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)

            // Consumer 성능 설정
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.consumer.autoOffsetReset)
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaProperties.consumer.enableAutoCommit)
            put(
                ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,
                kafkaProperties.consumer.sessionTimeout
                    .toMillis()
                    .toInt(),
            )
            put(
                ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG,
                kafkaProperties.consumer.heartbeatInterval
                    .toMillis()
                    .toInt(),
            )
            put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000) // 5분
            put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500)
        }

        logger.info { "Kafka Consumer 설정 완료: groupId=${kafkaProperties.groupName}" }
        return DefaultKafkaConsumerFactory(configProps)
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>,
    ): ConcurrentKafkaListenerContainerFactory<String, String> =
        ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            this.consumerFactory = consumerFactory
            setConcurrency(kafkaProperties.consumer.semaphoreLimit)
            containerProperties.pollTimeout = kafkaProperties.consumer.maxWaitTime.toMillis()
        }

    /**
     * JSON 역직렬화용 Consumer Factory
     */
    @Bean
    fun jsonConsumerFactory(): ConsumerFactory<String, ProductUpdatedEvent> {
        val configProps = buildCommonConfig().toMutableMap()

        configProps.apply {
            put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.groupName)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer::class.java)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.consumer.autoOffsetReset)
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaProperties.consumer.enableAutoCommit)
            put(JsonDeserializer.TRUSTED_PACKAGES, "com.mediquitous.productpoc.*")
        }

        return DefaultKafkaConsumerFactory(configProps)
    }

    // =====================================================
    // Security Configuration
    // =====================================================

    /**
     * 공통 설정 빌드 (보안 프로토콜 포함)
     */
    private fun buildCommonConfig(): Map<String, Any> {
        val config = mutableMapOf<String, Any>()

        // 브로커 설정
        config[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = kafkaProperties.brokers.joinToString(",")

        // 네트워크 타임아웃
        config[CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG] = 30000
        config[CommonClientConfigs.CONNECTIONS_MAX_IDLE_MS_CONFIG] = 540000

        // 보안 프로토콜 적용
        applySecurityConfig(config)

        return config
    }

    /**
     * 보안 프로토콜에 따른 설정 적용
     */
    private fun applySecurityConfig(config: MutableMap<String, Any>) {
        val security = kafkaProperties.security

        when (security.protocol) {
            SecurityProtocol.PLAINTEXT -> {
                config[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "PLAINTEXT"
                logger.info { "Kafka 보안: PLAINTEXT (인증 없음)" }
            }

            SecurityProtocol.TLS -> {
                config[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL"
                logger.info { "Kafka 보안: TLS" }
            }

            SecurityProtocol.SASL_PLAINTEXT -> {
                applySaslConfig(config, security, useTls = false)
            }

            SecurityProtocol.SASL_SCRAM -> {
                applySaslConfig(config, security, useTls = true)
            }

            SecurityProtocol.SASL_IAM -> {
                applySaslIamConfig(config, security)
            }
        }
    }

    /**
     * SASL SCRAM 인증 설정
     */
    private fun applySaslConfig(
        config: MutableMap<String, Any>,
        security: KafkaSecurityProperties,
        useTls: Boolean,
    ) {
        require(!security.username.isNullOrBlank()) { "SASL 인증에는 username이 필요합니다" }
        require(!security.password.isNullOrBlank()) { "SASL 인증에는 password가 필요합니다" }

        config[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = if (useTls) "SASL_SSL" else "SASL_PLAINTEXT"
        config[SaslConfigs.SASL_MECHANISM] = security.mechanism

        val jaasConfig =
            """
            org.apache.kafka.common.security.scram.ScramLoginModule required
            username="${security.username}"
            password="${security.password}";
            """.trimIndent().replace("\n", " ")

        config[SaslConfigs.SASL_JAAS_CONFIG] = jaasConfig

        logger.info { "Kafka 보안: SASL ${security.mechanism} (TLS=$useTls)" }
    }

    /**
     * AWS IAM 인증 설정 (MSK용)
     */
    private fun applySaslIamConfig(
        config: MutableMap<String, Any>,
        security: KafkaSecurityProperties,
    ) {
        require(!security.awsRegion.isNullOrBlank()) { "SASL_IAM 인증에는 AWS Region이 필요합니다" }

        config[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SASL_SSL"
        config[SaslConfigs.SASL_MECHANISM] = "AWS_MSK_IAM"

        val jaasConfig =
            """
            software.amazon.msk.auth.iam.IAMLoginModule required;
            """.trimIndent()

        config[SaslConfigs.SASL_JAAS_CONFIG] = jaasConfig
        config[SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS] = "software.amazon.msk.auth.iam.IAMClientCallbackHandler"

        logger.info { "Kafka 보안: AWS MSK IAM (region=${security.awsRegion})" }
    }
}
