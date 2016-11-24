Feature: subscribe to messagebus
Background:
  Given a rabbit mq server is started and correct exchange is initiated

  Scenario: receive message
    Given Subscribe microservice is up and running
    When I subscribe to service by connecting to "stream" endpoint with bindingKey "test"
    And I send a message on rabbitMQ using the correct exchange with a routingKey "test"
    Then I receive sent message on service return stream
