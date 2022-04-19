package ru.job4j.quartz;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

public class Post {

  private int id;
  private String title;
  private String link;
  private String description;
  private LocalDateTime created;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Post)) {
      return false;
    }
    Post post = (Post) o;
    return id == post.id && Objects.equals(link, post.link);
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + (link != null ? link.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Post.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("title='" + title + "'")
        .add("link='" + link + "'")
        .add("description='" + description + "'")
        .add("created=" + created)
        .toString();
  }
}
