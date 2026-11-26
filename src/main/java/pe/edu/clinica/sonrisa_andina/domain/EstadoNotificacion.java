package pe.edu.clinica.sonrisa_andina.domain;

public enum EstadoNotificacion {
    PENDIENTE("Pendiente"),
    ENVIADA("Enviada"),
    FALLIDA("Fallida");

    private final String etiqueta;

    EstadoNotificacion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getCss() {
        return switch (this) {
            case PENDIENTE -> "badge-reprogramada";
            case ENVIADA -> "badge-atendida";
            case FALLIDA -> "badge-cancelada";
        };
    }
}
