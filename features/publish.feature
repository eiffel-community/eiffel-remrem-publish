Feature: publish eiffel messages on message bus
  Background:
    Given a rabbit mq server is started and correct exchange is initiated
    And I have a queue intercepting all received messages on the same exchange

  Scenario: publish eiffel message
    Given that I send an eiffel message using routingKey "test"
    When I query the queue for received messages
    Then I see sent message in the queue delivered with the routingKey "test"

  Scenario: publish batch eiffel messaged
    Given that I send ten eiffel messages using routingKey "test"
    When I query the queue for received messages
    Then I see all ten sent messages in the queue delivered with the routingKey "test"
