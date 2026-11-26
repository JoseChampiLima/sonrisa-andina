package pe.edu.clinica.sonrisa_andina.repository.projection;

import pe.edu.clinica.sonrisa_andina.domain.EstadoCita;

public interface ConteoPorEstado {
    EstadoCita getEstado();

    long getTotal();
}
