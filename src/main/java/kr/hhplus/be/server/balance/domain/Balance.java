package kr.hhplus.be.server.balance.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Getter
@NoArgsConstructor
public class Balance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long balance;

    // Balance 생성 규칙
    public static Balance create(Long userId) {
        Balance b = new Balance();
        b.userId = userId;
        b.balance = 0L;
        return b;
    }

    // 충전
    public void charge(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전금액은 0보다 커야합니다.");
        }
        this.balance += amount;
    }

    // 사용
    public void use(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용금액은 0보다 커야합니다.");
        }
        if (this.balance < amount) {
            throw new IllegalStateException("사용 가능한 금액이 부족합니다.");
        }
        this.balance -= amount;
    }
}

