package com.svcsolicitarcredito.infrastructure.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

// Classe para configurar o ElasticSearch
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.svcsolicitarcredito.infrastructure.repository.es")
public class ElasticSearchConfig {

  @Value("${elasticsearch.host}")
  private String elasticsearchHost;

  @Value("${elasticsearch.port}")
  private int elasticsearchPort;

  @Bean
  public RestHighLevelClient client() {
    ClientConfiguration clientConfiguration = ClientConfiguration.builder()
      .connectedTo(elasticsearchHost + ":" + elasticsearchPort)
      .build();
    return RestClients.create(clientConfiguration).rest();
  }

  @Bean
  public ElasticsearchOperations elasticsearchTemplate() {
    return new ElasticsearchRestTemplate(client());
  }
}