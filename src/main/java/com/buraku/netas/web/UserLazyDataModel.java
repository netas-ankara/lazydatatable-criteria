package com.buraku.netas.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;

import org.primefaces.component.api.UIColumn;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.buraku.netas.domain.UserDTO;
import com.buraku.netas.service.UserService;

public class UserLazyDataModel extends LazyDataModel<UserDTO> implements SelectableDataModel<UserDTO> {

	private static final long serialVersionUID = -6123945723069023025L;
	private final transient UserService userService;
	private static final SortOrder DEFAULT_SORT_ORDER = SortOrder.ASCENDING;
	private static final String DEFAULT_SORT_FIELD = "id";

	public UserLazyDataModel(UserService userService) {
		this.userService = userService;
	}

	@Override
	public Object getRowKey(UserDTO user) {
		return user.getId();
	}

	@Override
	public UserDTO getRowData(String rowKey) {
		Long rowId = Long.valueOf(rowKey);
		List<UserDTO> users = (List<UserDTO>) userService.getWrapperData();
		return users.stream().filter(user -> user.getId().equals(rowId)).findAny().orElse(null);
	}

	@Override
	public List<UserDTO> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
		Sort sort = new Sort(getDirection(DEFAULT_SORT_ORDER), DEFAULT_SORT_FIELD);
		if (multiSortMeta != null) {
			List<Order> orders = multiSortMeta.stream()
					.map(m -> new Order(getDirection(m.getSortOrder() != null ? m.getSortOrder() : DEFAULT_SORT_ORDER),
							m.getSortField()))
					.collect(Collectors.toList());
			sort = new Sort(orders);
		}
		return filterAndSort(first, pageSize, filters, sort);
	}

	@Override
	public List<UserDTO> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		Sort sort = null;
		if (sortField != null) {
			sort = new Sort(getDirection(sortOrder != null ? sortOrder : DEFAULT_SORT_ORDER), sortField);
		} else if (DEFAULT_SORT_FIELD != null) {
			sort = new Sort(getDirection(sortOrder != null ? sortOrder : DEFAULT_SORT_ORDER), DEFAULT_SORT_FIELD);
		}
		return filterAndSort(first, pageSize, filters, sort);
	}

	private List<UserDTO> filterAndSort(int first, int pageSize, Map<String, Object> filters, Sort sort) {
		Boolean globalFilterCheck = false;
		Map<String, List<String>> newMap = new HashMap<String, List<String>>();
		if (filters.isEmpty()) {
			newMap.put("id", Arrays.asList(""));
		} else {
			for (Map.Entry<String, Object> entry : filters.entrySet()) {
				if (entry.getValue() instanceof String) {
					newMap.put(entry.getKey(), Arrays.asList(entry.getValue().toString()));
				} else {
					newMap.put(entry.getKey(), Arrays.asList((String[]) entry.getValue()));
				}
			}
		}

		if (newMap.get("globalFilter") != null) {
			if (newMap.get("globalFilter").toString().equals("[]")) {
				globalFilterCheck = false;
			} else {
				DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("form:userTable");
				List<UIColumn> column = dataTable.getColumns();
				for (UIColumn uiColumn : column) {
					newMap.put(uiColumn.getHeaderText().toLowerCase(),
							Arrays.asList(newMap.get("globalFilter").toString().replace("[", "").replace("]", "")));
				}
				globalFilterCheck = true;
			}
			newMap.remove("globalFilter");
		}

		Page<UserDTO> pageCriteria = userService.findByCriteria(sort.toString(),
				new PageRequest(first / pageSize, pageSize, sort), newMap, globalFilterCheck);
		this.setRowCount(((Number) pageCriteria.getTotalElements()).intValue());
		this.setWrappedData(pageCriteria.getContent());
		return pageCriteria.getContent();
	}

	private static Direction getDirection(SortOrder order) {
		switch (order) {
		case ASCENDING:
			return Direction.ASC;
		case DESCENDING:
			return Direction.DESC;
		case UNSORTED:
		default:
			return null;
		}
	}
}
