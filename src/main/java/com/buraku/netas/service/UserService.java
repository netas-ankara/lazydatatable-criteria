package com.buraku.netas.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.buraku.netas.domain.ColumnInfo;
import com.buraku.netas.domain.User;
import com.buraku.netas.domain.UserDTO;
import com.buraku.netas.repository.UserRepository;

import javax.persistence.*;
import javax.persistence.criteria.*;

@Service
public class UserService {

	private List<UserDTO> list;
	private List<ColumnInfo> colList;
	private Predicate finalQuery;
	private Predicate andQuery;

	@PersistenceContext
	public EntityManager em;

	@Autowired
	private UserRepository userRepository;

	private PageImpl<UserDTO> result = null;

	public Page<UserDTO> findByFilter(Map<String, String> filters, Pageable pageable) {
		return userRepository.findAll(getFilterSpecification(filters), pageable);
	}

	public Page<UserDTO> findByLogin(String filters, Pageable pageable) {
		return userRepository.findByLogin(filters, pageable);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Page<UserDTO> findByCriteria(String sort, Pageable pageable, Map<String, List<String>> filtersMap,
			Boolean globalFilterCheck) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<UserDTO> cq = cb.createQuery(UserDTO.class);
		Root<UserDTO> iRoot = cq.from(UserDTO.class);
		Join<UserDTO, User> bJoin = iRoot.join("user");
		// bJoin.on(cb.equal(bJoin.get("id"), iRoot.get("id")));
		List<Predicate> predicatesOr = new ArrayList<Predicate>();
		List<Predicate> predicatesAnd = new ArrayList<Predicate>();

		cq.multiselect(bJoin.get("id"), bJoin.get("login"), bJoin.get("firstname"), bJoin.get("lastname"),
				bJoin.get("dayofbirth"), iRoot.get("district"), iRoot.get("city"));

		filtersMap.entrySet().stream().filter(v -> v.getValue() != null && v.getValue().size() > 0).forEach(entry -> {
			entry.getValue().forEach(k -> {
				try {
					bJoin.<String>get(entry.getKey()).as(String.class);

					Expression<String> e1 = bJoin.<String>get(entry.getKey()).as(String.class);
					Expression convertedColumnName = cb.function("TO_CHAR", String.class, e1);
					if (entry.getValue().size() > 1 || globalFilterCheck) {
						predicatesOr.add(cb.like(cb.lower(convertedColumnName), "%" + k.toLowerCase() + "%"));
					} else {
						predicatesAnd.add(cb.like(cb.lower(convertedColumnName), "%" + k.toLowerCase() + "%"));
					}
				} catch (Exception e) {
					if (!k.isEmpty()) {
						if (!iRoot.<String>get(entry.getKey()).getJavaType().toString().contains("String")) {
							Expression<String> e1 = iRoot.<String>get(entry.getKey()).as(String.class);
							Expression convertedColumnName = cb.function("TO_CHAR", String.class, e1);
							if (entry.getValue().size() > 1 || globalFilterCheck) {
								predicatesOr.add(cb.like(convertedColumnName, "%" + k + "%"));
							} else {
								predicatesAnd.add(cb.like(convertedColumnName, "%" + k + "%"));
							}
						} else {
							if (entry.getValue().size() > 1 || globalFilterCheck) {
								predicatesOr.add(cb.like(cb.lower(iRoot.<String>get(entry.getKey())),
										"%" + k.toLowerCase() + "%"));
							} else {
								predicatesAnd.add(cb.like(cb.lower(iRoot.<String>get(entry.getKey())),
										"%" + k.toLowerCase() + "%"));
							}
						}
					}
				}
			});
		});

		Predicate[] predArrayOr = new Predicate[predicatesOr.size()];
		Predicate[] predArrayAnd = new Predicate[predicatesAnd.size()];

		predicatesOr.toArray(predArrayOr);
		predicatesAnd.toArray(predArrayAnd);

		finalQuery = cb.or(predArrayOr);
		andQuery = cb.and(predArrayAnd);
		Predicate pFinal = predArrayOr.length >= 2 ? cb.and(cb.or(finalQuery), andQuery)
				: cb.and(cb.or(cb.conjunction(), finalQuery), andQuery);

		cq.where(pFinal);

		Long count = calculateCount(filtersMap);

		List<Order> orders = new ArrayList<Order>(2);

		try {
			bJoin.<String>get(sort.split(":")[0]).as(String.class);

			if (sort.split(":")[1].replaceAll("\\s+", "").equals("ASC")) {
				orders.add(cb.asc(bJoin.get(sort.split(":")[0])));
			} else {
				orders.add(cb.desc(bJoin.get(sort.split(":")[0])));
			}
		} catch (Exception e) {
			if (sort.split(":")[1].replaceAll("\\s+", "").equals("ASC")) {
				orders.add(cb.asc(iRoot.get(sort.split(":")[0])));
			} else {
				orders.add(cb.desc(iRoot.get(sort.split(":")[0])));
			}
		}

		cq.orderBy(orders);
		TypedQuery<UserDTO> query = em.createQuery(cq);
		this.result = new PageImpl<UserDTO>(query.getResultList(), pageable, count);
		this.setColumns(this.distinguishColumns((List<UserDTO>) result.getContent()));
		this.setWrappedData((List<UserDTO>) result.getContent());
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Long calculateCount(Map<String, List<String>> filtersMap) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> sc = cb.createQuery(Long.class);
		Root<UserDTO> iRoot = sc.from(UserDTO.class);
		Join<UserDTO, User> bJoin = iRoot.join("user");
		List<Predicate> predicatesOr = new ArrayList<Predicate>();
		List<Predicate> predicatesAnd = new ArrayList<Predicate>();

		sc.multiselect(bJoin.get("id"), bJoin.get("login"), bJoin.get("firstname"), bJoin.get("lastname"),
				bJoin.get("dayofbirth"), iRoot.get("district"), iRoot.get("city"));

		filtersMap.entrySet().stream().filter(v -> v.getValue() != null && v.getValue().size() > 0).forEach(entry -> {
			entry.getValue().forEach(k -> {

				try {
					bJoin.<String>get(entry.getKey()).as(String.class);

					Expression<String> e1 = bJoin.<String>get(entry.getKey()).as(String.class);
					Expression convertedColumnName = cb.function("TO_CHAR", String.class, e1);
					if (entry.getValue().size() > 1) {
						predicatesOr.add(cb.like(cb.lower(convertedColumnName), "%" + k.toLowerCase() + "%"));
					} else {
						predicatesAnd.add(cb.like(cb.lower(convertedColumnName), "%" + k.toLowerCase() + "%"));
					}
				} catch (Exception e) {
					if (!k.isEmpty()) {
						if (!iRoot.<String>get(entry.getKey()).getJavaType().toString().contains("String")) {
							Expression<String> e1 = iRoot.<String>get(entry.getKey()).as(String.class);
							Expression convertedColumnName = cb.function("TO_CHAR", String.class, e1);
							if (entry.getValue().size() > 1) {
								predicatesOr.add(cb.like(convertedColumnName, "%" + k + "%"));
							} else {
								predicatesAnd.add(cb.like(convertedColumnName, "%" + k + "%"));
							}
						} else {
							if (entry.getValue().size() > 1) {
								predicatesOr.add(cb.like(cb.lower(iRoot.<String>get(entry.getKey())),
										"%" + k.toLowerCase() + "%"));
							} else {
								predicatesAnd.add(cb.like(cb.lower(iRoot.<String>get(entry.getKey())),
										"%" + k.toLowerCase() + "%"));
							}
						}
					}
				}
			});

		});

		Predicate[] predArrayOr = new Predicate[predicatesOr.size()];
		Predicate[] predArrayAnd = new Predicate[predicatesAnd.size()];

		predicatesOr.toArray(predArrayOr);
		predicatesAnd.toArray(predArrayAnd);

		finalQuery = cb.or(predArrayOr);
		andQuery = cb.and(predArrayAnd);
		Predicate pFinal = predArrayOr.length >= 2 ? cb.and(cb.or(finalQuery), andQuery)
				: cb.and(cb.or(cb.conjunction(), finalQuery), andQuery);

		sc.where(pFinal);

		sc.select(cb.count(iRoot));

		Long count = em.createQuery(sc).getSingleResult();

		return count;

	}
	
