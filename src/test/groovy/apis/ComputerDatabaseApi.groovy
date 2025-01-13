package apis

import base.BaseApi
import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.http.HttpProtocolBuilder
import static io.gatling.javaapi.core.CoreDsl.*
import static io.gatling.javaapi.http.HttpDsl.status

class ComputerDatabaseApi extends BaseApi {

    enum Endpoint {
        LoadHomePage,
        LoadAddComputerPage,
        LoadComputerListPage,
        GETSearchComputers,
        GETGetComputerDetails,
        POSTAddCompuer,
        POSTEditComputer,
        POSTDeleteComputer
    }

    ComputerDatabaseApi(Endpoint endpoint) {
        this.apiName = 'computer-database'
        this.baseUrl = "https://computer-database.gatling.io"
        this.reqMethod = ReqMethod.GET

        switch (endpoint) {
            case Endpoint.LoadHomePage:
                this.reqDesc = 'home'
                this.reqPath = '/computers'
                break
            case Endpoint.LoadAddComputerPage:
                this.reqDesc = 'add computer page'
                this.reqPath = '/computers/new'
                break
            case Endpoint.LoadComputerListPage:
                this.reqDesc = 'browse to page'
                this.reqPath = '/computers'
                break
            case Endpoint.GETSearchComputers:
                this.reqDesc = 'search keyword'
                this.reqPath = '/computers'
                break
            case Endpoint.GETGetComputerDetails:
                this.reqDesc = 'details of id'
                this.reqPath = '/computers/{id}'
                break
            case Endpoint.POSTAddCompuer:
                this.reqMethod = ReqMethod.POST
                this.reqDesc = 'add new computer'
                this.reqPath = '/computers'
                break
            case Endpoint.POSTEditComputer:
                this.reqMethod = ReqMethod.POST
                this.reqDesc = 'edit existing computer'
                this.reqPath = '/computers/{id}'
                break
            case Endpoint.POSTDeleteComputer:
                this.reqMethod = ReqMethod.POST
                this.reqDesc = 'delete existing computer'
                this.reqPath = '/computers/{id}/delete'
                break
        }
    }

    //provide api specific headers here, if any
    static HttpProtocolBuilder buildHttpProtocol() {
        new ComputerDatabaseApi(null).buildingHttpProtocol([
                Accept: 'text/html,application/xhtml+xml,application/xml',
                'Accept-Language': 'en-US,en',
                'Accept-Encoding': 'gzip, deflate'
        ])
    }

    static ChainBuilder loadHomePage() {
        exec(
                new ComputerDatabaseApi(Endpoint.LoadHomePage).buildHttpRequest()
                    .check(status().is(200))
        )
    }

    static ChainBuilder loadAddComputerPage() {
        exec(
                new ComputerDatabaseApi(Endpoint.LoadAddComputerPage).buildHttpRequest()
                    .check(status().is(200))
        )
    }

    static ChainBuilder browsePages(int repeatTimes) {
        repeat(repeatTimes, 'pageIndx').on(
                exec(
                    new ComputerDatabaseApi(Endpoint.LoadComputerListPage).tap {
                        reqDesc = "${reqDesc} - #{pageIndx}" //EL expression
                    }.buildHttpRequest([p: '#{pageIndx}']) //EL expression
                        .check(status().is(200))
                        //validate method to check lower and upper page boundary
                        .check(css('.current').validate('check page boundaries', (textContent, session) -> {
                            def indx = session.get('pageIndx')
                            def expContains = "${indx*10 + 1} to ${(indx + 1)*10}"
                            if(!textContent.contains(expContains)) {
                                throw new IllegalArgumentException("page counter '${textContent}' should contain '${expContains}'")
                            }
                        }))
                ).pause(1)
        )
    }

