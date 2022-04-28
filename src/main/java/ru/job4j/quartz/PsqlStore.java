package ru.job4j.quartz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

  private static Connection cnn;

  public PsqlStore(Properties cfg) throws SQLException {
    try {
      Class.forName(cfg.getProperty("driver-class-name"));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    cnn = DriverManager.getConnection(
        cfg.getProperty("url"),
        cfg.getProperty("username"),
        cfg.getProperty("password")
    );
  }

  @Override
  public void save(Post post) {
    try (PreparedStatement ps = cnn.prepareStatement(
        "insert into post(name, link, text, created) values (?, ?, ?, ?);",
        PreparedStatement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, post.getTitle());
      ps.setString(2, post.getLink());
      ps.setString(3, post.getDescription());
      ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
      if (ps.executeUpdate() == 1) {
        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            post.setId(generatedKeys.getInt(1));
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public List<Post> getAll() {
    List<Post> list = new ArrayList<>();
    try (PreparedStatement ps = cnn.prepareStatement("select * from post")) {
      try (ResultSet resultSet = ps.executeQuery()) {
        while (resultSet.next()) {
          list.add(getPost(resultSet));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }

  private Post getPost(ResultSet resultSet) throws SQLException {
    return new Post(
        resultSet.getInt("id"),
        resultSet.getString("name"),
        resultSet.getString("text"),
        resultSet.getString("link"),
        resultSet.getTimestamp("created").toLocalDateTime());
  }

  @Override
  public Post findById(int id) {
   Post postNew = null;
    try (PreparedStatement ps = cnn.prepareStatement("select * from post where id=?")) {
      ps.setInt(1, id);
      try (ResultSet resultSet = ps.executeQuery()) {
        if (resultSet.next()) {
          postNew = getPost(resultSet);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return postNew;
  }

  @Override
  public void close() throws Exception {
    if (cnn != null) {
      cnn.close();
    }
  }
}