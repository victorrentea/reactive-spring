package victor.training.reactive.reactor.advanced;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ThrottleWebClientMaxConnectionsTest {
    public static final int MAX_CONNECTIONS = 5;

    @Rule
    public WireMockRule wireMock = new WireMockRule(9999);

    public ThrottleWebClientMaxConnections target = new ThrottleWebClientMaxConnections(
        "http://localhost:9999/testing", MAX_CONNECTIONS);

    @Test
    public void throttleMaxConcurentHttpCalls() {
        // Given
        wireMock.addStubMapping(stubFor(post(urlPathEqualTo("/testing"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("Hello!")
                    .withFixedDelay(1000))));

        // When
        int NUMBER_OF_REQUESTS = 30;
        ParallelFlux<String> flux = Flux.range(0, NUMBER_OF_REQUESTS)
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(i -> target.makeRequest(i));

        long startTime = System.currentTimeMillis();
        StepVerifier.create(flux).expectNextCount(NUMBER_OF_REQUESTS).verifyComplete();
        long endTime = System.currentTimeMillis();


        log.info("Retrieve took {}", endTime - startTime);

        assertThat(endTime - startTime)
                .describedAs("Should take no of requests / max concurrent * latency")
                .isGreaterThanOrEqualTo((NUMBER_OF_REQUESTS / MAX_CONNECTIONS) * 1000);
    }

}