package com.buraku.netas.web;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.buraku.netas.domain.UserDTO;
import com.buraku.netas.service.UserService;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class UserBean {

	@Autowired
	private UserService userService;
	private UserLazyDataModel users;

	private List<String> districts;
	private List<String> cities;

	@PostConstruct
	private void init() {
		this.users = new UserLazyDataModel(userService);
		districts = new ArrayList<>();
		districts.add("ERYAMAN");
		districts.add("PENDİK");
		districts.add("ETİMESGUT");
		cities = new ArrayList<>();
		cities.add("ANKARA");
		cities.add("ISTANBUL");
		cities.add("ANTALYA");

	}

	public List<String> getCities() {
		return cities;
	}

	public void setCities(List<String> cities) {
		this.cities = cities;
	}
	
	public List<String> getDistricts() {
		return districts;
	}

	public void setDistricts(List<String> districts) {
		this.districts = districts;
	}

	public UserLazyDataModel getUsers() {
		return users;
	}

	public List<UserDTO> getUserDTO() {
		return userService.getWrapperData();
	}
}
