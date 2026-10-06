package integra.vacation_planner_backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Restaurant {
    @Id
    private UUID id;

    @Setter
    private String name;

    @Setter
    private String address;

    @Setter
    private LocalTime openingHour;

    @Setter
    private LocalTime closingHour;
}

