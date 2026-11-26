package pe.edu.clinica.sonrisa_andina.api.dto;

import pe.edu.clinica.sonrisa_andina.domain.Especialidad;

public record EspecialidadDto(Long id, String nombre) {

    public static EspecialidadDto de(Especialidad especialidad) {
        return new EspecialidadDto(especialidad.getId(), especialidad.getNombre());
    }
}
