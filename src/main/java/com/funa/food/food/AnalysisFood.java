package com.funa.food.food;

import com.funa.food.common.domain.BaseAuditEntity;
import com.funa.food.usage.UsageToken;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_analysis_food")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisFood extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String userStatus;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String foods; // JSON string of foods from OpenAI

    @Column(length = 2000)
    private String suitability;

    @Column(length = 5000)
    private String suggestion;

    @Column(length = 255)
    private String imageUserFileName;

    @Column(length = 255)
    private String imageFileName; // UUID server-side filename

    private Long imageFileSize; // bytes

    @Column(length = 50)
    private String imageSize; // e.g., 1024x768

    @OneToOne
    @JoinColumn(name = "usage_token_id", unique = true)
    private UsageToken usageToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_mode", length = 40)
    private AnalysisMode analysisMode;
}
