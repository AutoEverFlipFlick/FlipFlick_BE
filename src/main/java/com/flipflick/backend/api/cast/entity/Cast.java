package com.flipflick.backend.api.cast.entity;

import com.flipflick.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "casts")
public class Cast extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long tmdbId;

    private String name; // 배우 이름

    @Enumerated(EnumType.STRING)
    private Gender gender; // 배우 성별

    private String profileImage; // 배우 프로필 이미지
    private String placeOfBirth; // 배우 출생지

    private LocalDate birthday; // 배우 생일
    private LocalDate deathday; // 배우 death 일

    @Builder.Default
    @OneToMany(mappedBy = "cast", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Fetch(FetchMode.SUBSELECT)
    private List<Filmography> filmographies = new ArrayList<>();

}
