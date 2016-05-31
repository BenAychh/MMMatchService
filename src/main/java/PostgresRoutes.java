import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author benhernandez
 */
public class PostgresRoutes {
  private ComboPooledDataSource cpds;

  public PostgresRoutes() {
    String host = System.getenv("PG_PORT_5432_TCP_ADDR");
    String port = System.getenv("PG_PORT_5432_TCP_PORT");
    if (host == null) {
      host = "localhost";
    }
    if (port == null) {
      port = "5432";
    }
    cpds = new ComboPooledDataSource();
    cpds.setJdbcUrl("jdbc:postgresql://"
        + host + ":" + port + "/Interested?user=postgres");
  }
  public final Route getMatches = new Route() {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      Connection connection = cpds.getConnection();
      String query = "select interested_in from interested where email=?"
          + " AND interested_in IN"
          + " (select email from interested WHERE interested_in = ?)";
      PreparedStatement ps = connection.prepareStatement(query);
      ps.setString(1, request.queryParams("email"));
      ps.setString(2, request.queryParams("email"));
      ResultSet rs = ps.executeQuery();
      JSONArray matches = new JSONArray();
      JSONObject returner = new JSONObject();
      while (rs.next()) {
        matches.put(rs.getString("interested_in"));
      }
      returnReady(200, "Matches", returner, response);
      returner.put("matches", matches);
      rs.close();
      ps.close();
      connection.close();
      return returner;
    }
  };
  
  public final Route getInterested = new Route() {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      Connection connection = cpds.getConnection();
      String query = "select interested_in from interested where email=?";
      PreparedStatement ps = connection.prepareStatement(query);
      ps.setString(1, request.queryParams("email"));
      ResultSet rs = ps.executeQuery();
      JSONArray interested = new JSONArray();
      JSONObject returner = new JSONObject();
      while (rs.next()) {
        interested.put(rs.getString("interested_in"));
      }
      returnReady(200, "Interests", returner, response);
      returner.put("interests", interested);
      rs.close();
      ps.close();
      connection.close();
      return returner;
    }
  };
  
  public final Route putInterest = new Route() {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      JSONObject params = new JSONObject(request.body());
      Connection connection = cpds.getConnection();
      String query = "select id from interested where email=? AND "
          + "interested_in=?";
      PreparedStatement ps = connection.prepareStatement(query);
      ps.setString(1, params.getString("email"));
      ps.setString(2, params.getString("interestedIn"));
      ResultSet rs = ps.executeQuery();
      JSONObject returner = new JSONObject();
      if (!rs.next()) {
        query = "insert into interested (email, interested_in) "
            + "values (?, ?)";
        ps = connection.prepareStatement(query);
        ps.setString(1, params.getString("email"));
        ps.setString(2, params.getString("interestedIn"));
        ps.execute();
        query = "select * from interested where email= ? AND interested_in = ?";
        ps = connection.prepareStatement(query);
        ps.setString(1, params.getString("interestedIn"));
        ps.setString(2, params.getString("email"));
        rs = ps.executeQuery();
        if (rs.next()) {
          returnReady(200, params.getString("interestedIn")
              +" is interested in you, you are a match!", returner, response);
        } else {
          returnReady(200, "You have shown interest in "
              + params.getString("interestedIn"), returner, response);
        }
      } else {
        returnReady(400, "Already interested in "
            + params.getString("interestedIn"), returner, response);
      }
      rs.close();
      ps.close();
      connection.close();
      return returner;
    }
  };
  
  public final Route removeInterest = new Route() {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      JSONObject params = new JSONObject(request.body());
      Connection connection = cpds.getConnection();
      String query = "select id from interested where email=? AND "
          + "interested_in=?";
      PreparedStatement ps = connection.prepareStatement(query);
      ps.setString(1, params.getString("uninterestedIn"));
      ps.setString(2, params.getString("email"));
      ResultSet rs = ps.executeQuery();
      boolean match = false;
      if (rs.next()) {
        match = true;
      }
      query = "select id from interested where email=? AND "
          + "interested_in=?";
      ps = connection.prepareStatement(query);
      ps.setString(1, params.getString("email"));
      ps.setString(2, params.getString("uninterestedIn"));
      rs = ps.executeQuery();
      JSONObject returner = new JSONObject();
      if (rs.next()) {
        query = "delete from interested WHERE id=?";
        ps = connection.prepareStatement(query);
        ps.setInt(1, rs.getInt("id"));
        ps.execute();
        if (!match) {
          returnReady(200, "You have removed your interest in "
              + params.getString("uninterestedIn"), returner, response);
        } else {
          returnReady(200, "You are no longer a match with "
              + params.getString("uninterestedIn"), returner, response);
        }
      } else {
        returnReady(400, "No interest in " + params.getString("uninterestedIn")
            + " on record", returner, response);
      }
      rs.close();
      ps.close();
      connection.close();
      return returner;
    }
  };
  public final Route isMatch = new Route() {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      Connection connection = cpds.getConnection();
      String query = "select interested_in from interested where email=? " +
          "AND interested_in = ? AND interested_in IN" +
          "(select email from interested WHERE interested_in = ?)";
      PreparedStatement ps = connection.prepareStatement(query);
      ps.setString(1, request.queryParams("email"));
      ps.setString(2, request.queryParams("match"));
      ps.setString(3, request.queryParams("email"));
      ResultSet rs = ps.executeQuery();
      JSONObject returner = new JSONObject();
      if (rs.next()) {
        returner.put("match", true);
      } else {
        returner.put("match", false);
      }
      rs.close();
      ps.close();
      connection.close();
      return returner;
    }
  };
  
  private void returnReady(final int status, final String message,
      final JSONObject obj, final Response response) {
    obj.put("status", status);
    obj.put("message", message);
    response.status(status);
  }
}
