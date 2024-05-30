package roomescape.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import roomescape.controller.dto.PaymentErrorMessageResponse;
import roomescape.global.exception.RoomescapeException;
import roomescape.service.client.TossPaymentRestClient;
import roomescape.service.dto.PaymentRequestDto;

@Service
public class TossPaymentService {

    private final TossPaymentRestClient restClient;

    public TossPaymentService(TossPaymentRestClient restClient) {
        this.restClient = restClient;
    }

    @Transactional
    public void pay(String orderId, long amount, String paymentKey) {
        try {
            restClient.build()
                .post()
                .body(new PaymentRequestDto(orderId, amount, paymentKey))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            PaymentErrorMessageResponse response = e.getResponseBodyAs(PaymentErrorMessageResponse.class);
            if (response == null) {
                throw new RoomescapeException("결제가 승인되지 않았습니다.");
            }
            throw new RoomescapeException(response.message());
        } catch (RestClientResponseException e) {
            throw new RoomescapeException("결제가 승인되지 않았습니다.");
        }
    }
}
