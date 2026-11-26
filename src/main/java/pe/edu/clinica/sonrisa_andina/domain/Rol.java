package pe.edu.clinica.sonrisa_andina.domain;

public enum Rol {
    ADMIN("Administrador"),
    RECEPCION("Recepcion"),
    ODONTOLOGO("Odontologo"),
    PACIENTE("Paciente");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
