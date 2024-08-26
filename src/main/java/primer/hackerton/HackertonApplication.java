package primer.hackerton;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import primer.hackerton.s3.openapi.OpenApiManager;

import java.io.IOException;

@EnableScheduling
@EnableRetry
@RequiredArgsConstructor
@SpringBootApplication
public class HackertonApplication {

	private final OpenApiManager openApiManager;

	public static void main(String[] args) {
		SpringApplication.run(HackertonApplication.class, args);
	}

	@Bean
	public CommandLineRunner run() {
		return args -> {
			System.out.println("애플리케이션이 백그라운드에서 시작됩니다.");
		};
	}

	@Scheduled(fixedDelay = 5000, initialDelay = 5000)
	public void startProcessReports() throws InterruptedException, IOException {
		openApiManager.processReports("C:\\Users\\정연호\\Desktop\\공부방법\\이력서\\해커톤\\프라이머GenAI\\hackerton\\output1.csv");
	}
}
