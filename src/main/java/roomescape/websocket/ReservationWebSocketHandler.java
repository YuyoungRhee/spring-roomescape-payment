package roomescape.websocket;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import roomescape.application.ReservationPaymentDeletionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationWebSocketHandler extends TextWebSocketHandler {

    private final ReservationPaymentDeletionService reservationPaymentDeletionService;
    private final Map<String, Long> sessionToReservation = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long reservationId = extractReservationId(session.getUri());
        sessionToReservation.put(session.getId(), reservationId);
        log.info("WebSocket 연결됨 - reservationId={}", reservationId);

        // 연결 후 5분 후 강제 종료
        scheduleForceClose(session, Duration.ofMinutes(5));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long reservationId = sessionToReservation.remove(session.getId());
        reservationPaymentDeletionService.deleteIfNotPaid(reservationId);

        log.info("WebSocket 연결 종료 - reservationId={}", reservationId);
    }

    private Long extractReservationId(URI uri) {
        String path = uri.getPath();
        return Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
    }

    private void scheduleForceClose(WebSocketSession session, Duration timeout) {
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            try {
                if (session.isOpen()) {
                    log.info("연결 유지 시간 초과 - session 종료");
                    session.close();
                }
            } catch (IOException e) {
                log.error("WebSocket 강제 종료 실패", e);
            }
        }, timeout.toMillis(), TimeUnit.MILLISECONDS);
    }
}
