package ru.job4j.quartz;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.quartz.utils.DateTimeParser;

public class HabrCareerParse implements Parse {

  private final DateTimeParser dateTimeParser;

  public HabrCareerParse(DateTimeParser dateTimeParser) {
    this.dateTimeParser = dateTimeParser;
  }

  private String retrieveDescription(String link) throws IOException {
    Connection connection = Jsoup.connect(link);
    Document document = connection.get();
    Element descriptionElement = document.selectFirst(".style-ugc");
    return descriptionElement.text();
  }

  @Override
  public List<Post> list(String link) throws IOException {
    List<Post> list = new ArrayList<>();
    String pageLink = String.format("%s/vacancies/java_developer?page=", link);
    for (int countPage = 1; countPage < 6; countPage++) {
      Connection connection = Jsoup.connect(pageLink + countPage);
      Document document = connection.get();
      Elements rows = document.select(".vacancy-card__inner");
      rows.forEach(row -> {
        Element dateElement = row.select(".vacancy-card__date").first();
        LocalDateTime date = dateTimeParser.parse(dateElement.child(0).attr("datetime"));
        Element titleElement = row.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String vacancyName = titleElement.text();
        String linkOnVacancy = String.format("%s%s", link, linkElement.attr("href"));
        String description = null;
        try {
          description = retrieveDescription(linkOnVacancy);
        } catch (IOException e) {
          e.printStackTrace();
        }
        list.add(new Post(vacancyName, linkOnVacancy, description, date));
      });
    }
    return list;
  }
}