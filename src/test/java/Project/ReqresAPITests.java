package Project;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class ReqresAPITests extends baseReqres {
    private @Getter @Setter String token;
    private static Logger logger = LogManager.getLogger(ReqresAPITests.class);


    public void login() {
        Response response = given().header("Content-Type", "application/json").
                body(json.get("Login").toString()).when().post(ENDPOINT_LOGIN);
        setToken(response.jsonPath().getString("token"));
    }

    @Test
    public void checkUserCreationWorks() {
        var response = given().header("Content-Type", "application/json").
                body(json.toJSONString()).when().post(ENDPOINT_REGISTER).then();
        response.assertThat().statusCode(200).body("token", notNullValue());
        logger.info("Registration was successful");

        response = given().header("Content-Type", "application/json").
                body(json.toJSONString()).when().post(ENDPOINT_LOGIN).then();
        response.assertThat().statusCode(200).body("token", Matchers.notNullValue());
        logger.info("Login was successful");
    }

    @Test
    public void checkUserCanGetUserDataAndUpdate() {
        Generators generator = new Generators();
        login();
        var response = given().auth().oauth2(token).get(ENDPOINT_USERS).then();
        response.assertThat().statusCode(200).body("total", Matchers.not(lessThan(2)));
        logger.info("User can see more than 1 account");

        var responseFirstAccount = given().auth().oauth2(token).get(ENDPOINT_USERS + "/1").then().
                contentType(ContentType.JSON).extract().response();
        json = new JsonWorker(responseFirstAccount.getBody().asString()).parseAndReturnJsonFromString();
        String toAdd = generator.generateRandomString();
        json.put("Edited", toAdd);

        response = given().auth().oauth2(getToken()).and().given().header("Content-Type", "application/json").
                body(json.toJSONString()).put(ENDPOINT_USERS).then();
        response.assertThat().statusCode(200).body("Edited", Matchers.equalTo(toAdd));
        logger.info("First user was updated successfully");
    }

    @Test
    public void checkUserCanDeleteAnotherUser() {
        login();
        var response = given().auth().oauth2(getToken()).get(ENDPOINT_USERS + "/1").then().
                contentType(ContentType.JSON).extract().response();
        String firstAccount = response.getBody().asString();

        var ResponseDeletion = given().auth().oauth2(getToken()).delete(ENDPOINT_USERS + "/1").then();
        ResponseDeletion.assertThat().statusCode(204);
        logger.info("Deletion command was successful");

        response = given().auth().oauth2(getToken()).get(ENDPOINT_USERS + "/1").then().
                contentType(ContentType.JSON).extract().response();
        String firstAccountAfterDelete = response.getBody().asString();
        Assert.assertEquals(firstAccount, firstAccountAfterDelete,
                "User should not be able to alter database");
        logger.info("Database was not actually altered");
    }

    @Test
    public void checkFailedUserCreationCannotLogin() {
        var response = given().header("Content-Type", "application/json").
                body(json.toJSONString()).when().post(ENDPOINT_REGISTER).then();
        response.assertThat().statusCode(400).body("error", Matchers.equalTo(
                "Note: Only defined users succeed registration"));
        logger.info("Note: Only defined users succeed registration was returned");

        response = given().header("Content-Type", "application/json").
                body(json.toJSONString()).when().post(ENDPOINT_LOGIN).then();
        response.assertThat().statusCode(400).body("error", Matchers.equalTo("user not found"));
        logger.info("user not found was returned");

    }


    private class Generators {
        private String generateRandomString() {
            String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
            StringBuilder salt = new StringBuilder();
            Random rnd = new Random();
            while (salt.length() < 18) {
                int index = (int) (rnd.nextFloat() * SALTCHARS.length());
                salt.append(SALTCHARS.charAt(index));
            }
            String saltStr = salt.toString();
            return saltStr;
        }
    }
}
