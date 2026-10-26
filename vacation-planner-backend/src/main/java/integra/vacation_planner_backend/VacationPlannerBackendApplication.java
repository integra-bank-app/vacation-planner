package integra.vacation_planner_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VacationPlannerBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VacationPlannerBackendApplication.class, args);
    }
}
