package com.mjc.hotel.member.entity;

import com.mjc.hotel.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private MemberRole role;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "phone_verified")
    private Boolean phoneVerified;

}
