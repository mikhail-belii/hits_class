package com.example.hits.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SmokeSteps {

    @Autowired
    private ApplicationContext context;

    @Given("the Spring context is loaded")
    public void theSpringContextIsLoaded() {
    }

    @Then("the application starts successfully")
    public void theApplicationStartsSuccessfully() {
        assertNotNull(context);
    }
}
