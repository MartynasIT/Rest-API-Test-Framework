package Project;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;

import java.io.FileReader;
import java.io.IOException;


public class baseReqres {
    protected static final String ENDPOINT_LOGIN = "https://reqres.in/api/login";
    protected static final String ENDPOINT_REGISTER = "https://reqres.in/api/register";
    protected static final String ENDPOINT_USERS = "https://reqres.in/api/users";
    protected JSONObject json;

    @BeforeTest
    @Parameters("testDataPath")
    protected void initData(String testDataPath) {
        if (testDataPath != null)
            json = new JsonWorker(testDataPath).parseAndReturnJsonFromFile();
    }

    protected class JsonWorker {
        String path = null;

        protected JsonWorker(String path) {
            this.path = path;
        }

        protected JSONObject parseAndReturnJsonFromFile() {
            try {
                JSONParser parser = new JSONParser();
                return (JSONObject) parser.parse(new FileReader(path));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected JSONObject parseAndReturnJsonFromString() {
            try {
                JSONParser parser = new JSONParser();
                return (JSONObject) parser.parse(path);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}


