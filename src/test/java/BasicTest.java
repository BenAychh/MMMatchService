
import com.goebl.david.Request;
import com.goebl.david.Response;
import com.goebl.david.Webb;
import org.json.JSONObject;
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
public class BasicTest {
  
  @BeforeClass
  public static void beforeAll() throws Exception {
    String[] args = {};
    Thread.sleep(2000);
    MMMatchService.main(args);
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
  
}
