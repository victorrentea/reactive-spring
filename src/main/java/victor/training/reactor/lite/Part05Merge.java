package victor.training.reactor.lite;

import victor.training.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Learn how to merge flux.
 *
 * @author Sebastien Deleuze
 */
public class Part05Merge {

//========================================================================================

	// TODOnd flux2  Merge flux1 avalues with interleave
	Flux<User> mergeFluxWithInterleave(Flux<User> flux1, Flux<User> flux2) {
//		Mono.<Void>fromRunnable(() -> System.out.println(1))
//			.then(Mono.fromRunnable(() -> System.out.println(2)))
//			.subscribe();
//		return flux1.concatWith(flux2); // asta le porneste unul dupa altul
		return flux1.mergeWith(flux2);
	}

//========================================================================================

	// TODO Merge flux1 and flux2 values with no interleave (flux1 values and then flux2 values)
	Flux<User> mergeFluxWithNoInterleave(Flux<User> flux1, Flux<User> flux2) {
		return flux1.concatWith(flux2);
	}

//========================================================================================

	// TODO Create a Flux containing the value of mono1 then the value of mono2
	Flux<User> createFluxFromMultipleMono(Mono<User> mono1, Mono<User> mono2) {
		return mono1.concatWith(mono2);
	}

}
