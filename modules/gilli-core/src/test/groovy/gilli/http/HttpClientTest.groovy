package gilli.http

import org.testng.annotations.Test

import javax.ws.rs.core.Response

@Test
void simpleHttpClientGET()
{
    HttpClient.GET {

        host 'https://httpbin.org/'
        path 'get'

        param 'k1', 'v1'
        param 'k2', 'v2'
        param 'k2', 'v3'

        header 'header1', 'some-header1-value'

        response {

            withRawResponse {

                println "statusinfo = " + it.statusInfo
                println "status int = " + it.status
            }

            code {

                on(Response.Status.OK) {
                    println "STATUS : $status"
                }
            }

            json {

                println it.args.k1
            }

            eachLine {println it}

        }
    }
}

@Test
void simpleHttpClientPOST()
{
    HttpClient.POST {

        host 'https://httpbin.org/'
        path 'post'

        param 'k1', 'v1'
        param 'k2', 'v2'
        param 'k2', 'v3'

        header 'header1', 'some-header1-value'

        body {
            text 'first line of body'

            formData {
                key 'value'

                key2('some attr') {
                    key3 : 'value3'
                }
            }

        }

        response {

            withRawResponse {

                println "statusinfo = " + it.statusInfo
                println "status int = " + it.status
            }

            code {

                on(Response.Status.OK) {
                    println "STATUS : $status"
                }
            }

            json {

                println it.args.k1
            }

            eachLine {println it}

        }
    }
}
