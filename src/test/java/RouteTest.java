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
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 *
 * @author benhernandez
 */
public class MongoRouteTest {
  @BeforeClass
  public static void beforeAll() throws Exception {
    final String DEFAULT_MONGO_PORT = "27017";
    String host = System.getenv("MD_PORT_27017_TCP_ADDR");
    if (host == null) {
      host = "localhost";
    }
    String port = System.getenv("MD_PORT_27017_TCP_PORT");
    if (port == null) {
      port = DEFAULT_MONGO_PORT;
    }
    String[] args = {};
    Thread.sleep(2000);
    MMMatchService.main(args);
  }
  
  @Before
  public void beforeEach() {
    final String DEFAULT_MONGO_PORT = "27017";
    String host = System.getenv("MD_PORT_27017_TCP_ADDR");
    if (host == null) {
      host = "localhost";
    }
    String port = System.getenv("MD_PORT_27017_TCP_PORT");
    if (port == null) {
      port = DEFAULT_MONGO_PORT;
    }
    MongoClient mc = new MongoClient(host, Integer.parseInt(port));
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
  
  @Test
  public void test404Get() throws Exception {
      Webb webb = Webb.create();
      Request request = webb
              .get("http://localhost:8002/doesnotexist");
      Response<JSONObject> response = request
              .asJsonObject();
      JSONObject result = new JSONObject(response.getErrorBody().toString());
      JSONObject expected = new JSONObject();
      expected.put("message", "error");
      expected.put("status", 404);
      expected.put("requested resource", "/doesnotexist");
      expected.put("requested method", "GET");
      JSONAssert.assertEquals(expected, result, true);
  }

  @Test
  public void test404Post() throws Exception {
      Webb webb = Webb.create();
      Request request = webb
              .post("http://localhost:8002/doesnotexist");
      Response<JSONObject> response = request
              .asJsonObject();
      JSONObject result = new JSONObject(response.getErrorBody().toString());
      JSONObject expected = new JSONObject();
      expected.put("message", "error");
      expected.put("status", 404);
      expected.put("requested resource", "/doesnotexist");
      expected.put("requested method", "POST");
      JSONAssert.assertEquals(expected, result, true);
  }

  @Test
  public void test404Put() throws Exception {
      Webb webb = Webb.create();
      Request request = webb
              .put("http://localhost:8002/doesnotexist");
      Response<JSONObject> response = request
              .asJsonObject();
      JSONObject result = new JSONObject(response.getErrorBody().toString());
      JSONObject expected = new JSONObject();
      expected.put("message", "error");
      expected.put("status", 404);
      expected.put("requested resource", "/doesnotexist");
      expected.put("requested method", "PUT");
      JSONAssert.assertEquals(expected, result, true);
  }

  @Test
  public void test404Delete() throws Exception {
      Webb webb = Webb.create();
      Request request = webb
              .delete("http://localhost:8002/doesnotexist");
      Response<JSONObject> response = request
              .asJsonObject();
      JSONObject result = new JSONObject(response.getErrorBody().toString());
      JSONObject expected = new JSONObject();
      expected.put("message", "error");
      expected.put("status", 404);
      expected.put("requested resource", "/doesnotexist");
      expected.put("requested method", "DELETE");
      JSONAssert.assertEquals(expected, result, true);
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
  }
}
