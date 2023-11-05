package com.svcsolicitarcredito.infrastructure.adapter;

import com.svcsolicitarcredito.domain.entity.ListaPedidoCredito;
import com.svcsolicitarcredito.domain.entity.PedidoCredito;
import com.svcsolicitarcredito.domain.entity.SearchCriteria;
import com.svcsolicitarcredito.domain.port.out.SearchPedidoPort;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ElasticSearchPedidoAdapter implements SearchPedidoPort {
  @Autowired
  private ElasticsearchOperations elasticsearchOperations;

  // Faz uma consulta avançada nos pedidos usando o critério de busca informado e retorna uma lista de pedidos
  @Override
  public List<ListaPedidoCredito> searchPedidos(SearchCriteria searchCriteria) {
    // Cria um objeto de consulta nativa do Elasticsearch
    NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
    // Cria um objeto de consulta booleana do Elasticsearch
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    // Adiciona os critérios de filtragem à consulta booleana, se informados

    if (searchCriteria.getCodigoCliente() != null) {
      boolQuery.must(QueryBuilders.matchQuery("codigo_cliente", searchCriteria.getCodigoCliente()));
    }
    if (searchCriteria.getProduto() != null) {
      boolQuery.must(QueryBuilders.matchQuery("produto", searchCriteria.getProduto()));
    }
    if (searchCriteria.getValorMinimo() != null) {
      boolQuery.filter(QueryBuilders.rangeQuery("valor").gte(searchCriteria.getValorMinimo()));
    }
    if (searchCriteria.getValorMaximo() != null) {
      boolQuery.filter(QueryBuilders.rangeQuery("valor").lte(searchCriteria.getValorMaximo()));
    }
    if (searchCriteria.getValorMinimo() != null) {
      boolQuery.filter(QueryBuilders.rangeQuery("data_pedido").gte(searchCriteria.getDataInicio()));
    }
    if (searchCriteria.getValorMaximo() != null) {
      boolQuery.filter(QueryBuilders.rangeQuery("data_pedido").lte(searchCriteria.getDataFim()));
    }
    if (searchCriteria.getStatus() != null) {
      boolQuery.must(QueryBuilders.matchQuery("status", searchCriteria.getStatus()));
    }
    // Adiciona a consulta booleana ao construtor de consulta nativa
    queryBuilder.withQuery(boolQuery);
    // Adiciona os critérios de ordenação ao construtor de consulta nativa, se informados
    //if (searchCriteria.getSortBy() != null) {
     // queryBuilder.withSort(SortBuilders.fieldSort(searchCriteria.getSortBy()));
    //}
    // Adiciona os critérios de paginação ao construtor de consulta nativa, se informados
    if (searchCriteria.getPageNumber() != null && searchCriteria.getPageSize() != null) {
      queryBuilder.withPageable(PageRequest.of(searchCriteria.getPageNumber(), searchCriteria.getPageSize()));
    }
    // Executa a consulta nativa usando o cliente do Spring Data Elasticsearch e obtém os resultados
    SearchHits<ListaPedidoCredito> searchHits = elasticsearchOperations.search(queryBuilder.build(), ListaPedidoCredito.class);
    // Converte os resultados em uma lista de pedidos e retorna
    return searchHits.stream().map(hit -> hit.getContent()).collect(Collectors.toList());
  }
}
