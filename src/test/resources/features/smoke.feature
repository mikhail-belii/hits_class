Feature: Cucumber smoke test

  Scenario: Spring context loads
    Given the Spring context is loaded
    Then the application starts successfully
