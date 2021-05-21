package victor.training.reactive.reactor.lite;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
@Slf4j
public class ReactorContext {
   public static void main(String[] args) throws InterruptedException {

      Mono.just("orderData")
          .flatMap(d->Repo.save(d))
          .contextWrite(context -> context.put("username","jane"))
          .flatMap(u -> Mono.deferContextual(c -> {
             log.debug((String) c.get("username"));
             return Mono.just("x");
          }).thenReturn(u))
          .doOnError(e->e.printStackTrace())
          .contextWrite(context -> context.put("username","johndoe"))
          .subscribe();


      Thread.sleep(1000);
   }
}

@Slf4j
class Repo {
   public static Mono<String> save(String d) {
      return Mono.deferContextual(context -> {
         log.debug("INSERT INT o... username=" + context.get("username"));
         return Mono.just("x");
      });
   }
}
