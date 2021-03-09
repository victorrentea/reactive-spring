package victor.training.reactor.lite;

import reactor.core.publisher.Flux;
import victor.training.reactivespring.start.ThreadUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.time.Duration.ofMillis;
import static java.util.Arrays.asList;

/**
 * Learn how to create Flux instances.
 *
 * @author Sebastien Deleuze
 * @see <a href="https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html">Flux Javadoc</a>
 */
public class Part01Flux {

//========================================================================================

	// TODO Return an empty Flux
	Flux<String> emptyFlux() {
		return Flux.empty();
	}

//========================================================================================

	// TODO keep: manual emit

	// TODO Return a Flux that contains 2 values "foo" and "bar" without using an array or a collection
	Flux<String> fooBarFluxFromValues() {
		return Flux.just("foo", "bar")
			.doOnNext(s -> System.out.println("Vad " + s))
			.doOnComplete(()-> System.out.println("GATA!"));
		// idem cu
//		return Flux.create(sink -> {
//			sink.next("foo");
//			sink.next("bar");
//			sink.complete();
//		});
	}
	Flux<String> fooBarFluxFromValuesAsync() {
//		return Flux.just("foo", "bar");
		Flux<String> flux = Flux.create(sink -> {

			addCallback(new Consumer<String>() {
				@Override
				public void accept(String s) {
					if (s.equals("END")) {
						sink.complete();
					} else {
						sink.next(s);
					}
				}
			});
		});
		new Thread(() -> {
			ThreadUtils.sleep(100);
			// din alt thread, iti arunca eventuri
			for (Consumer<String> stringConsumer : abonati) {
				stringConsumer.accept("foo");
				stringConsumer.accept("bar");
				stringConsumer.accept("END");
			}}).start();

		return flux;
	}

	List<Consumer<String>> abonati = new ArrayList<>();
	// Ne inchipuim ca te abonezi la o sursa de eventuri externa (think, button click, mesaj pe coada)
	public void addCallback(Consumer<String> handler) {
		abonati.add(handler);
	}

//========================================================================================

	// TODO Create a Flux from a List that contains 2 values "foo" and "bar"
	Flux<String> fooBarFluxFromList() {
		List<String> strings = asList("foo", "bar");
		return Flux.fromIterable(strings);
	}

//========================================================================================

	// TODO Create a Flux that emits an IllegalStateException
	Flux<String> errorFlux() {
		// un Flux este o secventa de 0..N elemente urmate de COMPLETE sau ERROR
		return Flux.error(new IllegalStateException());
	}

	// TODO keep
	Flux<String> fooBarFluxThenError() {
		return Flux.create(sink -> {
			sink.next("foo");
			sink.next("bar");
			sink.error(new IllegalArgumentException());
		});
	}

//========================================================================================

		// TODO Create a Flux that emits increasing values from 0 to 9 each 100ms
	Flux<Long> counter() {
		return Flux.interval(ofMillis(100)).take(10);
	}

}
