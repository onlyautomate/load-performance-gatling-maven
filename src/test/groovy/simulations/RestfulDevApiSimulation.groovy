package simulations

import apis.RestfulDevApi
import base.BaseSimulation
import io.gatling.javaapi.http.HttpProtocolBuilder
import static io.gatling.javaapi.core.CoreDsl.*

@SuppressWarnings('unused')
class RestfulDevApiSimulation extends BaseSimulation {

    def chainBuilders = [
            RestfulDevApi.addObject('objId1', 'objName1'),
            RestfulDevApi.getObject('#{objId1}', '#{objName1}'),
            RestfulDevApi.addObject('objId2', 'objName2'),
            exec(session -> session.set('addedObjNames', [session.get('objName1'), session.get('objName2')])),
            RestfulDevApi.getMultipleObjects([id: '#{objId1},#{objId2}'], '#{addedObjNames}'),
            RestfulDevApi.deleteObject("#{objId1}"),
            RestfulDevApi.deleteObject("#{objId2}"),
            exec(session -> session.set('deletedObjNames', [])),
            RestfulDevApi.getMultipleObjects([id: '#{objId1},#{objId2}'], '#{deletedObjNames}')
    ]

    def scn = scenario("restful-dev api simulation to add, get, update, patch and delete objects")
                .exec(chainBuilders)

    List<HttpProtocolBuilder> protocols = [
            RestfulDevApi.buildHttpProtocol()
    ]

    RestfulDevApiSimulation() {
        setUp(scn.injectOpen(atOnceUsers(3))).protocols(protocols)
    }
}