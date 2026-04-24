package com.loopers.domain.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.Objects;

public final class Money {

    public static final Money ZERO = new Money(0);

    private final int value;

    private Money(int value) {
        if (value < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "금액은 0 이상이어야 합니다.");
        }
        this.value = value;
    }

    @JsonCreator
    public static Money of(int value) {
        return new Money(value);
    }

    @JsonValue
    public int value() {
        return value;
    }

    public Money add(Money other) {
        return new Money(this.value + other.value);
    }

    public Money subtract(Money other) {
        int result = this.value - other.value;
        return new Money(Math.max(result, 0));
    }

    public Money multiply(int factor) {
        if (factor < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "곱셈 계수는 0 이상이어야 합니다.");
        }
        return new Money(this.value * factor);
    }

    public Money percentOf(int percent) {
        if (percent < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "퍼센트는 0 이상이어야 합니다.");
        }
        return new Money(this.value * percent / 100);
    }

    public Money min(Money other) {
        return this.value <= other.value ? this : other;
    }

    public boolean isGreaterThan(Money other) {
        return this.value > other.value;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return this.value >= other.value;
    }

    public boolean isLessThan(Money other) {
        return this.value < other.value;
    }

    public boolean isZero() {
        return this.value == 0;
    }

    public boolean isPositive() {
        return this.value > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return value == money.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Money[value=" + value + "]";
    }
}
