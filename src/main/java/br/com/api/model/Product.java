package br.com.api.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Entity
@Table(name = "product")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "bar_code")
	private String barCode;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "price_1")
	private BigDecimal price1;
	
	@Column(name = "price_2")
	private BigDecimal price2;
	
	@Column(name = "dt_create")
	private LocalDateTime dtCreate;
	
	@Column(name = "dt_update")
	private LocalDateTime dtUpdate;
	
}
