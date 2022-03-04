package victor.training.reactive.reactor.lite;

import reactor.core.publisher.Mono;

public class Part02Mono {

//========================================================================================

	// TODO Return an empty Mono
	public Mono<String> emptyMono() {
		return Mono.empty();
	}

//========================================================================================

	// TODO Return a Mono that never emits any signal
	public Mono<String> monoWithNoSignal() {
		return null;
	}

//========================================================================================

	// TODO Return a Mono that contains a "foo" value
	public Mono<String> fooMono() {
		return Mono.just("foo");
	}

//========================================================================================

	public Mono<Void> methodReturningVoid() {
		// stuff
		return Mono.empty();
	}
	public Mono<Integer> methodThrowing(boolean b) {
		// stuff
		if (b) {
//			throw new IllegalArgumentException();
			return Mono.error(new IllegalArgumentException());
		}
		return Mono.just(1);
	}

	// TODO Return a Mono of data. data can come null.
	public Mono<String> optionalMono(String data) {

		return Mono.empty();
	}

//========================================================================================

	// TODO Create a Mono that emits an IllegalStateException
	public Mono<String> errorMono() {
		return null;
	}

}
