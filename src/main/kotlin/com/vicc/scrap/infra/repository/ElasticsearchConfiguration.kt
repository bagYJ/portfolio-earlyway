package com.vicc.scrap.infra.repository

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import java.time.Duration

@Configuration
class ElasticsearchConfiguration {

    @Bean
    fun restHighLevelClient(): RestHighLevelClient {
        val config = ClientConfiguration.builder().connectedTo("localhost:9200")
            .withConnectTimeout(Duration.ofSeconds(5))
            .withSocketTimeout(Duration.ofSeconds(10))
//            .withBasicAuth(elasticsearch.username, elasticsearch.password)
            .build()
        return RestClients.create(config).rest()
    }
}