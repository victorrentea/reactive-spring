package victor.training.reactive.reactor.lite;

import reactor.core.publisher.Mono;

import java.time.Duration;

public class Part02Mono {

//========================================================================================

	// TODO Return an empty Mono
	public Mono<String> emptyMono() {
//		return Mono.just(null); // Neither Flux nor Mono can emit a NULL. EVER.
		return Mono.empty()
//			.flatMap(e -> someCall(e)) // this call never happens PITFALLS
			;
	}

//========================================================================================

	// TODO Return a Mono that never emits any signal
	public Mono<String> monoWithNoSignal() {
		return Mono.never()
//			.timeout(Duration.ofSeconds(10))
			; // never used in prod, only for testing.
	}

//========================================================================================

	// TODO Return a Mono that contains a "foo" value
	public Mono<String> fooMono() {
		return Mono.just("foo");
	}

//========================================================================================

	// TODO Return a Mono of data. data can come null.
	public Mono<String> optionalMono(String data) {
		return Mono.justOrEmpty(data);
//		return Mono.just(data);
	}

//========================================================================================

	// TODO Create a Mono that emits an IllegalStateException
	public Mono<String> errorMono() {
		return Mono.error(new IllegalStateException());
	}

}
