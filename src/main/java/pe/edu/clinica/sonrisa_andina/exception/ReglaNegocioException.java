package pe.edu.clinica.sonrisa_andina.exception;

/**
 * Excepcion de negocio: se lanza cuando una operacion no cumple las reglas
 * de la clinica (cita solapada, fuera de horario, estado invalido, etc.).
 * El mensaje esta pensado para mostrarse directamente al usuario.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
