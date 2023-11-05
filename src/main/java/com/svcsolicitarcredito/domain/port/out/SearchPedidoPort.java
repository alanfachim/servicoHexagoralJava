package com.svcsolicitarcredito.domain.port.out;

import com.svcsolicitarcredito.domain.entity.ListaPedidoCredito;
import com.svcsolicitarcredito.domain.entity.PedidoCredito;
import com.svcsolicitarcredito.domain.entity.SearchCriteria;

import java.util.List;

public interface SearchPedidoPort {

  // Faz uma consulta avançada nos pedidos usando o critério de busca informado e retorna uma lista de pedidos
  List<ListaPedidoCredito> searchPedidos(SearchCriteria searchCriteria);

}
