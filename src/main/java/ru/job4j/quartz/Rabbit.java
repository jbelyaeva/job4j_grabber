package ru.job4j.quartz;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Rabbit {

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

  private int id;

  private LocalDateTime createdDate = LocalDateTime.now();

  public Rabbit() {
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public LocalDateTime getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(LocalDateTime created) {
    this.createdDate = createdDate;
  }

  @Override
  public String toString() {
    return String.format("id: %s, created_date: %s", id, FORMATTER.format(createdDate));
  }
}
