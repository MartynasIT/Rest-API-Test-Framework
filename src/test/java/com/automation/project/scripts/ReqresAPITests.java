package com.automation.project.scripts;

import com.automation.project.base.BaseTest;
import com.framework.utils.Generators;
import com.framework.utils.JsonReader;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import lombok.Getter;
import lombok.Setter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class ReqresAPITests extends BaseTest {
    private
    @Getter @Setter String token;
    private static Logger logger = LogManager.getLogger(ReqresAPITests.class);
    Generators generators;


    public void login() {
        Response response = given().header("Content-Type", "application/json").
                body(jsonReader.getValue("Login")).when().post(ENDPOINT_LOGIN);
        setToken(response.jsonPath().getString("token"));
    }

    @Test
    public void checkUserCreationWorks() {
        String loginDetails = jsonReader.returnJsonObjectAsString();
        var response = given().header("Content-Type", "application/json").
                body(loginDetails).when().post(ENDPOINT_REGISTER).then();
        response.assertThat().statusCode(200).body("token", notNullValue());
        logger.info("Registration was successful");

        response = given().header("Content-Type", "application/json").
                body(loginDetails).when().post(ENDPOINT_LOGIN).then();
        response.assertThat().statusCode(200).body("token", Matchers.notNullValue());
        logger.info("Login was successful");
    }

    @Test
    public void checkUserCanGetUserDataAndUpdate() {
        generators = new Generators();
        login();
        var response = given().auth().oauth2(token).get(ENDPOINT_USERS).then();
        response.assertThat().statusCode(200).body("total", Matchers.not(lessThan(2)));
        logger.info("User can see more than 1 account");

        var responseFirstAccount = given().auth().oauth2(token).get(ENDPOINT_USERS + "/1").then().
                contentType(ContentType.JSON).extract().response();
        JSONObject json = new JsonReader(responseFirstAccount.getBody().asString()).getJsonObject();
        String toAdd = generators.generateRandomString();
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
        String loginDetails = jsonReader.returnJsonObjectAsString();
        var response = given().header("Content-Type", "application/json").
                body(loginDetails).when().post(ENDPOINT_REGISTER).then();
        response.assertThat().statusCode(400).body("error", Matchers.equalTo(
                "Note: Only defined users succeed registration"));
        logger.info("Note: Only defined users succeed registration was returned");

        response = given().header("Content-Type", "application/json").
                body(loginDetails).when().post(ENDPOINT_LOGIN).then();
        response.assertThat().statusCode(400).body("error", Matchers.equalTo("user not found"));
        logger.info("user not found was returned");

    }
}
