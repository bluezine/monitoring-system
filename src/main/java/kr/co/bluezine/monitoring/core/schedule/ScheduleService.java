package kr.co.bluezine.monitoring.core.schedule;

import java.util.Optional;

import org.hibernate.ObjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleBootService scheduleBootService;

    public Schedule insertSchedule(Schedule schedule) throws Exception {
        return handleSchedule(schedule);
    }

    public Schedule selectSchedule(Schedule schedule) {
        Schedule result = null;
        Optional<Schedule> server = scheduleRepository.findById(schedule.getId());
        server.orElseThrow(() -> new ObjectNotFoundException(schedule.getId(), schedule.getClass().getName()));
        if (server.isPresent())
            result = server.get();
        result.setLogs(null);
        return result;
    }

    public Schedule updateSchedule(Schedule schedule) throws Exception {
        Schedule result = null;
        Optional<Schedule> server = scheduleRepository.findById(schedule.getId());
        server.orElseThrow(() -> new ObjectNotFoundException(schedule.getId(), schedule.getClass().getName()));
        server.ifPresent((serverSchedule) -> {
            schedule.setJobId(serverSchedule.getJobId());
            schedule.setLogs(serverSchedule.getLogs());
        });
        result = handleSchedule(schedule);
        result.setLogs(null);
        return result;
    }

    public void deleteSchedule(Schedule schedule) throws Exception {
        Optional<Schedule> server = scheduleRepository.findById(schedule.getId());
        server.orElseThrow(() -> new ObjectNotFoundException(schedule.getId(), schedule.getClass().getName()));
        if (server.isPresent()) {
            Schedule serverSchedule = server.get();
            scheduleBootService.unRegisterSchedule(serverSchedule);
            scheduleRepository.delete(serverSchedule);
        }
    }

    public Page<Schedule> listSchedule(int page, int pagePerCount) {
        Page<Schedule> result = scheduleRepository.findAll(PageRequest.of(page, pagePerCount));
        result.getContent().forEach((item) -> item.setLogs(null));
        return result;
    }

    private Schedule handleSchedule(Schedule schedule) throws Exception {
        if (schedule.isEnable()) {
            schedule = scheduleBootService.unRegisterSchedule(schedule);
            return scheduleBootService.registerSchedule(schedule);
        } else {
            return scheduleBootService.unRegisterSchedule(schedule);
        }
    }

}
