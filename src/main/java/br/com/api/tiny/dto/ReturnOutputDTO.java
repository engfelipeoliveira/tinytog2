package br.com.api.tiny.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ReturnOutputDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("status_processamento")
	private int processingStatus;

	@JsonProperty("status")
	private String status;

	@JsonProperty("pagina")
	private int page;

	@JsonProperty("numero_paginas")
	private int numberPages;

	@JsonProperty("produtos")
	private List<ContainerProductOutputDTO> products;

}
