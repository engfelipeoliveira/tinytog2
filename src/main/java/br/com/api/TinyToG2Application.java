package br.com.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import br.com.api.service.ProductService;

@EnableScheduling
@SpringBootApplication
public class TinyToG2Application {
	
	private final ProductService productService;

	public TinyToG2Application(ProductService productService) {
		this.productService = productService;
	}

	public static void main(String[] args) {
		SpringApplication.run(TinyToG2Application.class, args);
	}
	
	@Scheduled(cron = "* * * * * *")
	private void saveProducts() throws Exception {
		this.productService.main();
	}

}