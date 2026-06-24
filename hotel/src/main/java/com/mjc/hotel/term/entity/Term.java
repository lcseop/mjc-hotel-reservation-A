package com.mjc.hotel.term.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "terms")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "term_id")
    private Long termId;

    @Column(name = "term_type", length = 30)
    private String termType;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "effective_at")
    private LocalDateTime effectiveAt;
}
