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

import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import victor.training.reactive.reactor.lite.domain.User;

import java.time.Duration;
import java.util.function.Function;

import static java.time.Duration.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class Part03StepVerifier {

//========================================================================================

   // Use StepVerifier to check that the flux parameter emits "foo" and "bar" elements then completes successfully.
   public void expectFooBarComplete(Flux<String> flux) {
      StepVerifier.create(flux).expectNext("foo").expectNext("bar").expectComplete().verify();
   }

//========================================================================================

   //  Use StepVerifier to check that the flux parameter emits "foo" and "bar" elements then a RuntimeException error.
   public void expectFooBarError(Flux<String> flux) {
      StepVerifier.create(flux).expectNext("foo").expectNext("bar").expectError(RuntimeException.class).verify();
   }

//========================================================================================

   // Use StepVerifier to check that the flux parameter emits a User with "swhite" username
   // and another one with "jpinkman" then completes successfully.
   public void expectSkylerJesseComplete(Flux<User> flux) {
      StepVerifier.create(flux)
              .assertNext(user -> assertThat("swhite").isEqualTo(user.getUsername()))
              .assertNext(user -> assertThat("jpinkman").isEqualTo(user.getUsername()))
              .verifyComplete();
   }

//========================================================================================

   // Expect 5 elements then complete and notice how long the test takes.
   public void expect5Elements(Flux<Long> flux) {
      StepVerifier.withVirtualTime(() -> flux)
              .expectSubscription()
              .thenRequest(Long.MAX_VALUE)
              .expectNextCount(5)
              .expectComplete();
   }

//========================================================================================

   // TODO Expect the value "later" to arrive 1 hour after subscribe(). Make the test complete in <1 second
   // Manipiulate virtual with StepVerifier#withVirtualTime/.thenAwait
   // TODO expect no signal for 30 minutes
   // Victor, please review specially this one ...
   public void expectDelayedElement() {
      StepVerifier.withVirtualTime(()->timeBoundFlow())
              .thenAwait(ofMinutes(60))
              .expectNext("later")
              .expectNoEvent(ofMinutes(30))
              .expectComplete();


   }

   public Mono<String> timeBoundFlow() {
      return Mono.just("later").delayElement(ofHours(1));
   }

   private void fail() {
      throw new AssertionError("workshop not implemented");
   }


//========================================================================================

//   prodMethod()

   // 🎖🌟🌟🌟🌟 WARNING HARD CORE 🌟🌟🌟🌟
   public void verifySubscribedOnce(Function<TestedProdClass, Mono<Void>> testedRxCode) {
      // given
      SomeRxRepo mockRepo = mock(SomeRxRepo.class);
      TestedProdClass testedObject = new TestedProdClass(mockRepo);

      // the goal of the test: make sure the Mono returned from save() is .sucbecribe to!

//      Flux.interval(ofSeconds(1))`;


      // TASK
      // I get from FE a req to create an order.
      // My app POSTs to another system. and returns to my client a number (an request ID).
      // I need a Rabbit listener + a way to store the 'request being waited'
      //


//      TestPublisher.
//      when(mockRepo.save(any())).thenReturn()

      // - data item, comp/err

      // 1: create a TestPublisher that tracks subscribe signals
      TestPublisher testPublisher = TestPublisher.create();
      // 2. complete it
      // 3. program the repoMock to return the stubbed mono

      // when
      testedObject.correct().block();
      // TODO uncomment and make pass
      Mono<Void> publisher = testedRxCode.apply(testedObject);
      publisher.block();

      // then
      // 4. assert the number of times the TestPublisher was subscribed to = 1
   }

   public interface SomeRxRepo {
      Mono<Void> save(User user);
   }

   //region tested production code
   @Data
   public static class TestedProdClass {
      private final SomeRxRepo mockRepo;

      public Mono<Void> correct() { // <-- first test this
         return mockRepo.save(User.SKYLER);
      }
      // try to test manually these or just use the

      public void noSubscribe() {
         mockRepo.save(User.SKYLER);
      }

      public Mono<Void> doOnNext_noSubscribe() {
         return Mono.<Void>fromRunnable(() -> {
                System.out.println("Pretend some remote work");
             })
             .doOnNext(x ->
                 mockRepo.save(User.SKYLER) // bad
//                 auditRepo.save(User.SKYLER).subscribe()
             );
      }
      public Mono<Void> noDataSignal_noSubscribe() {
         return Mono.<Void>fromRunnable(() -> {
                System.out.println("Pretend some remote work");
             })
             .flatMap(x -> mockRepo.save(User.SKYLER));
      }

      public Mono<Void> correct_chained() {
         return Mono.<Void>fromRunnable(() -> {
                System.out.println("Pretend some remote work");
             })
             .then(mockRepo.save(User.SKYLER)); // no need to subscribe because I return the publisher.
//         someone else will subsscribe to it later (90% SPring when you return fromaa RestController a Mono/Flux)

// in fully-reactive app, you don't see .subscribe()

      }

      public Mono<Void> twice_resubscribe() {
         Mono<Void> save = mockRepo.save(User.SKYLER);
         save.subscribe();
         return save;
      }
   }
   //endregion

}
