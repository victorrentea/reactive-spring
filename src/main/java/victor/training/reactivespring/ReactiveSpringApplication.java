package victor.training.reactivespring;

import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@SpringBootApplication
public class ReactiveSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveSpringApplication.class, args);
	}

}


@RestController
class Res {
	@GetMapping
	Publisher<String> get() {
		return Flux.interval(Duration.ofMillis(500)).map(i -> " "+ i);
	}
}