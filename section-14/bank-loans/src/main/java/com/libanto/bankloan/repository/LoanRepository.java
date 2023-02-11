package com.libanto.bankloan.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.libanto.bankloan.model.Loan;

@Repository
public interface LoanRepository extends CrudRepository<Loan, Long> {
	List<Loan> findByCustomerIdOrderByStartDtDesc(int customerId);
}
