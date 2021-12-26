package br.com.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import br.com.api.model.service.ProductService;

@EnableScheduling
@SpringBootApplication
public class Tinytog2Application {
	
	private final ProductService productService;

	public Tinytog2Application(ProductService productService) {
		this.productService = productService;
	}

	public static void main(String[] args) {
		SpringApplication.run(Tinytog2Application.class, args);
	}
	
	@Scheduled(cron = "* * * * * *")
	private void saveProducts() throws Exception {
		this.productService.main();
	}

}