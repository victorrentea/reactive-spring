package victor.training.reactive.reactor.sample2;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import victor.training.reactive.intro.ThreadUtils;

import java.time.Duration;
import java.util.stream.Stream;

public class Two {


//   Ids are checked in a system (one call / 500 ms) and if not found there or error occurs, they are sent to another system

   private  CheckIdSystem checkIdSystem = new CheckIdSystem();
   private  PersonMS personMS;


   private ConnectableFlux<Long> hotFlux;


   private Sinks.Many<Person> personSink;
   private Flux<Person> singletonPersonFlux;


   public Two() {
      hotFlux = Flux.interval(Duration.ofMillis(500)).publish();
      hotFlux.connect();

      personSink = Sinks.many().multicast().onBackpressureBuffer();
      singletonPersonFlux = personSink.asFlux();

      hotFlux.zipWith(singletonPersonFlux, (t, p) -> p)
          .filterWhen(p -> checkIdSystem.existInRepo(p.getId()).map(b -> !b) )
//          .flatMap(person -> personMS.persist(person))
          .subscribe()
          ;
   }

   public static void main(String[] args) {

      Two two = new Two();
      new Thread(() -> two.method(Flux.fromStream(Stream.of("5L","a","5","6").map(Person::new))).block()).start();
      new Thread(() -> two.method(Flux.fromStream(Stream.of("5L","a","5","6").map(Person::new))).block()).start();
      ThreadUtils.sleep(5000);
   }



   public Mono<Void> method(Flux<Person> personFlux) {
      return personFlux.doOnNext(p -> personSink.tryEmitNext(p).orThrow()).then();
   }
}

@Slf4j
class CheckIdSystem {
   public Mono<Boolean> existInRepo(String id) {
      return Mono.fromSupplier(() -> {
         log.info("Call to SYSTEM");
         return true;
      });
   }
}
interface PersonMS {
   Mono<Long> persist(Person person);
}
@Data
class Person {
   private String id;

   public Person(String id) {
      this.id = id;
   }
}