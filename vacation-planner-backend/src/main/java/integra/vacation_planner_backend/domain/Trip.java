package integra.vacation_planner_backend.domain;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@Entity
@EqualsAndHashCode
@Table(name = "trip")
public class Trip {

    @Id
    @GeneratedValue
    private UUID id;

    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String username;

}