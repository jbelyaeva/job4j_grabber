package ru.job4j.quartz;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class HabrCareerParse {

  private static final String SOURCE_LINK = "https://career.habr.com";

  private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=",
      SOURCE_LINK);

  public static void main(String[] args) throws IOException {
    for (int countPage = 1; countPage < 6; countPage++) {
      Connection connection = Jsoup.connect(PAGE_LINK + countPage);
      Document document = connection.get();
      Elements rows = document.select(".vacancy-card__inner");
      rows.forEach(row -> {
        Element dateElement = row.select(".vacancy-card__date").first();
        String date = dateElement.child(0).attr("datetime");
        Element titleElement = row.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String vacancyName = titleElement.text();
        String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        System.out.printf("%s %s %s%n", vacancyName, link, date);
      });
    }
  }

  private String retrieveDescription(String link) throws IOException {
    StringJoiner sj = new StringJoiner(" ");
    Connection connection = Jsoup.connect(link);
    Document document = connection.get();
    Elements elements = document.select(".style-ugc").first().children();
    elements.forEach(elem -> {
      List<Node> descriptions = elem.childNodes();
      descriptions.forEach(d -> {
        if (!d.childNodes().isEmpty()) {
          String text = d.childNode(0).toString();
          sj.add(text);
        }
      });
    });
    return sj.toString();
  }
}