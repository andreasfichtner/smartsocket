package de.retterdesapok.smartSocketServer

import org.json.JSONObject
import java.time.*
import java.util.*
import kotlin.math.roundToInt

class Utilities {

    private fun loadEmissionsDataFromApi(): JSONObject {
        val baseurl = "https://api.co2signal.com/v1/latest?countryCode=AT"
        val params = mapOf("auth-token" to System.getenv("co2signal_apikey"))
        val emissionsApiResult = khttp.get(baseurl, params)
        val emissionsJson = emissionsApiResult.jsonObject
        return emissionsJson["data"] as JSONObject
    }

    fun getCurrentEmissionsData(emissionsDataRepository: EmissionsDataRepository?): EmissionsData {
        var lastDataPoint = emissionsDataRepository?.findTopByOrderByIdDesc()
        if(lastDataPoint == null || lastDataPoint.epochSecond < Date().toInstant().epochSecond - 60) {
            val currentData = loadEmissionsDataFromApi()
            lastDataPoint = EmissionsData()
            lastDataPoint.carbonIntensity = (currentData["carbonIntensity"] as Double).roundToInt()
            lastDataPoint.fossilFuelPercentage = (currentData["fossilFuelPercentage"] as Double).toInt()
            emissionsDataRepository?.save(lastDataPoint)
        }
        return lastDataPoint
    }

    // Device has just been connected, at what time should charging be finished?
    fun getChargingDueDateForDevice(device: Device): Long {
        var dateTime = ZonedDateTime.now()
        if(dateTime.hour * 60 + dateTime.minute > device.chargingFinishedHour * 60 + device.chargingFinishedMinute) {
            dateTime = dateTime.withDayOfYear(dateTime.dayOfYear + 1)
        }
        dateTime = dateTime.withHour(device.chargingFinishedHour)
        dateTime = dateTime.withMinute(device.chargingFinishedMinute)

        return dateTime.toEpochSecond()
    }

    fun shouldDeviceCharge(device: Device, emissionsDataRepository: EmissionsDataRepository?): Boolean {
        // Override active, we should charge
        if(device.immediateChargingActive) return true

        // Determine time until full charge is required
        val remainingChargeTime = device.maxChargingTimeSeconds - device.chargedSeconds

        // Remaining charge time is equal or lower than remaining time to target time
        // We need to charge or we would miss the target
        if(device.chargingDueEpoch - Date().toInstant().epochSecond <= remainingChargeTime) return true

        // Simple charging strategy: Require co2 emissions in g/kWh to be smaller than remaining time to start
        val emissions = getCurrentEmissionsData(emissionsDataRepository).carbonIntensity
        return (emissions / remainingChargeTime) > 1
    }

    fun createTestDevices(deviceRepository: DeviceRepository?) {
        val juliasDevice = Device()
        juliasDevice.chargingFinishedHour = 19
        juliasDevice.chargingFinishedMinute = 0
        juliasDevice.name = "Julias Rasenmäher"
        juliasDevice.type = "lawn_mower"
        juliasDevice.maxChargingTimeSeconds = 10000
        juliasDevice.accountedChargedSeconds = 8000
        deviceRepository?.save(juliasDevice)

        val alexDevice = Device()
        alexDevice.chargingFinishedHour = 23
        alexDevice.chargingFinishedMinute = 0
        alexDevice.name = "Alex' Autoscooter"
        alexDevice.type = "car"
        alexDevice.maxChargingTimeSeconds = 7200
        alexDevice.accountedChargedSeconds = 2000
        deviceRepository?.save(alexDevice)
    }
}