package victor.training.reactor.lite;

import lombok.extern.slf4j.Slf4j;
import victor.training.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Learn how to transform values.
 *
 * @author Sebastien Deleuze
 */
@Slf4j
public class Part04Transform {

//========================================================================================

	// TODO Capitalize the user username, firstname and lastname
	Mono<User> capitalizeOne(Mono<User> mono) {
		log.debug("produc noul MONO");
		return mono.map(user -> {
			log.debug("acum transform content mono ca a emis sursa");

			return capitalize(user);
		});
	}

	private User capitalize(User user) {
		return new User(
			user.getUsername().toUpperCase(),
			user.getFirstname().toUpperCase(),
			user.getLastname().toUpperCase());
	}

//========================================================================================

	// TODO Capitalize the users username, firstName and lastName
	Flux<User> capitalizeMany(Flux<User> flux) {
		return flux.map(this::capitalize);
	}

//========================================================================================

	// TODO Capitalize the users username, firstName and lastName using #asyncCapitalizeUser
	Flux<User> asyncCapitalizeMany(Flux<User> flux) {
//		FLux.flatMap(Mono<E>): Flux<E>
		 return flux.flatMap(user -> asyncCapitalizeUser(user));
	}

	Mono<User> asyncCapitalizeUser(User u) {
		return Mono.just(new User(u.getUsername().toUpperCase(), u.getFirstname().toUpperCase(), u.getLastname().toUpperCase()));
	}

}
