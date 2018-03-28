package com.buraku.netas.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.buraku.netas.domain.UserDTO;

public interface UserRepository extends PagingAndSortingRepository<UserDTO, Long>, JpaSpecificationExecutor<UserDTO>, JpaRepository<UserDTO, Long>, Repository<UserDTO, Long>{
	@Query(value = "select * from userdto where login like %:filter% \n-- #pageable\n",
    countQuery = "select count(*) from userdto where login like %:filter% \n-- #pageable\n",
    nativeQuery = true)
	Page<UserDTO> findByLogin(@Param("filter") String filters, Pageable pageable);
}
