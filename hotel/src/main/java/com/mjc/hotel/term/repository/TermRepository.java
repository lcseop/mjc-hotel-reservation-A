package com.mjc.hotel.term.repository;

import com.mjc.hotel.term.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {

    Optional<Term> findFirstByTermTypeAndDeletedFalse(String termType);
}
