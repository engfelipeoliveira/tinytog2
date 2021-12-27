package br.com.api.service;

import java.util.Optional;

import br.com.api.model.Product;

public interface ProductService {
	
	void main();
	Optional<Product> findByBarCode(String barCode);
	
}
