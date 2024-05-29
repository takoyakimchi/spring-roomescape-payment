package roomescape.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import roomescape.service.dto.PaymentRequestDto;
import roomescape.service.errorhandler.PaymentErrorHandler;

@Service
public class PaymentService {

    private static final String AUTHORIZATION_PREFIX = "Basic ";
    private static final String TOSS_PAYMENTS_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private final String tossPaymentTestKey;
    private final RestClient restClient;

    public PaymentService(@Value("${toss-payment.test-secret-key}") String key) {
        this.tossPaymentTestKey = key + ":";
        this.restClient = RestClient.builder()
            .baseUrl(TOSS_PAYMENTS_URL)
            .build();
    }

    public void pay(String orderId, long amount, String paymentKey) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode(tossPaymentTestKey.getBytes(StandardCharsets.UTF_8));
        String authorizations = AUTHORIZATION_PREFIX + new String(encodedBytes);

        restClient.post()
            .uri(TOSS_PAYMENTS_URL)
            .body(new PaymentRequestDto(orderId, amount, paymentKey))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", authorizations)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, new PaymentErrorHandler())
            .onStatus(HttpStatusCode::is5xxServerError, new PaymentErrorHandler())
            .toBodilessEntity();
    }
}