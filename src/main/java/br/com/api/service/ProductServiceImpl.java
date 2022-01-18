package br.com.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
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
	
	@Value("${tiny.api_format}")
	private String TINY_API_FORMAT;
	
	@Value("${tiny.api_retry}")
	private Long TINY_API_RETRY;
	
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
			
			if("erro".equalsIgnoreCase(responseOutputDTO.getResponse().getStatus())){
				try {
					LOGGER.info("API Tiny retornou erro. Aguardando alguns segundos para continuar.");
					Thread.sleep(TINY_API_RETRY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
			}else {
				productsTiny.addAll(responseOutputDTO.getResponse().getProducts());
				numberPages = responseOutputDTO.getResponse().getNumberPages();
				currentPage++;	
			}
		}
		
		productsTiny.stream().forEach(productTiny ->{
			if(StringUtils.isNotBlank(productTiny.getProduct().getCode())) {
				Optional<Product> optionalProductDB = this.productRepository.findByBarCode(productTiny.getProduct().getGtin());
				if(optionalProductDB.isPresent()) {
					Product oldProductDB = optionalProductDB.get();
					if(!"A".equalsIgnoreCase(productTiny.getProduct().getStatus())) {
						LOGGER.info("Excluindo produto excluido ou inativo " + productTiny.getProduct().getName());
						this.productRepository.delete(oldProductDB);
					}else {
						LOGGER.info("Atualizando produto " + productTiny.getProduct().getName());
						this.updateProductDB(oldProductDB, productTiny);
					}
				}else {
					if("A".equalsIgnoreCase(productTiny.getProduct().getStatus())) {
						LOGGER.info("Inserindo produto " + productTiny.getProduct().getName());
						this.createProductDB(productTiny);						
					}
				}				
			}
		});
	}
	
	private void updateProductDB(Product oldProductDB, ContainerProductOutputDTO productTiny) {
		oldProductDB = oldProductDB.toBuilder()
				.barCode(productTiny.getProduct().getGtin())
				.description(productTiny.getProduct().getName())
				.price1(productTiny.getProduct().getPrice())
				.price2(this.getPromotionalPrice(productTiny))
				.dtUpdate(LocalDateTime.now())
				.build();
		this.productRepository.save(oldProductDB);
	}
	
	private BigDecimal getPromotionalPrice(ContainerProductOutputDTO productTiny) {
		BigDecimal promotionalPrice = productTiny.getProduct().getPromotionalPrice();
		return promotionalPrice != null && promotionalPrice.compareTo(BigDecimal.ZERO) > 0 ? promotionalPrice : null;
	}
	
	private void createProductDB(ContainerProductOutputDTO productTiny) {
		Product newProductDB = Product.builder()
				.barCode(productTiny.getProduct().getGtin())
				.description(productTiny.getProduct().getName())
				.price1(productTiny.getProduct().getPrice())
				.price2(this.getPromotionalPrice(productTiny))
				.dtCreate(LocalDateTime.now())
				.build();
		this.productRepository.save(newProductDB);
	}
	
	private ResponseOutputDTO getAllProductsFromTinyErp(int page) {
		String url = String.format("%s/%s?token=%s&formato=%s&pagina=%s", TINY_API_URL, GET_PRODUCTS, TINY_API_TOKEN, TINY_API_FORMAT, page);
		LOGGER.info("Obtendo produtos da API Tiny {}", url);
		RestTemplate restTemplate = this.buildRestTemplate();
		return restTemplate.getForObject(url, ResponseOutputDTO.class);
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
