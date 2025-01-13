package simulations

import apis.ComputerDatabaseApi
import static io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.*
import io.gatling.javaapi.http.*

class ComputerDatabaseSimulation extends Simulation {

    //list of chain builders for a regular non-admin user
    def nonAdminUserChainBuilders = [
            ComputerDatabaseApi.loadHomePage(),
            ComputerDatabaseApi.browsePages(3),
            ComputerDatabaseApi.searchComputers('compList'),
            ComputerDatabaseApi.getComputerDetails('#{compList}')
    ]

    //list of chain builders for an admin user
    def adminUserChainBuilders = [
            ComputerDatabaseApi.loadAddComputerPage(),
            ComputerDatabaseApi.addComputer(),
            ComputerDatabaseApi.searchAndGetComputer('compName', 'compId'),
            ComputerDatabaseApi.editComputer('#{compName}', '#{compId}'),
            ComputerDatabaseApi.deleteComputer('#{compName}', '#{compId}')
    ]

    def nonAdminUserScenario = scenario('browse, search and get computer details').exec(nonAdminUserChainBuilders)

    //grouping requests
    def adminUserScenario = scenario('add, get, edit and delete computer').group('admin-only').on(exec(adminUserChainBuilders))

    //list of protocols
    List<HttpProtocolBuilder> protocols = [
            ComputerDatabaseApi.buildHttpProtocol()
    ]

    //simulation setup
    ComputerDatabaseSimulation() {
        setUp(
                //open injection, variable rate of arrival of virtual users
                nonAdminUserScenario.injectOpen(
                        nothingFor(2),
                        atOnceUsers(1),
                        rampUsers(2).during(4),
                        constantUsersPerSec(2).during(8),
                        constantUsersPerSec(2).during(5).randomized(),
                        rampUsersPerSec(2).to(3).during(5),
                        stressPeakUsers(4).during(10)
                ),
                //closed onjection, fixed number of concurrent virtual users at a time
                adminUserScenario.injectClosed(
                        constantConcurrentUsers(1).during(1),
                        rampConcurrentUsers(1).to(2).during(6)
                )
        )
                .protocols(protocols) //add supported promotocols
                .assertions(global().responseTime().max().lt(800)) //no requests should exceed max response time of 800ms
                .assertions(forAll().failedRequests().percent().gt(5.0)) //no request should have failure rate of more than 5%
                .assertions(details('admin-only').requestsPerSec().between(1.0, 10.0)) //applicable to requests from the said group only
                .assertions(details('admin-only', 'POST /computers/{id}/delete, delete existing computer').failedRequests().percent().is(0.0)) //no delete request should fail
    }
}