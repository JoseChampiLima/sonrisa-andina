package pe.edu.clinica.sonrisa_andina.api.dto;

import pe.edu.clinica.sonrisa_andina.domain.Odontologo;

public record OdontologoDto(
        Long id,
        String nombre,
        String dni,
        String especialidad,
        Long idEspecialidad,
        String nroColegiatura,
        boolean activo) {

    public static OdontologoDto de(Odontologo odontologo) {
        return new OdontologoDto(odontologo.getId(), odontologo.getNombreCompleto(),
                odontologo.getDni(), odontologo.getEspecialidad().getNombre(),
                odontologo.getEspecialidad().getId(), odontologo.getNroColegiatura(),
                odontologo.isActivo());
    }
}
