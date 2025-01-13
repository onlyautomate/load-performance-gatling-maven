package base

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.Session
import io.gatling.javaapi.core.Simulation
import static io.gatling.javaapi.core.CoreDsl.exec

abstract class BaseSimulation extends Simulation {

    @Override
    void before() {
        super.before()
    }

    @Override
    void after() {
        super.after()
    }

    static ChainBuilder printCurrentSessionDetails(List<String> sessionKeys) {
        exec { Session session ->
            def sb = new StringBuilder()
            sb.append(System.lineSeparator())
            sb.append("Details of the session:").append(System.lineSeparator())
            sessionKeys.each { String key ->
                sb.append("${key}: ${session.get(key)}").append(System.lineSeparator())
            }
            println(sb)
            session
        }
    }
}