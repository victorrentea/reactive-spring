package victor.training.reactive.reactor.pitfalls;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import victor.training.reactive.reactor.pitfalls.MissingSignals.Redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissingSignalsTest {

   @InjectMocks MissingSignals target;
   @Mock Redis redis;

   @Test
   void reportsToRedisIfCustomerFound() {
      // given
      when(redis.increaseApiUsageCounter(anyString())).thenReturn(Mono.empty());

      // when
      target.losingSignals(1).block();

      // then
      verify(redis).increaseApiUsageCounter("gigi");
   }
   @Test
   void reportsToRedisIfCustomerNOTFound() {
      // given
      when(redis.increaseApiUsageCounter(anyString())).thenReturn(Mono.empty());

      // when
      target.losingSignals(-1).block();

      // then
      verify(redis).increaseApiUsageCounter("gigi");
   }
}