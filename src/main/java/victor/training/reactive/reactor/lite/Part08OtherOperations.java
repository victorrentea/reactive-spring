package victor.training.reactive.reactor.lite;

import reactor.function.TupleUtils;
import reactor.util.function.Tuples;
import victor.training.reactive.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static reactor.function.TupleUtils.function;

/**
 * Learn how to use various other operators.
 *
 * @author Sebastien Deleuze
 */
public class Part08OtherOperations {

//========================================================================================

	// TODO Create a Flux of user from Flux of username, firstname and lastname.
	Flux<User> userFluxFromStringFlux(Flux<String> usernameFlux, Flux<String> firstnameFlux, Flux<String> lastnameFlux) {
//		return firstnameFlux.zipWith(lastnameFlux)
//			.zipWith(usernameFlux, (fullName, username) ->
//			new User( username, fullName.getT1(), fullName.getT2()));
		return Flux.zip(usernameFlux, firstnameFlux, lastnameFlux)
//			.map(tuple -> new User(tuple.getT1(), tuple.getT2(), tuple.getT3()));
//			.map(function((username, first,last) -> new User(username, first, last)));
			.map(function(User::new));
	}

//========================================================================================

	// TODO Return the mono which returns its value faster
	Mono<User> useFastestMono(Mono<User> mono1, Mono<User> mono2) {
		return Mono.firstWithValue(mono1,mono2);
	}

//========================================================================================

	// TODO Return the flux which returns the first value faster
	Flux<User> useFastestFlux(Flux<User> flux1, Flux<User> flux2) {
		return Flux.firstWithValue(flux1,flux2);
	}

//========================================================================================

	// TODO Convert the input Flux<User> to a Mono<Void> that represents the complete signal of the flux
	Mono<Void> fluxCompletion(Flux<User> flux) {
		return flux.then();
	}

//========================================================================================

	// TODO Return a valid Mono of user for null input and non null input user
	//  (hint: Reactive Streams do not accept null values)
	Mono<User> nullAwareUserToMono(User user) {
//		if (user == null) {
//			return Mono.empty();
//		}
		return Mono.justOrEmpty(user);
	}

//========================================================================================

	// TODO Return the same mono passed as input parameter, except that it will emit User.SKYLER when empty
	Mono<User> emptyToSkyler(Mono<User> mono) {
		return mono.defaultIfEmpty(User.SKYLER);
	}

//========================================================================================

	// TODO Convert the input Flux<User> to a Mono<List<User>> containing list of collected flux values
	Mono<List<User>> fluxCollection(Flux<User> flux) {
		return flux.collectList();
	}

}
