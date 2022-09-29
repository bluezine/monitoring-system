package kr.co.bluezine.monitoring.core.schedule;

import org.springframework.data.repository.CrudRepository;

public interface ScheduleLogRepository extends CrudRepository<Schedule.ScheduleLog, String> {

    <T extends Schedule.ScheduleLog> T save(T scheduleLog);
}
