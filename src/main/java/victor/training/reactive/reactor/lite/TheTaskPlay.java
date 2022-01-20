//package victor.training.reactive.reactor.lite;
//
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.core.publisher.Sinks;
//import reactor.core.publisher.Sinks.Many;
//
//public class TheTaskPlay {
//
//   public static void main(String[] args) {
//
//
//      Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
//
//
//
//      sink.asFlux().subscribe(s -> System.out.println("1: " + s));
//      sink.tryEmitNext("a");
//      sink.asFlux()
//          .doOnTerminate()
//          .subscribe(s -> System.out.println("1: " + s));
//      Mono<String> m1,m2;
//
////      Flux.zip(m1,m2, (s1,s2) -> 1);
//   }
//}
