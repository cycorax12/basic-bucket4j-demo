package in.virendra.oswal.server;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;

@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

}

@RestController
class PlanetResource {

	private static final String X_RATE_REFILL_TIME = "X-RATE-REFILL-TIME";

	private static final String X_RATE_TOKEN_AVAILABLE = "X-RATE-TOKEN-AVAILABLE";

	private Bucket bucket;

	private final List<String> PLANETS = Arrays.asList(
			new String[] { "Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto" });

	@GetMapping(value = "/planet")
	public ResponseEntity<String> getPlanet() {

		ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
		if (probe.isConsumed()) {
			Collections.shuffle(PLANETS);
			return ResponseEntity.ok().header(X_RATE_TOKEN_AVAILABLE, Long.toString(probe.getRemainingTokens()))
					.body(PLANETS.get(0));
		} else {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
					.header(X_RATE_REFILL_TIME, Long.toString(probe.getNanosToWaitForRefill() / 1_000_000_000)).build();
		}

	}

	@PostConstruct
	public void setupBucket() {
		Bandwidth limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(1)));
		this.bucket = Bucket4j.builder().addLimit(limit).build();
	}
}
