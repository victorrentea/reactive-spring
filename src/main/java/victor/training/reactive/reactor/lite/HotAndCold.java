package victor.training.reactive.reactor.lite;

import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.intro.ThreadUtils;

import java.time.Duration;

public class HotAndCold {
   public static void main(String[] args) {
      new HotAndCold().met();
      ThreadUtils.sleep(2000);
   }

   public void met() {


      // Hot publisher = external events that occur ar random moments
      // multiple subscribers will find out about data published AFTER they subscribe

      // COld publisher (99%) = nothing happens until you .subscribe()
      // Imagine you want to bring 1TB of data from Mono and process it with 2 subscriber

      Flux<String> f = Flux.just("a","b").delayElements(Duration.ofMillis(100));//~mongo.query();

		Flux<String> conn = f.publish().autoConnect(2);
		conn.subscribe(d->team1Func(d)); // might miss the data.
//		sleep(1);
		conn.subscribe(d->team2Func(d)); // might miss the data.
//		conn.connect();

      f.delayUntil(this::team1Func)
          .delayUntil(this::team2Func)
          .subscribe();

      // or (better):
//		f.flatMap(func2).subscribe(func1);

   }

   public Mono<Void> team1Func(String data) {
      System.out.println("Process1 " + data);
      return Mono.empty();
   }
   public Mono<Void> team2Func(String data) {
      System.out.println("Process2 " + data);
      return Mono.empty();
   }

}
