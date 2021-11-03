package victor.training.reactive.intro;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

public class UnFluxInfiniti {
   public static void main(String[] args) throws InterruptedException {

      // TODO convert to HOT
      Flux<?> flux = Flux.interval(Duration.ofMillis(500))

          .map(n -> n + "= " + UUID.randomUUID().toString())

//          .onBackpressureBuffer() // OOME
          ;
      flux.doOnNext(date -> System.out.println("Unu:" + date))
          .subscribe();


      Thread.sleep(2000);
      flux
          .doOnNext(date -> System.out.println("DOi:" + date))
          .buffer(100)
          .blockLast();


   }
   // In general echipele dupa ani merg de la Mono<List> la Flux, dar...

//   > La return mai usor as merge de la Mono<LIst<T>> la Flux<T>
             // dand flux iti poate da callerul backpressure
            // si poate procesa 'on the fly' si 'da mai departe'
//   > la param as prefera List<> in loc de Flux<> daca
         //   datele deja le ai toate in mem.
      /// dar cine stie volumele maine ?... (- catalin) :P

}
