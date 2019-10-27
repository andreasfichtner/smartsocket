package de.retterdesapok.smartSocketServer

import org.json.JSONObject
import java.time.*
import java.util.*
import kotlin.math.roundToInt

class Utilities {

    private fun loadEmissionsDataFromApi(): JSONObject {
        val baseurl = "https://api.co2signal.com/v1/latest?countryCode=DE"
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

        val zoneID = ZoneId.of("Europe/Berlin");
        val dateTime =  LocalDateTime.now()
        var currentISTime = dateTime.atZone(zoneID);

        if(currentISTime.hour * 60 + currentISTime.minute > device.chargingFinishedHour!! * 60 + device.chargingFinishedMinute!!) {
            currentISTime = currentISTime.withDayOfYear(currentISTime.dayOfYear + 1)
        }
        currentISTime = device.chargingFinishedHour?.let { currentISTime.withHour(it) }
        currentISTime = device.chargingFinishedMinute?.let { currentISTime.withMinute(it) }

        return currentISTime.toEpochSecond()
    }

    fun shouldDeviceCharge(device: Device, emissionsDataRepository: EmissionsDataRepository?): Boolean {
        // Override active, we should charge
        if(device.immediateChargingActive!!) return true

        // Determine time until full charge is required
        val remainingChargeTime = device.maxChargingTimeSeconds!! - device.chargedSeconds

        // Remaining charge time is equal or lower than remaining time to target time
        // We need to charge or we would miss the target
        if(device.chargingDueEpoch!! - Date().toInstant().epochSecond <= remainingChargeTime) return true

        // Simple charging strategy: Require co2 emissions in g/kWh to be smaller than remaining time to start
        val emissions = getCurrentEmissionsData(emissionsDataRepository).carbonIntensity
        println("Emissions" + emissions)
        println("Remaining charge time" + remainingChargeTime);
        return (emissions / remainingChargeTime) > 1
    }

    fun deviceConnected(device: Device, deviceRepository: DeviceRepository?) {
        device.chargingState = "plugged_in"
        device.pluggedInSince = Date().toInstant().epochSecond
        device.accountedChargedSeconds = 0
        device.unaccountedChargingSince = 0
        device.chargingDueEpoch = Utilities().getChargingDueDateForDevice(device)
        deviceRepository?.save(device)
    }

    fun createTestDevices(deviceRepository: DeviceRepository?) {
        val juliasDevice = Device()
        juliasDevice.chargingFinishedHour = 3
        juliasDevice.chargingFinishedMinute = 0
        juliasDevice.name = "Julias Rasenmäher"
        juliasDevice.type = "lawn_mower"
        juliasDevice.maxChargingTimeSeconds = 1800
        juliasDevice.accountedChargedSeconds = 0
        deviceRepository?.save(juliasDevice)

        val alexDevice = Device()
        alexDevice.chargingFinishedHour = 1
        alexDevice.chargingFinishedMinute = 0
        alexDevice.name = "Alex' Autoscooter"
        alexDevice.type = "car"
        alexDevice.maxChargingTimeSeconds = 7200
        alexDevice.accountedChargedSeconds = 0
        deviceRepository?.save(alexDevice)

        val andisDevice = Device()
        andisDevice.chargingFinishedHour = 12
        andisDevice.chargingFinishedMinute = 0
        andisDevice.name = "Andis Nokia"
        andisDevice.type = "smartphone"
        andisDevice.maxChargingTimeSeconds = 600
        andisDevice.accountedChargedSeconds = 0
        deviceRepository?.save(andisDevice)
        deviceConnected(andisDevice, deviceRepository)
    }
}