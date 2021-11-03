package victor.training.reactive.reactor.lite;

import org.springframework.web.client.AsyncRestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Learn how to create Mono instances.
 *
 * @author Sebastien Deleuze
 * @see <a href="https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html">Mono Javadoc</a>
 */
public class Part02Mono {

//========================================================================================

	// TODO Return an empty Mono
	Mono<String> emptyMono() {
		return Mono.empty();
	}

//========================================================================================

	// TODO Return a Mono that never emits any signal
	Mono<String> monoWithNoSignal() {

		return Mono.never();
//		return Mono.create(sink -> {
//
//			AsyncRestTemplate rest = new AsyncRestTemplate();
//			rest.getForEntity().addCallback(resp -> sink.success(resp));
//			// gata
//		}).timeout(Duration.ofMillis(100));
	}

//========================================================================================

	// TODO Return a Mono that contains a "foo" value
	Mono<String> fooMono() {
		return null;
	}

//========================================================================================

	// TODO Return a Mono of data. data can come null.
	Mono<String> optionalMono(String data) {
		return Mono.justOrEmpty(data);
	}

//========================================================================================

	// TODO Create a Mono that emits an IllegalStateException
	Mono<String> errorMono() {
		return Mono.error(new IllegalStateException());
	}

}
