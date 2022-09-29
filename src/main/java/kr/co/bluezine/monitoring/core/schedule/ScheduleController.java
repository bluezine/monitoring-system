package kr.co.bluezine.monitoring.core.schedule;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.co.bluezine.monitoring.core.web.CommonExceptionHandler;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleController extends CommonExceptionHandler {

    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<Schedule> insertSchedule(@RequestBody Schedule schedule) throws Exception {
        return new ResponseEntity<Schedule>(scheduleService.insertSchedule(schedule), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Schedule> selectSchedule(@RequestBody Schedule schedule) throws Exception {
        return new ResponseEntity<Schedule>(scheduleService.selectSchedule(schedule), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Schedule> updateSchedule(@RequestBody Schedule schedule) throws Exception {
        return new ResponseEntity<Schedule>(scheduleService.updateSchedule(schedule), HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Schedule> deleteSchedule(@RequestBody Schedule schedule) throws Exception {
        scheduleService.deleteSchedule(schedule);
        return new ResponseEntity<Schedule>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("list")
    public ResponseEntity<Page<Schedule>> listSchedule(@RequestParam int page, @RequestParam int pagePerCount)
            throws Exception {
        return new ResponseEntity<Page<Schedule>>(scheduleService.listSchedule(page, pagePerCount), HttpStatus.OK);
    }
}
