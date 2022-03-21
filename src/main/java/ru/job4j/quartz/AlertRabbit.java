package ru.job4j.quartz;
import java.io.InputStream;
import java.util.Properties;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

  public static void main(String[] args) {
    try {
      Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
      scheduler.start();
      JobDetail job = newJob(Rabbit.class).build();
      SimpleScheduleBuilder times = simpleSchedule()
          .withIntervalInSeconds(getInterval())
          .repeatForever();
      Trigger trigger = newTrigger()
          .startNow()
          .withSchedule(times)
          .build();
      scheduler.scheduleJob(job, trigger);
    } catch (SchedulerException se) {
      se.printStackTrace();
    }
  }

  public static class Rabbit implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
      System.out.println("Rabbit runs here ...");
    }
  }

  public static int getInterval() {
    int interval;
    try (InputStream in = AlertRabbit.class.getClassLoader()
        .getResourceAsStream("rabbit.properties")) {
      Properties config = new Properties();
      config.load(in);
      interval = Integer.parseInt(config.getProperty("rabbit.interval"));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return interval;
  }
}