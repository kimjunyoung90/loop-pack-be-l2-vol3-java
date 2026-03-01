package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "brands")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Builder
    private Brand(String name) {
        this.name = name;
    }

    public void update(String name) {
        this.name = name;
    }
}
