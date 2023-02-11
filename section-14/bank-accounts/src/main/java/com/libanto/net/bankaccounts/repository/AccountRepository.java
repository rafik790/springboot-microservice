package com.libanto.net.bankaccounts.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.libanto.net.bankaccounts.entity.Account;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long>{
	Account findByCustomerId(int customerId);
}
