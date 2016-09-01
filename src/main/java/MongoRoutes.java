/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.goebl.david.Webb;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 *
 * @author benhernandez
 */
public class MongoRoutes {
  public static final String DATABASE_NAME = "potentialMatches";
  public static final String TABLE_NAME = "potentialMatches";
  
  public final Route getPotentialMatches = new Route() {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      MongoClient mc = getMongoClient();
      MongoDatabase md = mc.getDatabase(DATABASE_NAME);
      MongoCollection<Document> potentialMatches
          = md.getCollection(TABLE_NAME);
      JSONObject returner = new JSONObject();
      Document matcher = new Document("email", request.queryParams("email"));
      Document profile = potentialMatches.find(matcher).first();
      if (profile != null) {
        returnReady(200, "User found", returner, response);
        returner.put("match suggestions", profile.get("matchSuggestions"));
      } else {
        returnReady(400, request.queryParams("email") + " does not exist",
            returner, response);
      }
      return returner;
    }
  };
  public final Route createOrUpdate = new Route() {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      JSONObject data = new JSONObject(request.body());
      MongoClient mc = getMongoClient();
      MongoDatabase md = mc.getDatabase(DATABASE_NAME);
      MongoCollection<Document> potentialMatches
          = md.getCollection(TABLE_NAME);
      JSONObject returner = new JSONObject();
      Document matcher = new Document("email", data.getString("email"));
      if (potentialMatches.find(matcher).first() == null) {
        data.put("matchSuggestions", new JSONArray());
        Document temp = convertJSONtoDocument(data);
        potentialMatches.insertOne(temp);
        if (notifyDaemon(data.getString("email"), true)) {
          returnReady(201, "Match profile created for "
              + data.getString("email"), returner, response);
        } else {
          returnReady(500, "Internal Daemon Error", returner, response);
        }
      } else {
        potentialMatches.updateOne(matcher,
            new Document("$set", convertJSONtoDocument(data)));
        if (notifyDaemon(data.getString("email"), true)) {
          returnReady(200, "Match profile updated for "
              + data.getString("email"), returner, response);
        } else {
          returnReady(500, "Internal Daemon Error", returner, response);
        }
      }
      mc.close();
      return returner;
    }
  };
  public final Route getMatchProfile = new Route() {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      MongoClient mc = getMongoClient();
      MongoDatabase md = mc.getDatabase(DATABASE_NAME);
      MongoCollection<Document> potentialMatches
          = md.getCollection(TABLE_NAME);
      JSONObject returner = new JSONObject();
      Document matcher = new Document("email", request.queryParams("email"));
      Document profile = potentialMatches.find(matcher).first();
      if (profile != null) {
        returnReady(200, "Match Profile",  returner, response);
        returner.put("profile", profile.toJson());
      } else {
        returnReady(400, "Email not found", returner, response);
      }
      mc.close();
      return returner;
    }
  };
  public final Route deactivate = new Route() {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      JSONObject data = new JSONObject(request.body());
      MongoClient mc = getMongoClient();
      MongoDatabase md = mc.getDatabase(DATABASE_NAME);
      MongoCollection<Document> potentialMatches
          = md.getCollection(TABLE_NAME);
      JSONObject returner = new JSONObject();
      Document matcher = new Document("email", data.getString("email"));
      Document active = new Document("$set", new Document("active", false));
      try {
        Document profile = potentialMatches.findOneAndUpdate(matcher, active);
        returnReady(200, data.getString("email") + " deactivated",
            returner, response);
        notifyDaemon(data.getString("email"), false);
      } catch (Exception e) {
        System.out.println("Error: " + e);
      }
      mc.close();
      return returner;
    }
  };
  public final Route activate = new Route() {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      JSONObject data = new JSONObject(request.body());
      MongoClient mc = getMongoClient();
      MongoDatabase md = mc.getDatabase(DATABASE_NAME);
      MongoCollection<Document> potentialMatches
          = md.getCollection(TABLE_NAME);
      JSONObject returner = new JSONObject();
      Document matcher = new Document("email", data.getString("email"));
      Document active = new Document("$set", new Document("active", true));
      try {
        Document profile = potentialMatches.findOneAndUpdate(matcher, active);
        returnReady(200, data.getString("email") + " activated",
            returner, response);
        notifyDaemon(data.getString("email"), false);
      } catch (Exception e) {
        System.out.println("Error: " + e);
      }
      mc.close();
      return returner;
    }
  };

  private final MongoClient getMongoClient() {
    String mongoUri = System.getenv("MONGODB_URI");
    return new MongoClient(mongoUri);
  }
  
  private Document convertJSONtoDocument(final JSONObject obj) {
    Document returner = new Document();
    obj.keySet().forEach(key -> {
      returner.append(key, obj.get(key));
    });
    return returner;
  }
  
  private void returnReady(final int status, final String message,
      final JSONObject obj, final Response response) {
    obj.put("status", status);
    obj.put("message", message);
    response.status(status);
  }
  
  private boolean notifyDaemon(final String email, final boolean updated)
  {
    String daemon = System.getenv("DAEMON_IP");
    if (daemon == null) {
      daemon = "localhost:8003";
    }
    Webb webb = Webb.create();
    com.goebl.david.Request request = webb.post("http://" + daemon + "/notify")
        .param("email", email)
        .param("update", updated);
    com.goebl.david.Response<Void> response = request.asVoid();
    return response.getStatusCode() == 200;
  }
}
