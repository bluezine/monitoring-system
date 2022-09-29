package kr.co.bluezine.monitoring.core.schedule;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.json.JSONObject;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean.StatefulMethodInvokingJob;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
class ScheduleBootService {
    public static boolean init = false;

    private final ApplicationContext applicationContext;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleLogRepository scheduleLogRepository;
    private final Scheduler scheduler;

    private static final Map<String, Schedule> schedules = new HashMap<>();

    @PostConstruct
    private void init() {
        log.info("ScheduleBootService ::: init() ::: Start");
        Long totalCount = scheduleRepository.count();
        Stream.iterate(0, n -> n + 1).limit((totalCount / 4) + 1).parallel().forEach((page) -> {
            PageRequest pageRequest = PageRequest.of((int) page, 4);
            scheduleRepository.findAll(pageRequest).forEach((schedule) -> {
                try {
                    schedule = convertSchedule(schedule);
                    if (schedule.isEnable())
                        registerSchedule(schedule);
                    log.debug("ScheduleBootService ::: init() ::: RegisterSchedule ::: {}[{}]", schedule.getName(),
                            schedule.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        init = true;
        log.info("ScheduleBootService ::: init() ::: End");
    }

    Schedule registerSchedule(Schedule schedule) throws Exception {
        log.debug("ScheduleBootService ::: registerSchedule() ::: {}[{}]", schedule.getName(), schedule.getId());
        schedule = convertSchedule(schedule);

        Map<String, Object> jobDataMap = new HashMap<>();
        jobDataMap.put("schedule", schedule);
        jobDataMap.put("applicationContext", applicationContext);

        JobDetail jobDetail = buildJobDetail(ScheduleWrapper.class, jobDataMap);
        schedule.setJobId(jobDetail.getKey().getName());

        Constructor<?> constructor = Class.forName(schedule.getModule() + "Executor")
                .getConstructor(schedule.getClass());
        schedule.setScheduleExecutor(Optional.ofNullable(
                (ScheduleExecutor) constructor.newInstance(schedule)));
        schedule.getScheduleExecutor().get().init();
        scheduler.scheduleJob(jobDetail, buildJobTrigger(schedule.getCron()));

        schedules.put(schedule.getId(), schedule);
        return scheduleRepository.save(new ObjectMapper().convertValue(schedule, Schedule.class));
    }

    public Schedule convertSchedule(Schedule schedule) throws Exception {
        JSONObject scheduleObj = new JSONObject(schedule);
        JSONObject scheduleJson = new JSONObject(scheduleObj.getString("json"));
        scheduleJson.keySet().stream().forEach((key) -> scheduleObj.put(key, scheduleJson.get(key)));

        return (Schedule) new ObjectMapper().readValue(scheduleObj.toString(),
                Class.forName(scheduleObj.getString("module")));
    }

    Schedule unRegisterSchedule(Schedule schedule) throws Exception {
        log.debug("ScheduleBootService ::: unRegisterSchedule() ::: {}[{}]", schedule.getName(), schedule.getId());
        if (StringUtils.hasText(schedule.getJobId()) && scheduler.checkExists(new JobKey(schedule.getJobId())))
            scheduler.deleteJob(new JobKey(schedule.getJobId()));
        schedules.remove(schedule.getId());

        if (schedules.containsKey(schedule.getId())) {
            schedule.setScheduleExecutor(schedules.get(schedule.getId()).getScheduleExecutor());
            schedule.getScheduleExecutor()
                    .ifPresent((scheduleExecutor) -> scheduleExecutor.destroy());
        }

        schedule.setJobId(null);
        return scheduleRepository.save(new ObjectMapper().convertValue(schedule, Schedule.class));
    }

    private JobDetail buildJobDetail(Class<? extends Job> job, Map<String, Object> params) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.putAll(params);

        return JobBuilder.newJob(job).usingJobData(jobDataMap).build();
    }

    private Trigger buildJobTrigger(String scheduleExp) {
        return TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(scheduleExp)).build();
    }

    @PreDestroy
    private void destroy() {
        log.info("ScheduleBootService ::: destroy() ::: Start");
        schedules.forEach((id, target) -> {
            target.getScheduleExecutor().ifPresent((scheduleExecutor) -> scheduleExecutor.destroy());
        });
        scheduleRepository.findAll().forEach((schedule) -> {
            schedule.setJobId(null);
            scheduleRepository.save(schedule);
        });
        log.info("ScheduleBootService ::: destroy() ::: End");
    }

    class ScheduleWrapper extends StatefulMethodInvokingJob {
        private Schedule schedule;

        @Override
        protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

            schedule = (Schedule) jobDataMap.get("schedule");
            if (schedule.getScheduleExecutor().isPresent()) {
                JSONObject result = new JSONObject();
                try {
                    result.put("message", schedule.getScheduleExecutor().get().execute());
                    log(result);
                } catch (Exception ex) {
                    result.put("message", schedule.getScheduleExecutor().get().exceptionHandler(ex));
                    result.put("exception", ex.getClass().getName() + ": " + ex.getMessage());
                    log(result);
                    throw new JobExecutionException(ex);
                }
            }
        }

        @SneakyThrows
        private void log(JSONObject log) {
            Schedule.ScheduleLog scheduleLog = new ObjectMapper().readValue(log.toString(), Schedule.ScheduleLog.class);
            scheduleLog.setScheduleId(schedule.getId());
            scheduleLogRepository.save(scheduleLog);
        }
    }
}
