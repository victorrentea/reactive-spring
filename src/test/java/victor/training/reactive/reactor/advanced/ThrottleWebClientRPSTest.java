package victor.training.reactive.reactor.advanced;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
@Slf4j
public class ThrottleWebClientRPSTest {

    public static final int MAX_RPS = 3;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9999);
    private ThrottleWebClientRPS target = new ThrottleWebClientRPS(
        "http://localhost:9999/testing", MAX_RPS);

    @Test
    public void whyDoesntThisWorkQuestionMarkQuestionMark() {
        // Given

        wireMockRule.addStubMapping(stubFor(post(urlPathEqualTo("/testing"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("Hello!")
                    .withFixedDelay(1000))));

        // When
        int NUMBER_OF_REQUESTS = 30;
        Flux<String> flux =
            Flux.interval(Duration.ofMillis(1))
                .doOnNext(e -> log.info("Incoming request " + e))
                .take(NUMBER_OF_REQUESTS)
                .onBackpressureBuffer()
                //Flux.range(0, NUMBER_OF_REQUESTS).parallel().runOn(Schedulers.boundedElastic())
                .flatMap(id -> target.makeRequest(id));

        long startTime = System.currentTimeMillis();
        StepVerifier.create(flux).expectNextCount(NUMBER_OF_REQUESTS).verifyComplete();
        long endTime = System.currentTimeMillis();

        log.info("Took {} ", endTime - startTime);

        assertThat(endTime - startTime)
                .describedAs("I don't understand why this is not taking longer than this")
                .isGreaterThanOrEqualTo((NUMBER_OF_REQUESTS / MAX_RPS) * 1000);
    }
}