package gilli.http

import gilli.util.DslUtil
import gilli.util.MapClosureDelegate
import groovy.json.JsonBuilder
import groovy.json.JsonDelegate
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import groovy.xml.XmlSlurper

class HttpClient
{
    static void GET(@DelegatesTo(HttpCaller) Closure c) {
        DslUtil.call(c, new HttpCaller()).delegate.call()
    }

    static void POST(@DelegatesTo(HttpCaller) Closure c) {
        DslUtil.call(c, new HttpCaller().method(HttpMethod.POST)).delegate.call()
    }
}

class HttpCaller {
    private String host
    private String path
    private HttpMethod method = HttpMethod.GET
    private Map<String, String> requestProperties = [:]
    private Map<String, List<String>> parameters = [:]
    private Closure responseClosure
    private BodyDelegate body

    void host(String host) {
        this.host = host
    }

    void path(String path) {
        this.path = path
    }

    HttpCaller method(HttpMethod method) {
        this.method = method
        this
    }

    void header(String key, Object value) {
        requestProperties[key] = String.valueOf(value)
    }

    void header(Map<String, Object> map) {
        if (map)
            map.each { k, v -> header(k, v) }
    }

    void header_contentType(String contentType)
    {
        header HttpHeaders.CONTENT_TYPE, contentType
    }

    Map<String, String> getHeader() {
        return requestProperties
    }

    void param(String key, Object value)
    {
        def v = parameters[key]

        if (v == null)
        {
            v = []
            parameters[key] = v
        }

        v.add(String.valueOf(value))
    }

    void param(Map<String, Object> map)
    {
        if (map)
            map.each { k, v -> param(k, v) }
    }

    void body(@DelegatesTo(BodyDelegate) Closure c)
    {
        this.body = DslUtil.call(c, new BodyDelegate()).delegate
    }

    void response(@DelegatesTo(HttpResponseDelegate) Closure c)
    {
        this.responseClosure = c
    }

    void call()
    {
        Client c = ClientBuilder.newClient()

        WebTarget target = c.target(host).path(path)

        if (parameters)
            parameters.each { k, vs -> target = target.queryParam(k, vs.toArray(new String[0])) }

        def b = target.request()

        if (requestProperties)
            requestProperties.each { k, v -> b = b.header(k, v) }

        Response resp = b.method(method.name())
        HttpResponseDelegate del = new HttpResponseDelegate(resp)

        DslUtil.call(responseClosure, del)
    }
}

class BodyDelegate
{
    List<String> bodyTexts = []

    void text(String text)
    {
        bodyTexts << text
    }

    void formData(Closure c)
    {
        def call = DslUtil.call(c, new JsonDelegate())
        bodyTexts << JsonOutput.toJson(call.delegate.content)
    }
}

class HttpResponseDelegate
{
    private Response response

    private File storedFile = File.createTempFile('gilli-http', null)

    HttpResponseDelegate(Response response) {
        this.response = response

        storedFile.withPrintWriter { w ->
            response.readEntity(InputStream).eachLine { w.println(it) }
        }
    }

    Status getStatus() {
        response.statusInfo.toEnum()
    }

    int getStatusCode()
    {
        response.statusInfo.statusCode
    }

    void withRawResponse(@ClosureParams(value = SimpleType, options = ['Response']) Closure c) {
        c.call(response)
    }

    /**
     * The variable `it` in the closure gives the root of the json response. It has the parsed
     * json data structure using Groovy's groovy.json.JsonSlurper. Refer the
     * <a href="https://groovy-lang.org/json.html">link</a> for more details on how to parse
     * @param c
     */
    void json(Closure c) {
        def root = new JsonSlurper().parse(storedFile)
        c.call(root)
    }

    /**
     * The variable `it` in the closure gives the root of the json response. It has the parsed
     *      * json data structure using Groovy's groovy.util.XmlSlurper. Refer the
     *      * <a href="https://groovy-lang.org/processing-xml.html">link</a> for more details on how to parse
     * @param c
     */
    void xml(Closure c) {
        def root = new XmlSlurper().parse(storedFile)
        c.call(root)
    }

    void eachLine(@ClosureParams(value = SimpleType, options = ['String']) Closure c) {
        storedFile.eachLine { c.call(it) }
    }

    void code(@DelegatesTo(ResponseCodeDelegate) Closure c)
    {
        DslUtil.call(c, new ResponseCodeDelegate(this))
    }
}

class ResponseCodeDelegate
{
    private HttpResponseDelegate response

    ResponseCodeDelegate(HttpResponseDelegate response)
    {
        this.response = response
    }

    void on(Status status, Closure c)
    {
        if (status == response.status)
            DslUtil.call(c, response)
    }

    void on(int statusCode, Closure c)
    {
        if (statusCode == response.statusCode)
            DslUtil.call(c, response)
    }
}

enum HttpMethod
{
    GET,
    POST,
    HEAD,
    OPTIONS,
    PUT,
    DELETE,
    TRACE

    String getValue()
    {
        return name().toLowerCase()
    }
}
