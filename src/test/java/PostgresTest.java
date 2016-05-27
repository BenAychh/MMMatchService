
import com.goebl.david.Request;
import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author benhernandez
 */
public class PostgresTest {
  @BeforeClass
  public static void beforeAll() throws SQLException{
      String host = System.getenv("PG_PORT_5432_TCP_ADDR");
      String port = System.getenv("PG_PORT_5432_TCP_PORT");
      if (host == null) {
          host = "localhost";
      }
      if (port == null) {
          port = "5432";
      }
      Connection connection = DriverManager.getConnection(
          "jdbc:postgresql://" + host + ":"
              + port + "/Interested?user=postgres");
      Statement statement = connection.createStatement();
      String dropQuery = "DROP TABLE IF EXISTS public.interested;";
      String query = "CREATE TABLE public.interested\n" +
              "(\n" +
              "  id serial,\n" + 
              "  email character varying NOT NULL,\n" +
              "  interested_in character varying NOT NULL,\n" +
              "  CONSTRAINT interested_pkey PRIMARY KEY (id)\n" +
              ")";
      statement.execute(dropQuery);
      statement.execute(query);
      statement.close();
      connection.close();
  }
  @Before
  public void beforeEach() throws SQLException {
    String host = System.getenv("PG_PORT_5432_TCP_ADDR");
      String port = System.getenv("PG_PORT_5432_TCP_PORT");
      if (host == null) {
          host = "localhost";
      }
      if (port == null) {
          port = "5432";
      }
      Connection connection = DriverManager.getConnection(
          "jdbc:postgresql://" + host + ":"
              + port + "/Interested?user=postgres");
      Statement statement = connection.createStatement();
      String dropQuery = "delete from interested";
      statement.execute(dropQuery);
      String query = "insert into interested (email, interested_in)"
          + "values ('testy@test.com', 'user1@email.com'),"
          + "('testy@test.com', 'user2@email.com'),"
          + "('testy@test.com', 'user3@email.com'),"
          + "('user1@email.com', 'testy@test.com'),"
          + "('user2@email.com', 'testy@test.com')";
      statement.execute(query);
      statement.close();
      connection.close();
  }
  @Test
  public void getMatchesTest() throws Exception {
    Webb webb = Webb.create();
    JSONObject payload = new JSONObject();
    payload.put("email", "inserttest@gmail.com");
    Request request = webb
            .get("http://localhost:8002/matches?email=testy@test.com");
    Response<JSONObject> response = request
            .asJsonObject();
    JSONObject result = response.getBody();
    JSONObject expected = new JSONObject();
    expected.put("message", "Matches");
    expected.put("status", 200);
    JSONArray matches = new JSONArray();
    matches.put("user1@email.com");
    matches.put("user2@email.com");
    expected.put("matches", matches);
    JSONAssert.assertEquals(expected, result, true);
    Assert.assertEquals(200, response.getStatusCode());
  }
    @Test
  public void getInterestedInTest() throws Exception {
    Webb webb = Webb.create();
    JSONObject payload = new JSONObject();
    payload.put("email", "inserttest@gmail.com");
    Request request = webb
            .get("http://localhost:8002/interested?email=testy@test.com");
    Response<JSONObject> response = request
            .asJsonObject();
    JSONObject result = response.getBody();
    JSONObject expected = new JSONObject();
    expected.put("message", "Interests");
    expected.put("status", 200);
    JSONArray matches = new JSONArray();
    matches.put("user1@email.com");
    matches.put("user2@email.com");
    matches.put("user3@email.com");
    expected.put("interests", matches);
    JSONAssert.assertEquals(expected, result, true);
    Assert.assertEquals(200, response.getStatusCode());
  }
}
