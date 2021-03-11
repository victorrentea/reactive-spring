package victor.training.reactor.pitfalls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

import static java.time.Duration.ofMillis;
import static org.assertj.core.api.Assertions.assertThat;
import static reactor.core.scheduler.Schedulers.boundedElastic;

@Slf4j
class Pitfalls {
	Mono<String> create(String s) {
		return Mono
			.delay(ofMillis(50), boundedElastic()).thenReturn("Create " + s)
//			.just("Create " + s )
			.doOnNext(log::info);
	}
}

// 6. Out-of-band Shared State: @see RaceVariables
// 7. Mutate argument @see RaceArguments
// 8. flux.flatMap(Mono) might explode parallelism
// 9. Leaking Flux : when toIterable, there's no cancel()
// 10. Standalone flux with no error handling, just subscribe() crashes at any exception