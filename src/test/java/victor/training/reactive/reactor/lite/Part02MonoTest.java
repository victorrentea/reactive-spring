package victor.training.reactive.reactor.lite;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Learn how to create Mono instances.
 *
 * @author Sebastien Deleuze
 * @see <a href="https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html">Mono Javadoc</a>
 */
public class Part02MonoTest {

	Part02Mono workshop = new Part02Mono();

//========================================================================================

	@Test
	public void emptyMono() {
		Mono<String> mono = workshop.emptyMono();
		StepVerifier.create(mono)
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void monoWithNoSignal() {
		Mono<String> mono = workshop.monoWithNoSignal();
		StepVerifier
				.create(mono)
				.expectSubscription()
				.expectTimeout(Duration.ofSeconds(1))
				.verify();
	}

//========================================================================================

	@Test
	public void fooMono() {
		Mono<String> mono = workshop.fooMono();
		StepVerifier.create(mono)
				.expectNext("foo")
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void optionalMono() {
		StepVerifier.create(workshop.optionalMono("foo"))
				.expectNext("foo")
				.verifyComplete();

		StepVerifier.create(workshop.optionalMono(null))
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void errorMono() {
		Mono<String> mono = workshop.errorMono();
		StepVerifier.create(mono)
				.verifyError(IllegalStateException.class);
	}

}
