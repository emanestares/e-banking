package com.example.banking.repository;

import com.example.banking.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Get transactions where account is sender OR receiver
    @Query("""
            SELECT t FROM Transaction t
            LEFT JOIN FETCH t.senderAccount sa
            LEFT JOIN FETCH t.receiverAccount ra
            WHERE sa.id = :accountId OR ra.id = :accountId
            ORDER BY t.createdAt DESC
            """)
    List<Transaction> findAllByAccountId(@Param("accountId") Long accountId);

    // Paginated version for large datasets
    @Query(value = """
            SELECT t FROM Transaction t
            LEFT JOIN FETCH t.senderAccount sa
            LEFT JOIN FETCH t.receiverAccount ra
            WHERE sa.id = :accountId OR ra.id = :accountId
            ORDER BY t.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(t) FROM Transaction t
            WHERE t.senderAccount.id = :accountId OR t.receiverAccount.id = :accountId
            """)
    Page<Transaction> findAllByAccountIdPaged(@Param("accountId") Long accountId, Pageable pageable);

    // Admin: all transactions
    @Query("""
            SELECT t FROM Transaction t
            LEFT JOIN FETCH t.senderAccount sa
            LEFT JOIN FETCH t.receiverAccount ra
            ORDER BY t.createdAt DESC
            """)
    List<Transaction> findAllWithDetails();

    // By user (across all their accounts)
    @Query("""
            SELECT t FROM Transaction t
            LEFT JOIN FETCH t.senderAccount sa
            LEFT JOIN FETCH t.receiverAccount ra
            WHERE sa.user.id = :userId OR ra.user.id = :userId
            ORDER BY t.createdAt DESC
            """)
    List<Transaction> findAllByUserId(@Param("userId") Long userId);
}