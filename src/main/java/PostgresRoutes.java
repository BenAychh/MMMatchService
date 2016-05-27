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
        returnReady(200, "Added interest in "
            + params.getString("interestedIn"), returner, response);
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
      ps.setString(1, params.getString("email"));
      ps.setString(2, params.getString("interestedIn"));
      ResultSet rs = ps.executeQuery();
      JSONObject returner = new JSONObject();
      if (rs.next()) {
        query = "delete from interested WHERE id=?";
        ps = connection.prepareStatement(query);
        ps.setInt(1, rs.getInt("id"));
        ps.execute();
        returnReady(200, "Removed interest in "
            + params.getString("interestedIn"), returner, response);
      } else {
        returnReady(400, "No interest in " + params.getString("interestedIn")
            + " on record", returner, response);
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
