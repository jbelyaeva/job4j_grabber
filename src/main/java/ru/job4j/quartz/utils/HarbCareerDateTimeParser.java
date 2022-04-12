package ru.job4j.quartz.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class HarbCareerDateTimeParser implements DateTimeParser {

  @Override
  public LocalDateTime parse(String parse) {
    return LocalDateTime.parse(parse, new DateTimeFormatterBuilder().appendOptional(
        DateTimeFormatter.ISO_DATE_TIME).toFormatter());
  }
}
