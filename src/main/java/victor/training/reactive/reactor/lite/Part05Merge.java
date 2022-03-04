package victor.training.reactive.reactor.lite;

import lombok.extern.slf4j.Slf4j;
import victor.training.reactive.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class Part05Merge {

//========================================================================================

	// TODO Merge flux1 and flux2 values with interleave
	public Flux<User> mergeFluxWithInterleave(Flux<User> flux1, Flux<User> flux2) {
		return flux1.doOnNext(e -> log.info("BEFORE1 MERGE: " + e.toString()))
			.doOnSubscribe(s -> log.info("RIGHT NOW mergeWith() subscribes to the flux1"))
			.mergeWith(flux2.doOnNext(e -> log.info("BEFORE2 MERGE: " + e.toString())))
			.doOnNext(e -> log.info("AFTER MERGE: " + e.toString()));
	}

//========================================================================================

	// TODO Merge flux1 and flux2 values with no interleave (flux1 values and then flux2 values)
	public Flux<User> mergeFluxWithNoInterleave(Flux<User> flux1, Flux<User> flux2) {
		return flux1.concatWith(flux2);
	}

//========================================================================================

	// TODO Create a Flux containing the value of mono1 then the value of mono2
	public Flux<User> createFluxFromMultipleMono(Mono<User> mono1, Mono<User> mono2) {
		return null;
	}

}
