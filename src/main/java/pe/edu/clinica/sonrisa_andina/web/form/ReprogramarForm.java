package pe.edu.clinica.sonrisa_andina.web.form;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReprogramarForm {

    @NotNull(message = "Indique la nueva fecha.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "Indique la nueva hora.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime hora;
}
