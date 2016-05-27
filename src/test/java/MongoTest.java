/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.goebl.david.Request;
import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 *
 * @author benhernandez
 */
public class MongoTest {  
  @Before
  public void beforeEach() {
    MongoClient mc = getMongoClient();
    MongoDatabase md= mc.getDatabase(MongoRoutes.DATABASE_NAME);
    MongoCollection<Document> potentialMatches
          = md.getCollection(MongoRoutes.TABLE_NAME);
    potentialMatches.drop();
    // Teacher in Mongo.
    Document teacher = new Document();
    teacher.append("email", "testy@test.com");
    BasicDBObject match1 = new BasicDBObject();
    match1.put("email", "match1@match.com");
    match1.put("perc", 90);
    BasicDBObject match2 = new BasicDBObject();
    match2.put("email", "match2@match.com");
    match2.put("perc", 97);
    List<BasicDBObject> matchSuggestions = new ArrayList<>();
    matchSuggestions.add(match1);
    matchSuggestions.add(match2);
    teacher.append("matchSuggestions", matchSuggestions);
    potentialMatches.insertOne(teacher);
    mc.close();
  }
  
  public void testGetPotentialMatches() throws Exception {
    Webb webb = Webb.create();
    JSONObject payload = new JSONObject();
    payload.put("email", "inserttest@gmail.com");
    Request request = webb
            .get("http://localhost:8002/suggestions?email=testy@test.com");
    Response<JSONObject> response = request
            .asJsonObject();
    JSONObject result = response.getBody();
    System.out.println(result);
    JSONObject expected = new JSONObject();
    expected.put("message", "User found");
    expected.put("status", 200);
    JSONArray suggestedMatches = new JSONArray();
    suggestedMatches.put(new JSONObject()
        .append("email", "match1@match.com")
        .append("perc", 90));
    suggestedMatches.put(new JSONObject()
        .append("email", "match2@match.com")
        .append("perc", 97));
    expected.put("match suggestions", suggestedMatches);
    System.out.println(expected);
    JSONAssert.assertEquals(expected, result, JSONCompareMode.STRICT);
  }
  
  @Test
  public void testInsert() throws Exception {
    Webb webb = Webb.create();
    JSONObject payload = new JSONObject();
    payload.put("email", "inserttest@gmail.com");
    Request request = webb
            .post("http://localhost:8002/upsert")
            .body(payload);
    Response<JSONObject> response = request
            .asJsonObject();
    JSONObject result = response.getBody();
    JSONObject expected = new JSONObject();
    expected.put("message", "inserttest@gmail.com added, starting match "
        + "calculations");
    expected.put("status", 201);
    JSONAssert.assertEquals(expected, result, true);
    Assert.assertEquals(201, response.getStatusCode());
    MongoClient mc = getMongoClient();
    MongoDatabase md= mc.getDatabase(MongoRoutes.DATABASE_NAME);
    MongoCollection<Document> potentialMatches
          = md.getCollection(MongoRoutes.TABLE_NAME);
    Document matcher = new Document("email", "inserttest@gmail.com");
    if (potentialMatches.find(matcher).first() == null) {
      Assert.fail("Profile not added");
    }
  }
  
  @Test
  public void testUpdate() throws Exception {
    Webb webb = Webb.create();
    JSONObject payload = new JSONObject();
    payload.put("email", "testy@test.com");
    payload.put("this", "is an insert test");
    Request request = webb
            .post("http://localhost:8002/upsert")
            .body(payload);
    Response<JSONObject> response = request
            .asJsonObject();
    JSONObject result = response.getBody();
    JSONObject expected = new JSONObject();
    expected.put("message", "testy@test.com updated, starting match "
        + "calculations");
    expected.put("status", 200);
    JSONAssert.assertEquals(expected, result, true);
    Assert.assertEquals(200, response.getStatusCode());
    MongoClient mc = getMongoClient();
    MongoDatabase md= mc.getDatabase(MongoRoutes.DATABASE_NAME);
    MongoCollection<Document> potentialMatches
          = md.getCollection(MongoRoutes.TABLE_NAME);
    Document matcher = new Document("email", "testy@test.com");
    Document results = potentialMatches.find(matcher).first();
    Assert.assertEquals(results.getString("this"), "is an insert test");
  }
  
  @Test
  public void testDeactivate() throws Exception {
    Webb webb = Webb.create();
    JSONObject payload = new JSONObject();
    payload.put("email", "testy@test.com");
    Request request = webb
            .put("http://localhost:8002/deactivate")
            .body(payload);
    Response<JSONObject> response = request
            .asJsonObject();
    JSONObject result = response.getBody();
    JSONObject expected = new JSONObject();
    expected.put("message", "testy@test.com deactivated");
    expected.put("status", 200);
    JSONAssert.assertEquals(expected, result, JSONCompareMode.STRICT);
    Assert.assertEquals(200, response.getStatusCode());
    MongoClient mc = getMongoClient();
    MongoDatabase md = mc.getDatabase(MongoRoutes.DATABASE_NAME);
    MongoCollection<Document> potentialMatches
        = md.getCollection(MongoRoutes.TABLE_NAME);
    Document matcher = new Document("email", "testy@test.com");
    Document results = potentialMatches.find(matcher).first();
    Assert.assertEquals(false, results.getBoolean("active"));
  }
  
  @Test
  public void testActivate() throws Exception {
    Webb webb = Webb.create();
    JSONObject payload = new JSONObject();
    payload.put("email", "testy@test.com");
    Request request = webb
            .put("http://localhost:8002/activate")
            .body(payload);
    Response<JSONObject> response = request
            .asJsonObject();
    JSONObject result = response.getBody();
    JSONObject expected = new JSONObject();
    expected.put("message", "testy@test.com activated");
    expected.put("status", 200);
    JSONAssert.assertEquals(expected, result, JSONCompareMode.STRICT);
    Assert.assertEquals(200, response.getStatusCode());
    MongoClient mc = getMongoClient();
    MongoDatabase md = mc.getDatabase(MongoRoutes.DATABASE_NAME);
    MongoCollection<Document> potentialMatches
        = md.getCollection(MongoRoutes.TABLE_NAME);
    Document matcher = new Document("email", "testy@test.com");
    Document results = potentialMatches.find(matcher).first();
    Assert.assertEquals(results.getBoolean("active"), true);
  }
  
  
  private MongoClient getMongoClient() {
    final String DEFAULT_MONGO_PORT = "27017";
    String host = System.getenv("MD_PORT_27017_TCP_ADDR");
    if (host == null) {
      host = "localhost";
    }
    String port = System.getenv("MD_PORT_27017_TCP_PORT");
    if (port == null) {
      port = DEFAULT_MONGO_PORT;
    }
    return new MongoClient(host, Integer.parseInt(port));
  }
}
