package vallegrande.edu.pe.agroTecno.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import vallegrande.edu.pe.agroTecno.model.FieldMeeting;

public interface FieldMeetingRepository extends ReactiveMongoRepository<FieldMeeting, String> {
    Flux<FieldMeeting> findByVisitStatus(String visitStatus);
}
