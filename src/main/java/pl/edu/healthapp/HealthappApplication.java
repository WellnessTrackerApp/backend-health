package pl.edu.healthapp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
		info = @Info(
				title = "Lifestyle & Health Advisor API",
				version = "1.0",
				description = "API documentation for tracking and analyzing health data"
		)
)
@SpringBootApplication
public class HealthappApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthappApplication.class, args);
	}

}
