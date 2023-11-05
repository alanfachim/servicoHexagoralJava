package com.svcsolicitarcredito.infrastructure.repository.es;

import com.svcsolicitarcredito.domain.entity.ListaPedidoCredito;
import com.svcsolicitarcredito.domain.entity.SolicitarCreditoID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.svcsolicitarcredito.domain.entity.PedidoCredito;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ElasticsearchDBPedidoRepository extends ElasticsearchRepository<ListaPedidoCredito, UUID> {


}
//    @Query("{\"bool\": {\"must\": [{\"match\": {\"authors.name\": \"?0\"}}]}}")
//    Page<PedidoCredito> findByAuthorsNameUsingCustomQuery(String name, Pageable pageable);
//
//    @Query("{\"bool\": {\"must\": {\"match_all\": {}}, \"filter\": {\"term\": {\"tags\": \"?0\" }}}}")
//    Page<PedidoCredito> findByFilteredTagQuery(String tag, Pageable pageable);
//
//@Query("{\"bool\": {\"must\": {\"match\": {\"authors.name\": \"?0\"}}, \"filter\": {\"term\": {\"tags\": \"?1\" }}}}")
//    Page<PedidoCredito> findByAuthorsNameAndFilteredTagQuery(String name, String tag, Pageable pageable);
