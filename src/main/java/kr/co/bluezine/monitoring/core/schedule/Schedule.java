package kr.co.bluezine.monitoring.core.schedule;

import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true, value = { "scheduleExecutor" })
@Entity(name = "schedule")
@Getter
@Setter
public class Schedule {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    private String name;
    private String module;
    private String cron;
    private String json;
    private String jobId;

    @ColumnDefault(value = "false")
    private boolean enable;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private List<Schedule.ScheduleLog> logs;

    @Transient
    private Optional<ScheduleExecutor> scheduleExecutor;

    @Entity(name = "schedule_log")
    @Data
    public static class ScheduleLog {
        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        private String id;

        @Column(name = "schedule_id")
        private String scheduleId;

        private String message;

        private String exception;
    }
}
