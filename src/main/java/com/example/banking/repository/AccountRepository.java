package com.example.banking.repository;

import com.example.banking.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserId(Long userId);

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumberAndIsActiveTrue(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT a FROM Account a JOIN FETCH a.user WHERE a.isActive = true ORDER BY a.createdAt DESC")
    List<Account> findAllActiveAccountsWithUsers();

    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.isActive = true")
    List<Account> findActiveAccountsByUserId(@Param("userId") Long userId);
}