package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import roomescape.common.event.EventPublisher;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.repository.PaymentRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.UncompletedPaymentDeletionRequestedEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationDeletionIfPaymentFailedService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    private final EventPublisher eventPublisher;

    public void deleteIfNotPaid(Long reservationId) {

        eventPublisher.raise(new UncompletedPaymentDeletionRequestedEvent(reservationId));

        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> {
                    log.error("결제 정보 없음 - reservationId={}", reservationId);
                    return new IllegalStateException("결제 정보 없음");
                });

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            paymentRepository.deleteByReservationId(reservationId);
            reservationRepository.deleteById(reservationId);
            log.info("예약, 결제 모두 삭제 됨 - reservationId={}, paymentId={}", reservationId, payment.getId());
            return;
        }
        log.info("예약 결제 성공 - reservationId={}, paymentId={}", reservationId, payment.getId());
    }
}
