package ru.job4j.quartz;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class AlertRabbit {

  public static void main(String[] args) {
    Properties properties = getProperties();
    try (Connection cn = init(properties)) {
      List<Long> store = new ArrayList<>();
      Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
      scheduler.start();
      JobDataMap data = new JobDataMap();
      data.put("store", store);
      data.put("connection", cn);
      JobDetail job = newJob(Rabbit.class)
          .usingJobData(data)
          .build();
      SimpleScheduleBuilder times = simpleSchedule()
          .withIntervalInSeconds(Integer.parseInt(properties.getProperty("rabbit.interval")))
          .repeatForever();
      Trigger trigger = newTrigger()
          .startNow()
          .withSchedule(times)
          .build();
      scheduler.scheduleJob(job, trigger);
      Thread.sleep(10000);
      scheduler.shutdown();
      System.out.println(store);
    } catch (Exception se) {
      se.printStackTrace();
    }
  }

  public static class Rabbit implements Job {

    @Override
    public void execute(JobExecutionContext context) {
      System.out.println("Rabbit runs here ...");
      List<Long> store = (List<Long>) context.getJobDetail().getJobDataMap().get("store");
      Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("connection");
      store.add(System.currentTimeMillis());
      add(cn);
    }
  }

  public static void add(Connection cn) {
    ru.job4j.quartz.Rabbit rabbit = new ru.job4j.quartz.Rabbit();
    try (PreparedStatement ps = cn.prepareStatement(
        "insert into rabbit (created_date) values (?);",
        PreparedStatement.RETURN_GENERATED_KEYS)) {
      ps.setTimestamp(1, Timestamp.valueOf(rabbit.getCreatedDate()));
      if (ps.executeUpdate() == 1) {
        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            rabbit.setId(generatedKeys.getInt(1));
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static Connection init(Properties properties) {
    Connection connection = null;
    try {
    Class.forName(properties.getProperty("driver-class-name"));
    connection = DriverManager.getConnection(
        properties.getProperty("url"),
        properties.getProperty("username"),
        properties.getProperty("password")
    );
    } catch (Exception e) {
      e.printStackTrace();
    }
    return connection;
  }

  public static Properties getProperties() {
    Properties properties = new Properties();
    try (InputStream in = AlertRabbit.class.getClassLoader()
        .getResourceAsStream("rabbit.properties")) {
      properties.load(in);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return properties;
  }
}