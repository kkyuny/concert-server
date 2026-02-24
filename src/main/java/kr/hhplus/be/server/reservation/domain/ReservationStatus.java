package kr.hhplus.be.server.reservation.domain;

public enum ReservationStatus {
    PENDING {
        @Override
        public boolean canTransitionTo(ReservationStatus next) {
            return next != null && (
                    next.name().equals(CONFIRMED.name()) ||
                    next.name().equals(CANCELED.name()) ||
                    next.name().equals(EXPIRED.name())
            );
        }
    },

    CONFIRMED {
        @Override
        public boolean canTransitionTo(ReservationStatus next) {
            return next != null && next.name().equals(CANCELED.name()); // EXPIRED는 불가
        }
    },

    CANCELED {
        @Override
        public boolean canTransitionTo(ReservationStatus next) {
            return false;  // 취소 후 변경 불가
        }
    },

    EXPIRED {
        @Override
        public boolean canTransitionTo(ReservationStatus next) {
            return next.name().equals(EXPIRED.name());  // 만료 후 변경 불가(같은 상태만 예외적 허용)
        }
    };

    public abstract boolean canTransitionTo(ReservationStatus next);
}
