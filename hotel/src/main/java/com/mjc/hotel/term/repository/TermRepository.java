package com.mjc.hotel.term.repository;

import com.mjc.hotel.term.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {
}
