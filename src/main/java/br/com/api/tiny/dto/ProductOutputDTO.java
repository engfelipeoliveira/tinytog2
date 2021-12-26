package br.com.api.tiny.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ProductOutputDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("id")
	private Long id;
	
	@JsonProperty("codigo")
	private String code;

	@JsonProperty("nome")
	private String name;

	@JsonProperty("preco")
	private BigDecimal price;

	@JsonProperty("preco_custo")
	private BigDecimal costPrice;
	
	@JsonProperty("preco_promocional")
	private BigDecimal promotionalPrice;
	
	@JsonProperty("situacao")
	private String status;
	
}
