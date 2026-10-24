package vallegrande.edu.pe.agroTecno.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import vallegrande.edu.pe.agroTecno.model.FieldMeeting;
import vallegrande.edu.pe.agroTecno.repository.FieldMeetingRepository;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FieldMeetingServiceImplTest {

    @Mock
    private FieldMeetingRepository fieldMeetingRepository;

    private FieldMeetingServiceImpl fieldMeetingService;

    @BeforeEach
    void setUp() {
        fieldMeetingService = new FieldMeetingServiceImpl(fieldMeetingRepository);
    }

    @Test
    void findAllReturnsMeetings() {
        FieldMeeting meeting = createMeeting("MET001", "Pendiente");
        when(fieldMeetingRepository.findAll()).thenReturn(Flux.just(meeting));

        StepVerifier.create(fieldMeetingService.findAll())
                .assertNext(m -> assertEquals("MET001", m.getId()))
                .verifyComplete();
    }

    @Test
    void findByVisitStatusReturnsMatchingMeetings() {
        FieldMeeting meeting = createMeeting("MET001", "Completa");
        when(fieldMeetingRepository.findByVisitStatus("Completa")).thenReturn(Flux.just(meeting));

        StepVerifier.create(fieldMeetingService.findByVisitStatus("Completa"))
                .assertNext(m -> assertEquals("Completa", m.getVisitStatus()))
                .verifyComplete();
    }

    @Test
    void findByVisitStatusThrowsWhenStatusInvalid() {
        assertThrows(IllegalArgumentException.class, () -> fieldMeetingService.findByVisitStatus("Invalido"));
        verify(fieldMeetingRepository, never()).findByVisitStatus(any());
    }

    @Test
    void saveSavesValidMeeting() {
        FieldMeeting meeting = createMeeting("MET002", "Pendiente");
        when(fieldMeetingRepository.save(any(FieldMeeting.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(fieldMeetingService.save(meeting))
                .assertNext(saved -> {
                    assertEquals("MET002", saved.getId());
                    assertNotNull(saved.getCreatedAt());
                })
                .verifyComplete();
    }

    @Test
    void saveThrowsWhenIdPatternInvalid() {
        FieldMeeting meeting = createMeeting("INVALID", "Pendiente");
        assertThrows(IllegalArgumentException.class, () -> fieldMeetingService.save(meeting));
        verify(fieldMeetingRepository, never()).save(any());
    }

    @Test
    void updateUpdatesExistingMeeting() {
        FieldMeeting existing = createMeeting("MET001", "Pendiente");
        FieldMeeting update = createMeeting("MET001", "Completa");
        update.setObservation("Visita realizada con exito");

        when(fieldMeetingRepository.findById("MET001")).thenReturn(Mono.just(existing));
        when(fieldMeetingRepository.save(any(FieldMeeting.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(fieldMeetingService.update("MET001", update))
                .assertNext(saved -> {
                    assertEquals("Completa", saved.getVisitStatus());
                    assertEquals("Visita realizada con exito", saved.getObservation());
                    assertNotNull(saved.getUpdateAt());
                })
                .verifyComplete();
    }

    private FieldMeeting createMeeting(String id, String status) {
        FieldMeeting meeting = new FieldMeeting();
        meeting.setId(id);
        meeting.setMeetingDateTime(LocalDateTime.now());
        meeting.setCustomerId("CUS001");
        meeting.setFarmName("Fundo San Jose");
        meeting.setFarmAddress(Map.of("district", "Ica"));
        meeting.setOrderSalesId("1");
        meeting.setFruitQuality("Buena");
        meeting.setVisitStatus(status);
        return meeting;
    }
}
