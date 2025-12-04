package kr.hhplus.be.server.reservation.domain;

public enum ReservationStatus {
    PENDING {
        @Override
        public boolean canTransitionTo(ReservationStatus next) {
            return next == CONFIRMED || next == CANCELED || next == EXPIRED;
        }
    },

    CONFIRMED {
        @Override
        public boolean canTransitionTo(ReservationStatus next) {
            return next == CANCELED; // EXPIRED는 불가
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
            return false;  // 만료 후 변경 불가
        }
    };

    public abstract boolean canTransitionTo(ReservationStatus next);
}
