package victor.training.reactive.reactor.lite;

import victor.training.reactive.reactor.lite.domain.User;
import victor.training.reactive.reactor.lite.repository.ReactiveRepository;
import victor.training.reactive.reactor.lite.repository.ReactiveUserRepository;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Learn how to control the demand.
 *
 * @author Sebastien Deleuze
 */
public class Part06Request {

	ReactiveRepository<User> repository = new ReactiveUserRepository();

//========================================================================================

	// TODO Create a StepVerifier that initially requests all values and expect 4 values to be received
	StepVerifier requestAllExpectFour(Flux<User> flux) {
		return StepVerifier.create(flux)
			.thenRequest(4)
			.expectNextCount(4)
			.expectComplete();
	}

//========================================================================================

	// TODO Create a StepVerifier that initially requests 1 value and expects User.SKYLER
	//  then requests another value and expects User.JESSE then stops verifying by cancelling the source
	StepVerifier requestOneExpectSkylerThenRequestOneExpectJesse(Flux<User> flux) {
		return StepVerifier.create(flux)
			.thenRequest(1)
			.expectNextMatches(u -> u.equals(User.SKYLER))
			.thenRequest(1)
			.expectNextMatches(u -> u.equals(User.JESSE))
			.thenCancel();
	}

//========================================================================================

	// TODO Return a Flux with all users stored in the repository that prints
	//  automatically logs for all Reactive Streams signals
	Flux<User> fluxWithLog() {
		return repository.findAll().onBackpressureDrop().log();
	}

//========================================================================================

	// TODO Return a Flux with all users stored in the
	//  repository that prints "Starring:" on subscribe, "firstname lastname" for all values and "The end!" on complete
	Flux<User> fluxWithDoOnPrintln() {
		return repository.findAll()
				.doOnSubscribe(subscription -> System.out.println("Starring:"))
				.doOnNext(user -> System.out.println(user.getFirstname() + " " + user.getLastname()))
				.doOnComplete(() -> System.out.println("The end!"));
	}

}
