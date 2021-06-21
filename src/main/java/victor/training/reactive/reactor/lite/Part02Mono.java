package victor.training.reactive.reactor.lite;

import reactor.core.publisher.Mono;

import java.util.Optional;

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
//		return Mono.empty();
		return Mono.create(sink -> {
			sink.success();
		});
	}

//========================================================================================

	// TODO Return a Mono that never emits any signal
	Mono<String> monoWithNoSignal() {
		return Mono.create(sink -> {
//			sink.success(); // completion without data
//			sink.success("a"); // data signal + completion signal
//			sink.error(new RuntimeException()); // ERROR signal.
		});
	}

//========================================================================================

	// TODO Return a Mono that contains a "foo" value
	Mono<String> fooMono() {
//		Optional.of(null); fails just like the line below would
//		return Mono.just(null);

//		Optional.ofNullable(dataPossilbeNull);
//		return Mono.justOrEmpty(dataPossilbeNull);

		return Mono.just("foo");
	}

//========================================================================================

	// TODO Create a Mono that emits an IllegalStateException
	Mono<String> errorMono() {
		return Mono.error(new IllegalStateException());
	}

}
