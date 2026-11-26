package pe.edu.clinica.sonrisa_andina.web.form;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CitaForm {

    @NotNull(message = "Seleccione un paciente.")
    private Long idPaciente;

    @NotNull(message = "Seleccione un odontologo.")
    private Long idOdontologo;

    @NotNull(message = "Seleccione un servicio.")
    private Long idServicio;

    @NotNull(message = "Indique la fecha de la cita.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "Indique la hora de la cita.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime hora;

    @Size(max = 200, message = "El motivo no debe superar 200 caracteres.")
    private String motivo;
}
