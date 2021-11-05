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

	public ReactiveRepository<User> repository = new ReactiveUserRepository();

//========================================================================================

	// TODO Create a StepVerifier that initially requests all values and expect 4 values to be received then a COMPLETION
	public StepVerifier requestAllExpectFourThenComplete(Flux<User> flux) {
		return null;
	}

//========================================================================================

	// TODO Create a StepVerifier that initially requests all values and expect 3 values to be received
	// Note: don't care for COMPLETION.
	public StepVerifier requestAllExpectThree(Flux<User> flux) {
		return null;
	}

//========================================================================================

	// TODO Create a StepVerifier that initially requests 1 value and expects User.SKYLER then requests another value and expects User.JESSE then stops verifying by cancelling the source
	public StepVerifier requestOneExpectSkylerThenRequestOneExpectJesse(Flux<User> flux) {
		return null;
	}

//========================================================================================

	// TODO Return a Flux with all users stored in the repository that prints automatically logs for all Reactive Streams signals
	public Flux<User> fluxWithLog() {
		return null;
	}

//========================================================================================

	// TODO Return a Flux with all users stored in the repository that prints "Starring:" on subscribe, "firstname lastname" for all values and "The end!" on complete
	public Flux<User> fluxWithDoOnPrintln() {
		return null;
	}

//========================================================================================

	// TODO no matter how many items are requested to the flux you return, the upstream should be requested at most 10 at once
	public Flux<Integer> throttleUpstreamRequest(Flux<Integer> upstream) {
		return upstream;
	}

}
