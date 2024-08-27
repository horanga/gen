package primer.hackerton;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@RequiredArgsConstructor
@SpringBootApplication
public class HackertonApplication {

	public static void main(String[] args) {
		SpringApplication.run(HackertonApplication.class, args);
	}
}
