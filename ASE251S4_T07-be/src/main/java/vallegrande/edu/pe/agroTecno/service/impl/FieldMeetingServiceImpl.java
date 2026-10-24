package vallegrande.edu.pe.agroTecno.service.impl;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.FieldMeeting;
import vallegrande.edu.pe.agroTecno.repository.FieldMeetingRepository;
import vallegrande.edu.pe.agroTecno.service.FieldMeetingService;

@Service
public class FieldMeetingServiceImpl implements FieldMeetingService {

    private static final Set<String> VISIT_STATUSES = Set.of("Pendiente", "Completa", "Pospuesta", "Cancelada");
    private static final Pattern ID_PATTERN = Pattern.compile("^MET[0-9]{3,}$");

    private final FieldMeetingRepository fieldMeetingRepository;

    @Autowired
    public FieldMeetingServiceImpl(FieldMeetingRepository fieldMeetingRepository) {
        this.fieldMeetingRepository = fieldMeetingRepository;
    }

    @Override
    public Flux<FieldMeeting> findAll() {
        return fieldMeetingRepository.findAll();
    }

    @Override
    public Flux<FieldMeeting> findByVisitStatus(String visitStatus) {
        validateVisitStatus(visitStatus);
        return fieldMeetingRepository.findByVisitStatus(visitStatus);
    }

    @Override
    public Mono<FieldMeeting> findById(String id) {
        return fieldMeetingRepository.findById(id);
    }

    @Override
    public Mono<FieldMeeting> save(FieldMeeting fieldMeeting) {
        validate(fieldMeeting);
        fieldMeeting.setCreatedAt(LocalDateTime.now());
        return fieldMeetingRepository.save(fieldMeeting);
    }

    @Override
    public Mono<FieldMeeting> update(String id, FieldMeeting fieldMeeting) {
        validate(fieldMeeting);
        return fieldMeetingRepository.findById(id)
                .flatMap(existing -> {
                    fieldMeeting.setId(id);
                    existing.setMeetingDateTime(fieldMeeting.getMeetingDateTime());
                    existing.setCustomerId(fieldMeeting.getCustomerId());
                    existing.setFarmName(fieldMeeting.getFarmName());
                    existing.setFarmAddress(fieldMeeting.getFarmAddress());
                    existing.setOrderSalesId(fieldMeeting.getOrderSalesId());
                    existing.setFruitQuality(fieldMeeting.getFruitQuality());
                    existing.setObservation(fieldMeeting.getObservation());
                    existing.setVisitStatus(fieldMeeting.getVisitStatus());
                    existing.setUpdateAt(LocalDateTime.now());
                    return fieldMeetingRepository.save(existing);
                });
    }

    @Override
    public Mono<FieldMeeting> delete(String id) {
        return fieldMeetingRepository.findById(id)
                .flatMap(existing -> {
                    existing.setDeletedAt(LocalDateTime.now());
                    return fieldMeetingRepository.save(existing);
                });
    }

    @Override
    public Mono<FieldMeeting> restore(String id) {
        return fieldMeetingRepository.findById(id)
                .flatMap(existing -> {
                    existing.setRestoredAt(LocalDateTime.now());
                    return fieldMeetingRepository.save(existing);
                });
    }

    private void validate(FieldMeeting fieldMeeting) {
        if (fieldMeeting == null
                || fieldMeeting.getId() == null
                || !ID_PATTERN.matcher(fieldMeeting.getId()).matches()) {
            throw new IllegalArgumentException("id must match ^MET[0-9]{3,}$");
        }
        if (fieldMeeting.getMeetingDateTime() == null
                || isBlank(fieldMeeting.getCustomerId())
                || isBlank(fieldMeeting.getFarmName())
                || fieldMeeting.getFarmAddress() == null
                || isBlank(fieldMeeting.getOrderSalesId())) {
            throw new IllegalArgumentException("meeting_date_time, customer_id, farm_name, farm_address and order_sales_id are required");
        }
        validateVisitStatus(fieldMeeting.getVisitStatus());
    }

    private void validateVisitStatus(String visitStatus) {
        if (!VISIT_STATUSES.contains(visitStatus)) {
            throw new IllegalArgumentException("visit_status must be Pendiente, Completa, Pospuesta or Cancelada");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
