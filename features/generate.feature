Feature: generate a validated eiffel messages on message bus

  Scenario: generate a validated eiffel message
    Given Generate microservice is up and running
    When I send a messagebody containing all required fields for the correct messageType
    Then I receive back a validated eiffel message of the type I specified

  Scenario: fail on incorrect input
    Given Generate microservice is up and running
    When I send a messagebody containing some of the required fields for the correct messageType
    Then I receive back an error message specifying that I failed on validation
