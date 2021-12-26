package br.com.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.api.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	
	Optional<Product> findByBarCode(@Param("barCode") String barCode);

}
