package com.example.demo.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name ="Products")
public class Product {
@Id


@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;


//	@Column(length = 50, unique = true, nullable = false)
@Column(length = 50)
private String maSP;

@Column(length = 50)
private String name;

@Column
private Double price;

@Column
private int quantity;

@Column (length = 500)
private String description;

@Column(length = 500)
private String photo;

@Column(length = 50)
private String volume;


//quan hệ nhiều 1 với bảng category thông qua cột id
@ManyToOne
@JoinColumn(name="CategoryId", referencedColumnName = "id")
private Category category;

//quan hệ nhiều 1 với bảng user thông qua cột id
@ManyToOne
@JoinColumn(name="UserId", referencedColumnName = "id")
private User user;

public Product() {
	super();
}

public Product(Long id) {
	super();
	this.id = id;
}

public Product(String maSP, String name, Double price, int quantity, String description, String photo, String volume,
		Category category, User user) {
	super();
	this.maSP = maSP;
	this.name = name;
	this.price = price;
	this.quantity = quantity;
	this.description = description;
	this.photo = photo;
	this.volume = volume;
	this.category = category;
	this.user = user;
}

public Product(Long id, String maSP, String name, Double price, int quantity, String description, String photo,
		String volume, Category category, User user) {
	super();
	this.id = id;
	this.maSP = maSP;
	this.name = name;
	this.price = price;
	this.quantity = quantity;
	this.description = description;
	this.photo = photo;
	this.volume = volume;
	this.category = category;
	this.user = user;
}

public Long getId() {
	return id;
}

public void setId(Long id) {
	this.id = id;
}

public String getMaSP() {
	return maSP;
}

public void setMaSP(String maSP) {
	this.maSP = maSP;
}

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

public Double getPrice() {
	return price;
}

public void setPrice(Double price) {
	this.price = price;
}

public int getQuantity() {
	return quantity;
}

public void setQuantity(int quantity) {
	this.quantity = quantity;
}

public String getDescription() {
	return description;
}

public void setDescription(String description) {
	this.description = description;
}

public String getPhoto() {
	return photo;
}

public void setPhoto(String photo) {
	this.photo = photo;
}

public String getVolume() {
	return volume;
}

public void setVolume(String volume) {
	this.volume = volume;
}

public Category getCategory() {
	return category;
}

public void setCategory(Category category) {
	this.category = category;
}

public User getUser() {
	return user;
}

public void setUser(User user) {
	this.user = user;
}





}
