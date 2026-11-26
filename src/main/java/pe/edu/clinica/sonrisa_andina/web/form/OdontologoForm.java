package pe.edu.clinica.sonrisa_andina.web.form;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OdontologoForm {

    @NotBlank(message = "El DNI es obligatorio.")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 digitos.")
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios.")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios.")
    private String apellidos;

    @NotBlank(message = "El telefono es obligatorio.")
    @Pattern(regexp = "\\d{6,15}", message = "El telefono debe tener entre 6 y 15 digitos.")
    private String telefono;

    @NotNull(message = "Seleccione una especialidad.")
    private Long idEspecialidad;

    @NotBlank(message = "El numero de colegiatura es obligatorio.")
    @Size(max = 10, message = "El numero de colegiatura no debe superar 10 caracteres.")
    private String nroColegiatura;

    private boolean activo = true;

    @NotEmpty(message = "Seleccione al menos un dia de atencion.")
    private List<Integer> dias = new ArrayList<>();

    @NotNull(message = "Indique la hora de inicio de atencion.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime horaInicio = LocalTime.of(9, 0);

    @NotNull(message = "Indique la hora de fin de atencion.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime horaFin = LocalTime.of(20, 0);
}
