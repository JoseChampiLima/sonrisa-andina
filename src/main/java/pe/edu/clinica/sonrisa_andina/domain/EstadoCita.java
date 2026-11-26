package pe.edu.clinica.sonrisa_andina.domain;

/**
 * Estados por los que pasa una cita durante su ciclo de vida.
 */
public enum EstadoCita {
    PROGRAMADA("Programada"),
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada"),
    REPROGRAMADA("Reprogramada"),
    ATENDIDA("Atendida"),
    AUSENTE("Ausente");

    private final String etiqueta;

    EstadoCita(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Estados que ya no admiten cambios de agenda. */
    public boolean esFinal() {
        return this == ATENDIDA || this == AUSENTE;
    }

    /** Estados que ocupan un espacio de la agenda del odontologo. */
    public boolean ocupaAgenda() {
        return this != CANCELADA && this != REPROGRAMADA;
    }

    /** Clase CSS usada en las vistas (badge). */
    public String getCss() {
        return switch (this) {
            case PROGRAMADA -> "badge-programada";
            case CONFIRMADA -> "badge-confirmada";
            case CANCELADA -> "badge-cancelada";
            case REPROGRAMADA -> "badge-reprogramada";
            case ATENDIDA -> "badge-atendida";
            case AUSENTE -> "badge-ausente";
        };
    }
}
