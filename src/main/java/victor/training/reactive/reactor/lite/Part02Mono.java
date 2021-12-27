package victor.training.reactive.reactor.lite;

import reactor.core.publisher.Mono;

public class Part02Mono {

//========================================================================================

	// Return an empty Mono
	public Mono<String> emptyMono() {
		return Mono.empty();
	}

//========================================================================================

	// Return a Mono that never emits any signal
	public Mono<String> monoWithNoSignal() {
		return Mono.never();
	}

//========================================================================================

	// Return a Mono that contains a "foo" value
	public Mono<String> fooMono() {
		return Mono.just("foo");
	}

//========================================================================================

	// Return a Mono of data. data can come null.
	public Mono<String> optionalMono(String data) {
		return Mono.justOrEmpty(data);
	}

//========================================================================================

	// Create a Mono that emits an IllegalStateException
	public Mono<String> errorMono() {
		return Mono.error(IllegalStateException::new);
	}

}