	private List<ColumnInfo> distinguishColumns(List<UserDTO> result) {
		result.forEach(entry -> {
			colList = new ArrayList<>();
			String[] splitTheToString = entry.toString().split("=");
			for (int i = 0; i < splitTheToString.length - 1; i++) {
				try {
					if (!splitTheToString[i].split(",")[1].equals("city") && !splitTheToString[i].split(",")[1].equals("district")) {
						colList.add(new ColumnInfo(splitTheToString[i].split(",")[1],
								splitTheToString[i].split(",")[1].toUpperCase()));
					}
				} catch (Exception e) {
					if (!splitTheToString[i].split(",")[0].equals("city") && !splitTheToString[i].split(",")[0].equals("district")) {
						colList.add(new ColumnInfo(splitTheToString[i].split(",")[0],
								splitTheToString[i].split(",")[0].toUpperCase()));
					}
				}
			}
			return;
		});
		return colList;
	}

	public void setColumns(List<ColumnInfo> colList) {
		this.colList = colList;
	}

	public List<ColumnInfo> getColumns() {
		return this.colList;
	}

	public void setWrappedData(List<UserDTO> list) {
		this.list = list;
	}

	public List<UserDTO> getWrapperData() {
		return this.list;
	}

	@Transactional
	public void create(UserDTO user) {
		userRepository.save(user);
	}

	private Specification<UserDTO> getFilterSpecification(Map<String, String> filterValues) {
		return (Root<UserDTO> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
			Optional<Predicate> predicate = filterValues.entrySet().stream()
					.filter(v -> v.getValue() != null && v.getValue().length() > 0).map(entry -> {
						Path<?> path = root;
						String key = entry.getKey();
						if (entry.getKey().contains(".")) {
							String[] splitKey = entry.getKey().split("\\.");
							path = root.join(splitKey[0]);
							key = splitKey[1];
						}
						return builder.like(path.get(key).as(String.class), "%" + entry.getValue() + "%");
					}).collect(Collectors.reducing((a, b) -> builder.and(a, b)));
			return predicate.orElseGet(() -> alwaysTrue(builder));
		};
	}

	private Predicate alwaysTrue(CriteriaBuilder builder) {
		return builder.isTrue(builder.literal(true));
	}

}
