package com.funa.food.usage;

import com.funa.food.common.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_usage_token")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsageToken extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;

    @Column(length = 100)
    private String modelName;

    private Long requestDuration; // ms
}
