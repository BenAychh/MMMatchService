/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.logging.Level;
import org.json.JSONObject;
import java.util.logging.Logger;
import spark.Request;
import spark.Response;
import spark.Route;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;

/**
 *
 * @author benhernandez
 */
public class MMMatchService {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    Logger mongoLogger = Logger.getLogger( "org.mongodb" );
    mongoLogger.setLevel(Level.SEVERE); // e.g. or Log.WARNING, etc.
    port(8002);
    MongoRoutes mongoRoutes = new MongoRoutes();
    put("/upsert", mongoRoutes.createOrUpdate);
    get("/matchprofile", mongoRoutes.getMatchProfile);
    get("/suggestions", mongoRoutes.getPotentialMatches);
    put("/deactivate", mongoRoutes.deactivate);
    put("/activate", mongoRoutes.activate);
    
    PostgresRoutes postgresRoutes = new PostgresRoutes();
    get("/matches", postgresRoutes.getMatches);
    get("/interested", postgresRoutes.getInterested);
    get("/relationship", postgresRoutes.getRelationship);
    get("/isinterest", postgresRoutes.isInterest);
    put("/addinterest", postgresRoutes.putInterest);
    put("/removeinterest", postgresRoutes.removeInterest);
    get("*", error);
    post("*", error);
    put("*", error);
    delete("*", error);
  }
  
  private static Route error = new Route() {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        JSONObject res = new JSONObject();
        res.put("message", "error");
        res.put("status", 404);
        res.put("requested resource", request.pathInfo());
        res.put("requested method", request.requestMethod());
        response.status(404);
        return res.toString();
    }
  };
}
