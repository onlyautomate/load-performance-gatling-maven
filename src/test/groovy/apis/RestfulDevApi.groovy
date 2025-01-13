package apis

import base.BaseApi
import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.http.HttpProtocolBuilder
import static io.gatling.javaapi.core.CoreDsl.*
import static io.gatling.javaapi.http.HttpDsl.status

class RestfulDevApi extends BaseApi {

    enum Endpoint {
        POSTAddObject,
        PUTUpdateObject,
        PATCHPartiallyUpdateObject,
        GETGetObjects,
        GETGetMultipleObjectsByIds,
        GETGetSingleObjectById,
        DELETERemoveObject
    }

    RestfulDevApi(Endpoint endpoint) {
        this.apiName = 'restful-dev-api'
        this.baseUrl = "https://api.restful-api.dev"
        this.reqContentType = ContentType.Json
        this.respContentType = ContentType.Json

        switch (endpoint) {
            case Endpoint.POSTAddObject:
                this.reqDesc = 'add new object'
                this.reqMethod = ReqMethod.POST
                this.reqPath = '/objects'
                break
            case Endpoint.PUTUpdateObject:
                this.reqDesc = 'update object'
                this.reqMethod = ReqMethod.PUT
                this.reqPath = '/objects/{id}'
                break
            case Endpoint.PATCHPartiallyUpdateObject:
                this.reqDesc = 'partially update object'
                this.reqMethod = ReqMethod.PATCH
                this.reqPath = '/objects/{id}'
                break
            case Endpoint.GETGetObjects:
                this.reqDesc = 'get all objects'
                this.reqMethod = ReqMethod.GET
                this.reqPath = '/objects'
                break
            case Endpoint.GETGetMultipleObjectsByIds:
                this.reqDesc = 'get multiple objects by ids'
                this.reqMethod = ReqMethod.GET
                this.reqPath = '/objects'
                break
            case Endpoint.GETGetSingleObjectById:
                this.reqDesc = 'get single object details using id'
                this.reqMethod = ReqMethod.GET
                this.reqPath = '/objects/{id}'
                break
            case Endpoint.DELETERemoveObject:
                this.reqDesc = 'delete object using id'
                this.reqMethod = ReqMethod.DELETE
                this.reqPath = '/objects/{id}'
                break
        }
    }

    static HttpProtocolBuilder buildHttpProtocol() {
        new RestfulDevApi(null).buildingHttpProtocol([:]) //provide api specific headers, if any
    }

    static ChainBuilder addObject(String saveObjIdAs, String saveObjNameAs) {
        exec(
                exec(session -> session.setAll([ computerName: "Comp-${new Date().time.toString()}".toString() ])),
                new RestfulDevApi(Endpoint.POSTAddObject).buildHttpRequest('data/payloads/addObject.json')
                        .check(status().is(200))
                        .check(jmesPath('name').ofString().is(session -> session.get('computerName')).saveAs(saveObjNameAs))
                        .check(jmesPath('id').notNull().saveAs(saveObjIdAs))
        )
    }

    static ChainBuilder getObject(String elKeyObjId, String elKeyObjName) {
        exec(
                new RestfulDevApi(Endpoint.GETGetSingleObjectById).buildHttpRequest([elKeyObjId])
                        .check(status().is(200))
                        .check(jmesPath('name').ofString().isEL(elKeyObjName))
        )
    }

    static ChainBuilder getMultipleObjects(Map queryMapObjIds, String elKeyObjNameList) {
        exec(
                new RestfulDevApi(Endpoint.GETGetMultipleObjectsByIds).buildHttpRequest(queryMapObjIds)
                        .check(status().is(200))
                        .check(
                                jmesPath('[].name').ofList().isEL(elKeyObjNameList)
                        )
        )
    }

    static ChainBuilder deleteObject(String elKeyObjId) {
        exec(
                new RestfulDevApi(Endpoint.DELETERemoveObject).buildHttpRequest([elKeyObjId])
                        .check(status().is(200))
                        .check(substring("Object with id = ${elKeyObjId} has been deleted."))
        )
    }
}