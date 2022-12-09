package gilli.extras.geo

import gilli.http.HttpClient
import groovy.json.JsonSlurper

class Geo
{
    static Map curLocation()
    {
        String resp = HttpClient.GET('https://ipinfo.io/json')
        //println "resp = $resp"
        return new JsonSlurper().parse(resp.toCharArray());
    }

    static String getCurrentCity()
    {
        return curLocation().get('city')
    }

    static Map latlong(String address)
    {
        def list

        HttpClient.GET {

            host "https://maps.googleapis.com"
            path "maps/api/geocode/xml"

            param 'address', address
            param 'sensor', 'false'
            param 'key', 'AIzaSyBrIfJHaC2GmsXZ5Hmj8hQglYXogW24jzc'

            response {
                xml {
                    root ->
                        list = [
                                    lat: root.result.geometry.location.lat.text(),
                                    lng: root.result.geometry.location.lng.text()
                        ]
                }
            }
        }

        return list
    }
}


