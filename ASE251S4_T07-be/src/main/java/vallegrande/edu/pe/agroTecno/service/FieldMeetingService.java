package vallegrande.edu.pe.agroTecno.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.FieldMeeting;

public interface FieldMeetingService {
    Flux<FieldMeeting> findAll();
    Flux<FieldMeeting> findByVisitStatus(String visitStatus);
    Mono<FieldMeeting> findById(String id);
    Mono<FieldMeeting> save(FieldMeeting fieldMeeting);
    Mono<FieldMeeting> update(String id, FieldMeeting fieldMeeting);
    Mono<FieldMeeting> delete(String id);
    Mono<FieldMeeting> restore(String id);
}
