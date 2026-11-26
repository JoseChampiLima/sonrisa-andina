package pe.edu.clinica.sonrisa_andina.api.dto;

import java.time.LocalDate;

public record InasistenciaResponse(
        LocalDate desde,
        LocalDate hasta,
        long totalCitas,
        long atendidas,
        long ausentes,
        long canceladas,
        double tasaInasistencia,
        double tasaCancelacion) {
}
