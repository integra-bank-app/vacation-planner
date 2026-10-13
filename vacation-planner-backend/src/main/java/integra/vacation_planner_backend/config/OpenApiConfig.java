package integra.vacation_planner_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vacationPlannerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Vacation Planner API")
                .description("API documentation for Vacation Planner")
                .version("1.0.0"));
    }
}
