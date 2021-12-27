package br.com.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import br.com.api.model.Product;
import br.com.api.repository.ProductRepository;
import br.com.api.tiny.dto.ContainerProductOutputDTO;
import br.com.api.tiny.dto.ResponseOutputDTO;

@Service
public class ProductServiceImpl implements ProductService {
	
	private final ProductRepository productRepository;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

	public ProductServiceImpl(ProductRepository productRepository) {
		super();
		this.productRepository = productRepository;
	}
	
	@Value("${tiny.api_url}")
	private String TINY_API_URL;

	@Value("${tiny.api_token}")
	private String TINY_API_TOKEN;
	
	@Value("${tiny.api_formato}")
	private String TINY_API_FORMATO;
	
	@Value("${tiny.api_get_products}")
	private String GET_PRODUCTS;

	@Override
	public Optional<Product> findByBarCode(String barCode) {
		LOGGER.info("Busca produto {}", barCode);
		return this.productRepository.findByBarCode(barCode);
	}
	
	@Override
	public void main() {
		Integer numberPages = null;
		Integer currentPage = 1;
		List<ContainerProductOutputDTO> productsTiny = new ArrayList<>();

		while (numberPages == null || currentPage <= numberPages) {
			ResponseOutputDTO responseOutputDTO = this.getAllProductsFromTinyErp(currentPage);
			productsTiny.addAll(responseOutputDTO.getResponse().getProducts());
			numberPages = responseOutputDTO.getResponse().getNumberPages();
			currentPage++;
		}
		
		productsTiny.stream().forEach(productTiny ->{
			Optional<Product> optionalProductDB = this.productRepository.findByBarCode(productTiny.getProduct().getCode());
			if(optionalProductDB.isPresent()) {
				Product oldProductDB = optionalProductDB.get();
				if(!"A".equalsIgnoreCase(productTiny.getProduct().getStatus())) {
					LOGGER.info("Excluindo produto excluido ou inativo " + productTiny.getProduct().getName());
					this.productRepository.delete(oldProductDB);
				}else {
					oldProductDB = oldProductDB.toBuilder()
							.barCode(productTiny.getProduct().getCode())
							.description(productTiny.getProduct().getName())
							.price1(productTiny.getProduct().getPrice())
							.price2(productTiny.getProduct().getPromotionalPrice())
							.dtUpdate(LocalDateTime.now())
							.build();
							LOGGER.info("Atualizando produto " + productTiny.getProduct().getName());
							this.productRepository.save(oldProductDB);						
				}
			}else {
				Product newProductDB = Product.builder()
						.barCode(productTiny.getProduct().getCode())
						.description(productTiny.getProduct().getName())
						.price1(productTiny.getProduct().getPrice())
						.price2(productTiny.getProduct().getPromotionalPrice())
						.dtCreate(LocalDateTime.now())
						.build();
				LOGGER.info("Inserindo produto " + productTiny.getProduct().getName());
				this.productRepository.save(newProductDB);
			}
		});
	}
	
	private ResponseOutputDTO getAllProductsFromTinyErp(int page) {
		String url = String.format("%s/%s?token=%s&formato=%s&pagina=%s", TINY_API_URL, GET_PRODUCTS, TINY_API_TOKEN, TINY_API_FORMATO, page);
		LOGGER.info("Obtendo produtos da API Tiny {}", url);
		RestTemplate restTemplate = this.buildRestTemplate();
		ResponseOutputDTO result = restTemplate.getForObject(url, ResponseOutputDTO.class);
		
		return result;
	}
	
	private RestTemplate buildRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();        
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));        
		messageConverters.add(converter);  
		restTemplate.setMessageConverters(messageConverters);
		
		return restTemplate; 
	}

}
