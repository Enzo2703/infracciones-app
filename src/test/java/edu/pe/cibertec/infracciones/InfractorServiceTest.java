package edu.pe.cibertec.infracciones;
import edu.pe.cibertec.infracciones.model.*;
import edu.pe.cibertec.infracciones.repository.*;
import edu.pe.cibertec.infracciones.service.impl.InfractorServiceImpl;
import edu.pe.cibertec.infracciones.service.impl.MultaServiceImpl;
import edu.pe.cibertec.infracciones.exception.InfractorBloqueadoException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InfractorServiceTest {

    @Mock
    private MultaRepository multaRepository;

    @Mock
    private InfractorRepository infractorRepository;

    @InjectMocks
    private MultaServiceImpl multaService;

    @InjectMocks
    private InfractorServiceImpl infractorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void calcularDeuda_debeCalcularCorrectamente() {

        Long infractorId = 1L;

        Multa multa1 = new Multa();
        multa1.setMonto(200.0);
        multa1.setEstado(EstadoMulta.PENDIENTE);

        Multa multa2 = new Multa();
        multa2.setMonto(300.0);
        multa2.setEstado(EstadoMulta.VENCIDA);

        when(multaRepository.findByInfractor_Id(infractorId))
                .thenReturn(Arrays.asList(multa1, multa2));

        double deuda = infractorService.calcularDeuda(infractorId);

        assertEquals(545.0, deuda);
    }

    @Test
    void desasignarVehiculo_sinMultasPendientes_debeEliminarVehiculo() {

        // ARRANGE
        Infractor infractor = new Infractor();
        infractor.setId(1L);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1L);

        infractor.setVehiculos(new ArrayList<>(List.of(vehiculo)));

        when(infractorRepository.findById(1L))
                .thenReturn(Optional.of(infractor));

        when(multaRepository.findByVehiculo_IdAndEstado(1L, EstadoMulta.PENDIENTE))
                .thenReturn(Collections.emptyList());

        // ACT
        infractorService.desasignarVehiculo(1L, 1L);

        // ASSERT
        assertTrue(infractor.getVehiculos().isEmpty());
        verify(infractorRepository).save(infractor);
    }

    @Test
    void transferirMulta_debeTransferirCorrectamente() {

        // ARRANGE
        Multa multa = new Multa();
        multa.setId(1L);
        multa.setEstado(EstadoMulta.PENDIENTE);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1L);
        multa.setVehiculo(vehiculo);

        Infractor infractorB = new Infractor();
        infractorB.setId(2L);
        infractorB.setBloqueado(false);
        infractorB.setVehiculos(new ArrayList<>(List.of(vehiculo)));

        when(multaRepository.findById(1L))
                .thenReturn(Optional.of(multa));

        when(infractorRepository.findById(2L))
                .thenReturn(Optional.of(infractorB));

        // ACT
        multaService.transferirMulta(1L, 2L);

        // ASSERT
        assertEquals(infractorB, multa.getInfractor());
        verify(multaRepository).save(multa);
    }

    @Test
    void transferirMulta_infractorBloqueado_noDebeGuardar() {

        // ARRANGE
        Infractor infractorA = new Infractor();
        infractorA.setId(1L);
        infractorA.setBloqueado(false);

        Infractor infractorB = new Infractor();
        infractorB.setId(2L);
        infractorB.setBloqueado(true); //  BLOQUEADO

        Multa multa = new Multa();
        multa.setId(1L);
        multa.setEstado(EstadoMulta.PENDIENTE);
        multa.setInfractor(infractorA);

        when(multaRepository.findById(1L)).thenReturn(Optional.of(multa));
        when(infractorRepository.findById(2L)).thenReturn(Optional.of(infractorB));

        // ArgumentCaptor
        ArgumentCaptor<Multa> multaCaptor = ArgumentCaptor.forClass(Multa.class);

        // ACT + ASSERT
        assertThrows(InfractorBloqueadoException.class, () -> {
            multaService.transferirMulta(1L, 2L);
        });

        // VERIFY
        verify(multaRepository, never()).save(any());
    }

}
