package integra.vacation_planner_backend.scheduler;

import integra.vacation_planner_backend.service.FlightService;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;

@Component
public class FlightPriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(FlightPriceScheduler.class);

    private final FlightService flightService;

    public FlightPriceScheduler(FlightService flightService) {
        this.flightService = flightService;
    }

    @Scheduled(cron = "${flights.price.cron}", zone = "UTC")
    public void updateFlightPrices() {
        log.info("Running daily flight price update");
        flightService.applyDailyChange();
    }
}
