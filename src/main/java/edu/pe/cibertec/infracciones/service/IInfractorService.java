package edu.pe.cibertec.infracciones.service;

import edu.pe.cibertec.infracciones.dto.InfractorRequestDTO;
import edu.pe.cibertec.infracciones.dto.InfractorResponseDTO;
import java.util.List;

public interface IInfractorService {
    InfractorResponseDTO registrarInfractor(InfractorRequestDTO dto);
    InfractorResponseDTO obtenerInfractorPorId(Long id);
    List<InfractorResponseDTO> obtenerTodos();
    void desasignarVehiculo(Long infractorId, Long vehiculoId);
    double calcularDeuda(Long infractorId);


}