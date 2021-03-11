package victor.training.reactor.lite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactivespring.start.ThreadUtils;

import java.time.Duration;

public class Play {
   public static void main(String[] args) {

      Mono<String> dateDinDbMono = Mono.defer(() -> Mono.just(blockingDBSelect()))
          .subscribeOn(Schedulers.boundedElastic());

//      dateDinDbMono.subscribe(Play::doStuff1WithDBData);
//      dateDinDbMono.subscribe(Play::doStuff2WithDBData); // 2nd DB call
      // cum faci 2 chestii separate pe baza aceluiasi mONO fara sa executi de 2 ori apelul expensive

      // 1) doOnNext daca stuff1 e NON-BLOCKING
//      dateDinDbMono
//          .doOnNext(Play::doStuff1WithDBData)
//          .subscribe(Play::doStuff2WithDBData);

      // 2) le inlantui
//      dateDinDbMono // Mono<String>
//          .flatMap(date -> expensiveStuff1(date)
//              .thenReturn(date)
//          )
//          .subscribe(Play::doStuff2WithDBData);

      // 3) doua scumpe in secventa
//      dateDinDbMono // Mono<String>
//          .flatMap(date -> expensiveStuff1(date)
//              .thenReturn(date)
//          )
//          .flatMap(date1 -> expensiveStuff2(date1))
//         .subscribe();
//       3) doua scumpe in paralel - recomandat
      dateDinDbMono // Mono<String>
          .flatMap(date -> {
                 Mono<Void> mono1 = expensiveStuff2(date).subscribeOn(Schedulers.boundedElastic());
                 Mono<Void> mono2 = expensiveStuff1(date).subscribeOn(Schedulers.parallel());
                 return Flux.concat(mono1, mono2).then();
              }
          )
          .retry(2)
         .subscribe();




      ThreadUtils.sleep(300);

   }


   private static final Logger log = LoggerFactory.getLogger(Play.class);
   public static Mono<Void> expensiveStuff1(String date) {
      return Mono.fromRunnable(() -> {
         log.info("Stuff1 " + date);
      });
   }
   public static Mono<Void> expensiveStuff2(String date) {
      return Mono.fromRunnable(() -> {
         log.info("Stuff2 " + date);
      });
   }


   public static void doStuff1WithDBData(String date) {
      System.out.println("Stuff1 " + date);
   }
   public static void doStuff2WithDBData(String date) {
      System.out.println("Stuff2 " + date);
   }

   private static String blockingDBSelect() {
      System.out.printf("DB call\n");
      if (Math.random()<.5) throw new IllegalArgumentException();
      System.out.printf("DB call OK\n");
      return "lol";
   }

}
