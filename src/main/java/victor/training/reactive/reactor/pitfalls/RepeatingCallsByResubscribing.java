package victor.training.reactive.reactor.pitfalls;

import lombok.SneakyThrows;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import victor.training.reactive.reactor.complex.Product;

import java.util.concurrent.ExecutionException;

public class RepeatingCallsByResubscribing {
   public static void get(String[] args) throws ExecutionException, InterruptedException {

      Mono<Integer> voidMono = httpCall()
          .then(Mono.fromCallable(() -> {
             System.out.println("this after the completion of HTTP");
             return 1;
          }))
//          .cache()
          ;

//      HttpServletRequest

      voidMono.subscribe(data -> {
         System.out.println("Data via calback: "+ data);
      });
      voidMono.toFuture().get();

   }

   private static void someMethod(Mono<Void> voidMono) {

   }

   public static Mono<Void> httpCall() {
      return WebClient.create().get()
          .uri("http://localhost:9999/api/audit-resealed/")
          .retrieve()
          .toBodilessEntity()
//             .doOnSubscribe(s -> log.info("Calling Audit REST"))
          .then();
   }

}
