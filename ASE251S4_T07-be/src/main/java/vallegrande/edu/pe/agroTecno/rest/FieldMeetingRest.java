package vallegrande.edu.pe.agroTecno.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.FieldMeeting;
import vallegrande.edu.pe.agroTecno.service.FieldMeetingService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/api/field-meetings")
public class FieldMeetingRest {

    private final FieldMeetingService fieldMeetingService;

    @Autowired
    public FieldMeetingRest(FieldMeetingService fieldMeetingService) {
        this.fieldMeetingService = fieldMeetingService;
    }

    @GetMapping
    public Flux<FieldMeeting> findAll() {
        return fieldMeetingService.findAll();
    }

    @GetMapping("/status/{status}")
    public Flux<FieldMeeting> findByVisitStatus(@PathVariable String status) {
        return fieldMeetingService.findByVisitStatus(status);
    }

    @GetMapping("/{id}")
    public Mono<FieldMeeting> findById(@PathVariable String id) {
        return fieldMeetingService.findById(id);
    }

    @PostMapping
    public Mono<FieldMeeting> save(@RequestBody FieldMeeting fieldMeeting) {
        return fieldMeetingService.save(fieldMeeting);
    }

    @PutMapping("/{id}")
    public Mono<FieldMeeting> update(@PathVariable String id, @RequestBody FieldMeeting fieldMeeting) {
        return fieldMeetingService.update(id, fieldMeeting);
    }

    @PatchMapping("/delete/{id}")
    public Mono<FieldMeeting> delete(@PathVariable String id) {
        return fieldMeetingService.delete(id);
    }

    @PatchMapping("/restore/{id}")
    public Mono<FieldMeeting> restore(@PathVariable String id) {
        return fieldMeetingService.restore(id);
    }
}
