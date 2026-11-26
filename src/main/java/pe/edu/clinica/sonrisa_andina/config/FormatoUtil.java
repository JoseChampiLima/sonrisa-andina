package pe.edu.clinica.sonrisa_andina.config;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Utilidades de formato usadas desde las plantillas Thymeleaf
 * (por ejemplo: ${@fmt.fechaHora(cita.inicio)}).
 */
@Component("fmt")
public class FormatoUtil {

    private static final Locale PE = new Locale("es", "PE");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy", PE);
    private static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", PE);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm", PE);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", PE);
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("EEEE", PE);

    public String fecha(LocalDate valor) {
        return valor == null ? "" : valor.format(FECHA);
    }

    public String fechaLarga(LocalDate valor) {
        return valor == null ? "" : capitalizar(valor.format(FECHA_LARGA));
    }

    public String hora(LocalTime valor) {
        return valor == null ? "" : valor.format(HORA);
    }

    public String fechaHora(LocalDateTime valor) {
        return valor == null ? "" : valor.format(FECHA_HORA);
    }

    public String dia(LocalDate valor) {
        return valor == null ? "" : capitalizar(valor.format(DIA));
    }

    public String diaHora(LocalDateTime valor) {
        return valor == null ? "" : capitalizar(valor.format(DateTimeFormatter.ofPattern("EEE dd/MM", PE)))
                + " " + valor.format(HORA);
    }

    public String moneda(BigDecimal valor) {
        if (valor == null) {
            return "S/ 0.00";
        }
        DecimalFormat formato = new DecimalFormat("#,##0.00");
        return "S/ " + formato.format(valor);
    }

    public String porcentaje(double valor) {
        return String.format(PE, "%.1f %%", valor);
    }

    public String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase(PE) + texto.substring(1);
    }
}
