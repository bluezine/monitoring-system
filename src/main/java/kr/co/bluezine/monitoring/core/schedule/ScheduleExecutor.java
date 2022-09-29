package kr.co.bluezine.monitoring.core.schedule;

import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@RequiredArgsConstructor
@Getter
@Setter
public abstract class ScheduleExecutor {

    protected abstract void init();

    protected abstract String execute() throws Exception;

    protected abstract String exceptionHandler(Throwable ex);

    protected abstract void destroy();
}
