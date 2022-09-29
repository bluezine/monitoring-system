package kr.co.bluezine.monitoring.core.schedule;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface ScheduleRepository extends CrudRepository<Schedule, String> {

    <T extends Schedule> T save(T schedule);

    Optional<Schedule> findById(String id);

    void delete(Schedule schedule);

    long count();

    Page<Schedule> findAll(Pageable pageable);
}
