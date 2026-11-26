package pe.edu.clinica.sonrisa_andina.web.form;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServicioForm {

    @NotBlank(message = "El nombre del servicio es obligatorio.")
    @Size(max = 80, message = "El nombre no debe superar 80 caracteres.")
    private String nombre;

    @NotNull(message = "Indique la duracion en minutos.")
    @Min(value = 5, message = "La duracion minima es de 5 minutos.")
    private Integer duracionMin;

    @NotNull(message = "Indique el precio referencial.")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo.")
    private BigDecimal precio = BigDecimal.ZERO;

    private boolean activo = true;

    @NotEmpty(message = "Seleccione al menos una especialidad habilitada.")
    private List<Long> especialidades = new ArrayList<>();
}
