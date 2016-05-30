import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._


class BasicSimulation extends Simulation {
  // The ip of the server in which the application is deployed
  //val serverIp = "142.133.110.175"
  val serverIp = "localhost"

  val httpConf = http
    .baseURL("http://" + serverIp + ":8080")
    .acceptHeader("application/json;charset=UTF-8")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  //val url = "/remrem-publish-0.1.0/producer/msg?rk=3"
  val url = "/producer/msg?rk=3"
  var msg = """{
                "meta": {
                  "type": "EiffelActivityStartedEvent",
                  "version": "1.0",
                  "time": 1234567890,
                  "domainId": "example.domain",
                  "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee0"
                },
                "data": {
                  "executionUri": "https://my.jenkins.host/myJob/43",
                  "liveLogs": [
                    {
                     "name": "My build log",
                     "uri": "file:///tmp/logs/data.log"
                    }
                  ]
                },
                "links": {
                  "activityExecution": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1",
                  "previousActivityExecution": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee2"
                }
              }"""
  val body = StringBody("[" + msg + "]")

  val scn = scenario("BasicSimulation")
    .exec(http("/producer")
      .post(url).body(body)
      .header("Content-Type", "application/json")
      .check(status.is(200))
    )

  setUp(
    scn.inject(
      //atOnceUsers(1)
      //rampUsersPerSec(10) to(500) during(3 seconds),
      constantUsersPerSec(100) during(60 seconds) randomized
    )
  ).protocols(httpConf)
}
