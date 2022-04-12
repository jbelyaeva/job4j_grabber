package ru.job4j.quartz.utils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class HarbCareerDateTimeParser implements DateTimeParser {

  @Override
  public LocalDateTime parse(String parse) {
    return ZonedDateTime.parse(parse).toLocalDateTime();
  }
}
