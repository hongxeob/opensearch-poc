package com.mediquitous.productpoc.config

import org.apache.hc.client5.http.auth.AuthScope
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.ssl.SSLContextBuilder
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

@Configuration
class OpenSearchConfig {
    @Bean
    @ConfigurationProperties(prefix = "opensearch")
    fun openSearchProperties(): OpenSearchProperties = OpenSearchProperties()

    @Bean
    fun openSearchClient(properties: OpenSearchProperties): OpenSearchClient {
        val uri = URI(properties.address)

        val host =
            HttpHost(
                uri.scheme,
                uri.host,
                if (uri.port == -1) 443 else uri.port,
            )

        val credentialsProvider =
            BasicCredentialsProvider().apply {
                setCredentials(
                    AuthScope(null, -1),
                    UsernamePasswordCredentials(
                        properties.username,
                        properties.password.toCharArray(),
                    ),
                )
            }

        val sslContext =
            SSLContextBuilder
                .create()
                .loadTrustMaterial(null) { _, _ -> true }
                .build()

        val tlsStrategy =
            ClientTlsStrategyBuilder
                .create()
                .setSslContext(sslContext)
                .setHostnameVerifier { _, _ -> true }
                .build()

        val connectionManager: PoolingAsyncClientConnectionManager =
            PoolingAsyncClientConnectionManagerBuilder
                .create()
                .setTlsStrategy(tlsStrategy)
                .build()

        val transport =
            ApacheHttpClient5TransportBuilder
                .builder(host)
                .setHttpClientConfigCallback { httpClientBuilder ->
                    httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setConnectionManager(connectionManager)
                }.build()

        return OpenSearchClient(transport)
    }

    private fun parseHost(hostStr: String): HttpHost {
        val parts = hostStr.split(":")
        val host = parts[0]
        val port = if (parts.size > 1) parts[1].toInt() else 9200
        return HttpHost("https", host, port)
    }

    data class OpenSearchProperties(
        var address: String = "https://opensearch-cluster.dev.nugu.jp",
        var localhost: String = "localhost:9200",
        var username: String = "",
        var password: String = "",
        var connectionTimeout: Int = 5000,
        var socketTimeout: Int = 60000,
        var indices: Indices = Indices(),
    ) {
        data class Indices(
            var products: String = "zelda-products",
            var sellers: String = "zelda-sellers",
        )
    }
}