    static ChainBuilder searchComputers(String saveComputerDetailsListAs) {
        feed(csv('data/lists/searchComputers.csv').random())
            .exec(
                new ComputerDatabaseApi(Endpoint.GETSearchComputers).tap {
                    reqDesc = "${reqDesc} - #{searchKeyword}"
                }.buildHttpRequest([f: '#{searchKeyword}'])
                    .check(status().is(200))
                    //xpath to do the text based verification
                    .check(xpath("count(//td/a[contains(@href, '/computers/')][not(contains(text(), #{searchKeyword}))])").is('0'))
                    .check(css("td a[href*='/computers/']", 'href').ofNode().findAll().transform { nodes ->
                        def list = nodes.collect {
                            [
                                compId: (it.getAttribute('href').split('/') as List).last,
                                compName: it.textContent
                            ]
                        }
                        list.shuffle()
                        list.take(3)
                    }.saveAs(saveComputerDetailsListAs)) //save the details of id and name of any 3 random computers from search results as a list
            ).exitHereIfFailed() //fail the scenario here
    }

    static ChainBuilder getComputerDetails(String elKeyComputerDetailsList) {
        foreach(elKeyComputerDetailsList, 'compDetails1').on(
            new ComputerDatabaseApi(Endpoint.GETGetComputerDetails).tap {
                reqDesc = "${reqDesc} - #{compDetails1.compId}"
            }.buildHttpRequest(['#{compDetails1.compId}'])
                .check(status().is(200))
                .check(css('#name', 'value').isEL('#{compDetails1.compName}'))
        )
    }

    static ChainBuilder searchAndGetComputer(String saveComputerNameAs, String saveComputerIdAs) {
        //random feed strategy
        feed(csv('data/lists/searchComputers.csv').random())
                .exec(
                        new ComputerDatabaseApi(Endpoint.GETSearchComputers).tap {
                            reqDesc = "${reqDesc} - #{searchKeyword}"
                        }.buildHttpRequest([f: '#{searchKeyword}'])
                            .check(status().is(200))
                            .check(css("td a[href*='/computers/']", 'href').ofNode().findRandom().transformWithSession { node, session ->
                                def compId = (node.getAttribute('href').split('/') as List).last
                                def compName = node.textContent
                                [
                                        id: compId,
                                        name: compName
                                ]
                            }.saveAs('compDetails')),
                ).exec(session -> {
                        def map = session.get('compDetails') as Map
                        session.set(saveComputerNameAs, map.name).set(saveComputerIdAs, map.id)
                    }
                ).exitHereIfFailed() //fail the scenario here
    }

    static ChainBuilder editComputer(String elKeyComputerName, String elKeyComputerId) {
        def updatedCompName = 'MyComp-' + new Date().time.toString()
        exec(
                new ComputerDatabaseApi(Endpoint.GETGetComputerDetails).tap {
                    reqDesc = "${reqDesc} - ${elKeyComputerId}"
                }.buildHttpRequest([elKeyComputerId])
                    .check(status().is(200))
                    .check(css('#name', 'value').isEL(elKeyComputerName)),
                new ComputerDatabaseApi(Endpoint.POSTEditComputer).tap {
                    reqDesc = "${reqDesc} - ${elKeyComputerId}"
                }.buildHttpRequest([elKeyComputerId])
                    .formParamMap([
                            name: updatedCompName,
                            introduced: '2024-04-04',
                            discontinued: '2024-11-11',
                            company: 36
                    ])
                        .check(status().is(200))
                        .check(substring("Computer ${updatedCompName} has been updated")) //given textContent is searched and matched through the DOM
        )
    }

    static ChainBuilder addComputer() {
        //circuler feed strategy
        feed(csv('data/lists/newComputers.csv').circular())
            .exec(
                    new ComputerDatabaseApi(Endpoint.POSTAddCompuer).tap {
                        reqDesc = "${reqDesc} - #{name}"
                    }.buildHttpRequest()
                        .formParamMap([
                                name: '#{name}',
                                introduced: '#{introduced}',
                                discontinued: '#{discontinued}',
                                company: '#{company}'
                        ])
                            .check(status().is(200))
                            .check(substring('Computer #{name} has been created'))
            )
    }

    static ChainBuilder deleteComputer(String elKeyComputerName, String elKeyComputerId) {
        exec(
                new ComputerDatabaseApi(Endpoint.POSTDeleteComputer).buildHttpRequest([elKeyComputerId])
                    .check(status().is(200))
                    .check(substring("Computer ${elKeyComputerName} has been deleted"))
        )
    }
}