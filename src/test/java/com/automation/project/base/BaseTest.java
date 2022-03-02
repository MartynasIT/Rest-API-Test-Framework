package com.automation.project.base;

import com.framework.utils.JsonReader;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

public class BaseTest {
    protected static final String ENDPOINT_LOGIN = "https://reqres.in/api/login";
    protected static final String ENDPOINT_REGISTER = "https://reqres.in/api/register";
    protected static final String ENDPOINT_USERS = "https://reqres.in/api/users";
    protected JsonReader jsonReader;

    @BeforeTest
    @Parameters("testDataPath")
    protected void initData(@Optional String testDataPath) {
        if (testDataPath != null)
            jsonReader = new JsonReader(testDataPath);
    }
}


