package in.virendra.oswal.client;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);

	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}

@Component
class Runner implements CommandLineRunner {

	Logger LOG = LoggerFactory.getLogger(Runner.class);

	@Autowired
	RestTemplate _rt;

	@Override
	public void run(String... args) throws Exception {
		while (true) {
			Thread.sleep(10000);
			try {
				ResponseEntity<String> response = _rt.getForEntity(new URI("http://localhost:8001/planet"),
						String.class);
				if (response.getStatusCode() == HttpStatus.OK) {
					LOG.info(String.format("Planet received %s\n Headers: %s", response.getBody(),
							response.getHeaders().toString()));
				}
			} catch (HttpClientErrorException ex) {
				if (ex.getRawStatusCode() == 429) {
					LOG.warn("Rate limit exhausted");
					LOG.info("Await time before refill: " + ex.getResponseHeaders().toString());
				}
			}

		}
	}
}