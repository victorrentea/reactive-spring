package victor.training.reactor.lite;

import reactor.core.scheduler.Schedulers;
import victor.training.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * Learn how to use various other operators.
 *
 * @author Sebastien Deleuze
 */
public class Part08OtherOperations {

//========================================================================================

	// TODO Create a Flux of user from Flux of username, firstname and lastname.
	Flux<User> userFluxFromStringFlux(Flux<String> usernameFlux, Flux<String> firstnameFlux, Flux<String> lastnameFlux) {
		// the way of the (stupid) warior
		//		Flux<Tuple2<String, String>> tuple2Flux = usernameFlux.zipWith(firstnameFlux, Tuples::of);
	//		return tuple2Flux.zipWith(lastnameFlux, (tamp2, lastName) -> new User(tamp2.getT1(), tamp2.getT1(), lastName));
		return Flux.zip(usernameFlux, firstnameFlux, lastnameFlux)
			.map(tuple3 -> new User(tuple3.getT1(), tuple3.getT2(), tuple3.getT3()));
	}

//========================================================================================

	// TODO Return the mono which returns its value faster
	Mono<User> useFastestMono(Mono<User> mono1, Mono<User> mono2) {
		return Mono.firstWithValue(mono1, mono2);
	}

//========================================================================================

	// TODO Return the flux which returns the first value faster
	Flux<User> useFastestFlux(Flux<User> flux1, Flux<User> flux2) {
		return Flux.firstWithValue(flux1, flux2);
	}

//========================================================================================

	// TODO Convert the input Flux<User> to a Mono<Void> that represents the complete signal of the flux
	Mono<Void> fluxCompletion(Flux<User> flux) {
		return flux.thenEmpty(persistFileImportSUCCESS()).then();
	}

	private Mono<Void> persistFileImportSUCCESS() {
		// Insert status in DB, do network call.
		return Mono.just("OK").delayElement(Duration.ofMillis(500)).then();
	}

//========================================================================================

	// TODO Return a valid Mono of user for null input and non null input user
	//  (hint: Reactive Streams do not accept null values)
	Mono<User> nullAwareUserToMono(User user) {
		return Mono.justOrEmpty(user);
	}

//========================================================================================

	// TODO Return the same mono passed as input parameter,
	//  except that it will emit User.SKYLER when empty
	Mono<User> emptyToSkyler(Mono<User> mono) {
		return mono.defaultIfEmpty(User.SKYLER);
	}

//========================================================================================

	// TODO Convert the input Flux<User> to a Mono<List<User>>
	//  containing list of collected flux values
	Mono<List<User>> fluxCollection(Flux<User> flux) {
//		flux.buffer(100)
//			.subscribeOn(Schedulers.boundedElastic())
//			.flatMap(page -> networkCall(page));
//		flux.window

		return flux.collectList();
	}

}
