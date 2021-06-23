/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package victor.training.reactive.reactor.lite;

import java.time.Duration;

import org.mockito.Mockito;
import reactor.test.publisher.TestPublisher;
import victor.training.reactive.reactor.lite.domain.User;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.when;

/**
 * Learn how to use StepVerifier to test Mono, Flux or any other kind of Reactive Streams Publisher.
 *
 * @author Sebastien Deleuze
 * @see <a href="https://projectreactor.io/docs/test/release/api/reactor/test/StepVerifier.html">StepVerifier Javadoc</a>
 */
public class Part03StepVerifierTest {

	Part03StepVerifier workshop = new Part03StepVerifier();

//========================================================================================

	@Test
	public void expectFooBarComplete() {
		workshop.expectFooBarComplete(Flux.just("foo", "bar"));
	}

//========================================================================================

	@Test
	public void expectFooBarError() {
		workshop.expectFooBarError(Flux.just("foo", "bar").concatWith(Mono.error(new RuntimeException())));
	}

//========================================================================================

	@Test
	public void expectSkylerJesseComplete() {
		workshop.expectSkylerJesseComplete(Flux.just(new User("swhite", null, null), new User("jpinkman", null, null)));
	}

//========================================================================================

	@Test
	public void expect10Elements() {
		workshop.expect10Elements(Flux.interval(Duration.ofSeconds(1)).take(10));
	}

//========================================================================================

	@Test
	public void expectDelayedElement() {
		workshop.expectDelayedElement();
	}

//========================================================================================

	@Test
	public void testPublisher() {
		class AlertService {
			public Mono<Void> raise(int level) {
				return empty();
			}
		}
		class StringService {
			public Mono<Integer> parse(String s) {
				return Mono.just(s.length());
			}
		}
		StringService stringService = Mockito.mock(StringService.class);
		TestPublisher<Integer> stringPublisher = TestPublisher.createCold();
		Mockito.when(stringService.parse("test")).thenReturn(stringPublisher.mono());
		stringPublisher.emit(4);

		AlertService alertService = Mockito.mock(AlertService.class);
		TestPublisher<Void> alertPublisher = TestPublisher.createCold();
		Mockito.when(alertService.raise(4)).thenReturn(alertPublisher.mono());
		alertPublisher.complete();

		Mono.just("test")
			.flatMap(s -> stringService.parse(s))
			.doOnNext(i -> {
				if (i <= 4) {
					System.out.println("OK");
				} else {
					System.out.println("KO");
				}
			})
			.delayUntil(i ->  (i<=4) ?alertService.raise(i):empty())
			.block();


		alertPublisher.assertWasSubscribed();

//		workshop.expectDelayedElement();
	}

}
