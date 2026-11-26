package pe.edu.clinica.sonrisa_andina.api.dto;

import java.math.BigDecimal;
import java.util.List;

import pe.edu.clinica.sonrisa_andina.domain.Servicio;

public record ServicioDto(
        Long id,
        String nombre,
        int duracionMin,
        BigDecimal precio,
        boolean activo,
        List<String> especialidades) {

    public static ServicioDto de(Servicio servicio) {
        return new ServicioDto(servicio.getId(), servicio.getNombre(), servicio.getDuracionMin(),
                servicio.getPrecio(), servicio.isActivo(),
                servicio.getEspecialidades().stream().map(e -> e.getNombre()).sorted().toList());
    }
}
