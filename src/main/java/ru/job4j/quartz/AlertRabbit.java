package ru.job4j.quartz;
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
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

  public static void main(String[] args) {
    try (Connection cn = init()) {
      init();
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
          .withIntervalInSeconds(getInterval())
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

  public static Connection init() {
    Connection connection;
    try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
      Properties config = new Properties();
      config.load(in);
      Class.forName(config.getProperty("driver-class-name"));
      connection = DriverManager.getConnection(
          config.getProperty("url"),
          config.getProperty("username"),
          config.getProperty("password")
      );
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return connection;
  }
}