package kr.co.bluezine.monitoring.schedule.sample;

import java.util.Random;

import kr.co.bluezine.monitoring.core.schedule.ScheduleExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SampleScheduleExecutor extends ScheduleExecutor {

    private final SampleSchedule schedule;

    @Override
    protected void init() {
        log.info("SampleScheduleExecutor ::: init()");
    }

    @Override
    protected String execute() throws Exception {
        log.info("Sample Schedule ::: {}", schedule.getUuid());
        if (new Random().nextInt(5) == 0) {
            throw new Exception("sample exception");
        }
        return "success";
    }

    @Override
    protected String exceptionHandler(Throwable ex) {
        return "fail";
    }

    @Override
    protected void destroy() {
        log.info("SampleScheduleExecutor ::: destroy()");
    }
}
