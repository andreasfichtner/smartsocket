package de.retterdesapok.smartSocketServer

import org.json.JSONObject

class Utilities {
    fun currentEmissionsData(): JSONObject {
        val baseurl = "https://api.co2signal.com/v1/latest?countryCode=DE"
        val params = mapOf("auth-token" to System.getenv("co2signal_apikey"))
        val emissionsApiResult = khttp.get(baseurl, params)
        val emissionsJson = emissionsApiResult.jsonObject
        return emissionsJson["data"] as JSONObject
    }
}