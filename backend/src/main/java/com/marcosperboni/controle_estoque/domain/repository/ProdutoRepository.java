package com.marcosperboni.controle_estoque.domain.repository;

import com.marcosperboni.controle_estoque.domain.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarrasAndIdNot(String codigoBarras, Long id);
}
