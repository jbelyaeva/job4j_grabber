package ru.job4j.quartz;

import java.sql.SQLException;
import java.util.List;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.*;
import java.util.Properties;
import ru.job4j.quartz.utils.HarbCareerDateTimeParser;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
  private static final String SOURCE_LINK = "https://career.habr.com";
  private final Properties cfg = new Properties();

  public Store store() throws SQLException {
    return new PsqlStore(cfg);
  }

  public Scheduler scheduler() throws SchedulerException {
    Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
    scheduler.start();
    return scheduler;
  }

  public void cfg() throws IOException {
    try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
      cfg.load(in);
    }
  }

  @Override
  public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
    JobDataMap data = new JobDataMap();
    data.put("store", store);
    data.put("parse", parse);
    JobDetail job = newJob(GrabJob.class)
        .usingJobData(data)
        .build();
    SimpleScheduleBuilder times = simpleSchedule()
        .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("rabbit.interval")))
        .repeatForever();
    Trigger trigger = newTrigger()
        .startNow()
        .withSchedule(times)
        .build();
    scheduler.scheduleJob(job, trigger);
  }

  public static class GrabJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
      JobDataMap map = context.getJobDetail().getJobDataMap();
      Store store = (Store) map.get("store");
      Parse parse = (Parse) map.get("parse");
      List<Post> postList = parse.list(SOURCE_LINK);
      for (Post postElement: postList) {
        store.save(postElement);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    Grabber grab = new Grabber();
    grab.cfg();
    Scheduler scheduler = grab.scheduler();
    Store store = grab.store();
    grab.init(new HabrCareerParse(new HarbCareerDateTimeParser()), store, scheduler);
  }
}