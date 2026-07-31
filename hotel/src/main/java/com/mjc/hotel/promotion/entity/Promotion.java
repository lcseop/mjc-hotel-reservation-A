package com.mjc.hotel.promotion.entity;

import com.mjc.hotel.room.entity.RoomType;
import com.mjc.hotel.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "promotion")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Promotion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private RoomType roomType ;

    @Column(length = 50, name="promotion_name")
    private String promotionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type")
    private ConditionType conditionType;

    @Column(name="start_date")
    private LocalDateTime startDate;

    @Column(name="end_date")
    private LocalDateTime endDate;

    @Column(name = "discount_info", nullable = false)
    private String discountContent; // 예: "최대 30%", "30,000원"

    @Builder.Default
    @Column(name = "deleted")
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void update(String promotionName) {
        this.promotionName = promotionName;
    }
}
