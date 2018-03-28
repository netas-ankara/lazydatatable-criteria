package com.buraku.netas.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="USERDTO")
public class UserDTO implements Serializable {

	private static final long serialVersionUID = -7860243025833384447L;

	@Id
	private Long id;
	private String login;
	private String firstname;
	private String lastname;
	private Date dayofbirth;
	private String city;
	private String district;
	@OneToOne(fetch=FetchType.LAZY,cascade=CascadeType.ALL)
	@JoinColumn(name="id")
	User user;

	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getfirstname() {
		return firstname;
	}

	public void setfirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getlastname() {
		return lastname;
	}

	public void setlastname(String lastname) {
		this.lastname = lastname;
	}

	public Date getdayofbirth() {
		return dayofbirth;
	}

	public void setdayofbirth(Date dayofbirth) {
		this.dayofbirth = dayofbirth;
	}
	
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	@Override
	public String toString() {
		return "id=" + id + ",login=" + login + ",firstname=" + firstname + ",lastname=" + lastname
				+ ",dayofbirth=" + dayofbirth + ",city=" + city + ",district=" + district;
	}

	public UserDTO(Long userId, String login, String firstname, String lastname, Date dayofbirth, String district, String city){
        this.id = userId;
        this.login = login;
        this.firstname = firstname;   
        this.lastname = lastname; 
        this.dayofbirth = dayofbirth; 
        this.district = district; 
        this.city = city; 
  }
	
	public UserDTO() {}

}
