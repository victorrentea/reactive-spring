package victor.training.reactor.lite;

import reactor.core.Disposable;
import victor.training.reactivespring.start.ThreadUtils;
import victor.training.reactor.lite.domain.User;
import victor.training.reactor.lite.repository.ReactiveRepository;
import victor.training.reactor.lite.repository.ReactiveUserRepository;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static java.time.Duration.ofMillis;

/**
 * Learn how to control the demand.
 *
 * @author Sebastien Deleuze
 */
public class Part06Request {

	ReactiveRepository<User> repository = new ReactiveUserRepository();

//========================================================================================

	// TODO Create a StepVerifier that initially requests all values and expect 4 values to be received
	StepVerifier requestAllExpectFour(final Flux<User> flux) {
		Flux<User> flux2 = flux.log();
		return StepVerifier.create(flux2)
			.expectNextCount(4)
			.expectComplete();
	}

//========================================================================================

	// TODO Create a StepVerifier that initially requests 1 value and expects
	//  User.SKYLER then requests another value and expects User.JESSE
	//  then stops verifying by cancelling the source
	StepVerifier requestOneExpectSkylerThenRequestOneExpectJesse(Flux<User> flux) {
//		flux.scan
		return StepVerifier.create(flux)
			.expectNext(User.SKYLER)
			.expectNext(User.JESSE)
			.thenCancel();
	}

//========================================================================================

	// TODO Return a Flux with all users stored in the repository that prints
	//  automatically logs for all Reactive Streams signals
	Flux<User> fluxWithLog() {
		return repository.findAll()
			.log()
			;
	}

//========================================================================================

	// TODO Return a Flux with all users stored in the repository that prints
	//  "Starring:" on subscribe,
	//  "firstname lastname" for all values and
	//  "The end!" on complete
	Flux<User> fluxWithDoOnPrintln() {
		return repository.findAll()
			.doOnSubscribe(subscription -> System.out.println("Starring:"))
			.doOnNext(user -> System.out.println(user.getFirstname() + " " + user.getLastname()))
			.doOnComplete(() -> { // SUCCESSFUL completion
				// real world: ack the message on kafka
				// send a notification: "data exported"
				System.out.println("The end!");
			})
			.doFinally(signalType -> { // ~ finally {
				// close a file that you wrote to
			})
			;
	}

	public static void main(String[] args) {
		Disposable disposable = Flux.interval(ofMillis(100))
//			.take(2)
			.doOnNext(n -> System.out.println(n))
			.doOnCancel(() -> System.out.println("CANCEL!!!"))
			.doAfterTerminate(() -> System.out.println("COMPLETE or ERROR"))
			.doFinally(signal -> System.out.println("fianlly {" + signal))
			.subscribe();

		ThreadUtils.sleep(1000);
		disposable.dispose();
		ThreadUtils.sleep(100);
		System.out.println("exit");

	}

}
