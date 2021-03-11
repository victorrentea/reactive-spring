package victor.training.reactor.pitfalls;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import victor.training.reactivespring.start.ThreadUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class RaceVariables {

   public class Repo {
      public Flux<String> findAll() {
         return Flux.interval(ofMillis(20)).take(10).map(i -> "User" + i);
      }
   }
   @RequiredArgsConstructor
   public static class SharingVariables {
      private final Repo repo;

      int camp;
      public Flux<String> problem() {
         AtomicInteger count = new AtomicInteger();
         return repo.findAll()
             .doOnNext(t -> {
                camp++;
                count.incrementAndGet();
             })
             .doOnComplete(() -> System.out.println("Count: " + camp));
      }
   }


   @Test
   public void doNotMutateLocalVariables() throws InterruptedException {
      SharingVariables sharing = new SharingVariables(new Repo());
      Flux<String> flux = sharing.problem();
      flux.subscribe();
      ThreadUtils.sleep(1000);
      sharing.problem().subscribe();
      // TODO .subscribe again to the same flux

      Thread.sleep(300);
   }
}
