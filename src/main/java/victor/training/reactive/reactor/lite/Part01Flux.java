package victor.training.reactive.reactor.lite;

import reactor.core.publisher.Flux;
import victor.training.reactive.spring.mongo.Event;
import victor.training.reactive.spring.mongo.EventReactiveRepo;

import javax.swing.*;
import java.time.Duration;
import java.util.List;

public class Part01Flux {

//========================================================================================
	// TODO Return an empty Flux
	public Flux<String> emptyFlux() {
//		return from webflux REST endpoint allEvents.map(toDto);
//			.map(list -> list.stream)
//		List<Event> allEvents = mongoRepo.findAll();// in a blocking ap
		return Flux.empty(); // ~ empty list
	}

//========================================================================================

	// TODO Return a Flux that contains 2 values "foo" and "bar" without using an array or a collection
	public Flux<String> fooBarFluxFromValues() {
//		return Flux.just("foo","bar");
		JButton ohGodNo;
		return Flux.create(sink -> {
//			ohGodNo.addActionListener(e -> sink.next("Boo"));
			sink.next("foo");
			sink.next("bar");
			sink.complete();
		});
	}

//========================================================================================

	// TODO Create a Flux from a List that contains 2 values "foo" and "bar"
	public Flux<String> fluxFromList(List<String> list) {
		return Flux.fromIterable(list);
	}

//========================================================================================

	// TODO Create a Flux that emits an IllegalStateException
	public Flux<String> errorFlux() {
//		Flux.range(1, 1000)
//			.flatMap(id -> nonBockingCallA(id))
//			.flatMap(objA -> nonBockingCallB(objB))
//		;
		return Flux.create(sink -> {
			sink.error(new IllegalStateException());
			sink.next("IMPOSSIBLE, violates reactive streams "); // AFTER an ERROR no DATA or COMPLETION ever comes.
			;
		});

//		return Flux.error(new IllegalStateException());
	}

//========================================================================================

	// TODO Create a Flux that emits increasing values from 0 to 9 each 100ms
	public Flux<Long> countEach100ms() {
		return Flux.interval(Duration.ofMillis(100));
	}

}
