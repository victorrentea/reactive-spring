package victor.training.reactive.reactor.advanced;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.just;

@ExtendWith(MockitoExtension.class)
public class ComplexTestExample {
   @InjectMocks
   private BizLogic bizLogic;
   @Mock
   private StringService stringService;
   @Mock
   private AlertService alertService;

   @Test
   public void testPublisher() {
      Mockito.when(stringService.parse("test")).thenReturn(just(1));

      TestPublisher<Void> alertTestMono = TestPublisher.createCold();
      alertTestMono.complete();
      Mockito.when(alertService.raise(1)).thenReturn(alertTestMono.mono());

      Mono<Integer> resultMono = bizLogic.bizLogic(just("test").publishOn(Schedulers.single()));

      StepVerifier.create(resultMono)
          .expectNext(1)
          .verifyComplete();
      alertTestMono.assertWasSubscribed();
   }
}
@Slf4j
@Component
@RequiredArgsConstructor
class BizLogic {
   private final StringService stringService;
   private final AlertService alertService;

   public Mono<Integer> bizLogic(Mono<String> inputMono) {
      return inputMono
          .flatMap(s -> stringService.parse(s))
          .delayUntil(i -> alertIfNecessary(i));
   }

   private Mono<Void> alertIfNecessary(Integer i) {
      if (i <= 4) {
         return alertService.raise(i).log("break-when-it-trouble"/*, "debug"*/);
      } else {
         return empty();
      }
   }
}
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
