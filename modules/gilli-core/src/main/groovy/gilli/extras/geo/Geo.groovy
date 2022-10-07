package gilli.extras.geo

import gilli.http.HttpClient

class Geo
{
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


