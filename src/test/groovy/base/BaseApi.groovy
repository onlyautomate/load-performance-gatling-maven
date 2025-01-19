package base

import io.gatling.javaapi.http.HttpProtocolBuilder
import io.gatling.javaapi.http.HttpRequestActionBuilder
import static io.gatling.javaapi.core.CoreDsl.ElFileBody
import static io.gatling.javaapi.http.HttpDsl.http

abstract class BaseApi {

    static Map<String, String> commonHeaders = [:]
    String apiName
    String baseUrl
    String reqPath
    ReqMethod reqMethod
    String reqDesc
    Map<String, String> reqHeaders = [:]
    ContentType reqContentType
    ContentType respContentType

    static enum ReqMethod {
        POST,
        GET,
        PUT,
        DELETE,
        PATCH
    }

    static enum ContentType {
        Json("application/json"),
        Xml("application/xhtml+xml"),
        FormURLEncoded("application/x-www-form-urlencoded"),
        MultiPartForm("multipart/form-data")

        public final String value

        ContentType(String value) {
            this.value = value
        }
    }

    private Map<String, String> gatherRequestHeaders() {
        def map = new HashMap<String, String>()
        if(this.reqHeaders) {
            map.putAll(this.reqHeaders)
        }
        if(this.reqContentType) {
            map.put('Content-Type', this.reqContentType.value)
        }
        if(this.respContentType) {
            map.put('Accept', this.respContentType.value)
        }
        map
    }

    //different ways in which request configuration can be provided to create HttpRequestActionBuilder
    HttpRequestActionBuilder buildHttpRequest(){
        buildHttpRequest(null, null, null)
    }

    HttpRequestActionBuilder buildHttpRequest(List<String> pathPrams){
        buildHttpRequest(pathPrams, null, null)
    }

    HttpRequestActionBuilder buildHttpRequest(Map<String, String> queryParams){
        buildHttpRequest(null, queryParams, null)
    }

    @SuppressWarnings('unused')
    HttpRequestActionBuilder buildHttpRequest(List<String> pathPrams, Map<String, String> queryParams) {
        buildHttpRequest(pathPrams, queryParams, null)
    }

    HttpRequestActionBuilder buildHttpRequest(String requestBodyTemplatePath) {
        buildHttpRequest(null, null, requestBodyTemplatePath)
    }

    //reusable method to create HttpRequestActionBuilder for any api endpoint
    HttpRequestActionBuilder buildHttpRequest(List<String> pathPrams, Map<String, String> queryParams, String requestBodyTemplatePath) {
        def refinedPath = this.reqPath
        if(pathPrams) {
            def firstSet = refinedPath.findIndexValues { it.contains('{') }
            def secondSet = refinedPath.findIndexValues { it.contains('}') }
            if(firstSet.size() != secondSet.size() || firstSet.size() != pathPrams.size()) {
                throw new Exception("path parameters incorrectly specified for the endpoint '${this.reqDesc}', cannot proceed")
            }
            pathPrams.eachWithIndex { String param, int indx ->
                def toBeReplaced = this.reqPath.substring(firstSet[indx] as int, secondSet[indx] as int + 1).replaceAll("\\{", "\\\\{")
                refinedPath = refinedPath.replaceAll(toBeReplaced, param)
            }
        }

        def httpDetails = "${this.reqMethod.name()} ${this.reqPath}, ${this.reqDesc}".toString()
        HttpRequestActionBuilder httpRequestActionBuilder
        if(requestBodyTemplatePath) {
            httpRequestActionBuilder =
                    http(httpDetails)
                            .httpRequest(this.reqMethod.name(), refinedPath)
                            .headers(gatherRequestHeaders())
                            .queryParamMap(queryParams ? queryParams : [:])
                            .body(ElFileBody(requestBodyTemplatePath))
        } else {
            httpRequestActionBuilder =
                    http(httpDetails)
                            .httpRequest(this.reqMethod.name(), refinedPath)
                            .headers(gatherRequestHeaders())
                            .queryParamMap(queryParams ? queryParams : [:])
        }

        httpRequestActionBuilder
    }

    protected HttpProtocolBuilder buildingHttpProtocol(Map<String, Object> apiHeaders) {
        def httpProtocol = http.baseUrl(this.baseUrl)
        if(commonHeaders) {
            httpProtocol.headers(commonHeaders)
        }
        if(apiHeaders) {
            httpProtocol.headers(apiHeaders)
        }
        httpProtocol
    }
}