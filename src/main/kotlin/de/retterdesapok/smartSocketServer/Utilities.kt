package de.retterdesapok.smartSocketServer

import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import java.time.*
import java.util.*
import java.util.spi.CalendarDataProvider
import kotlin.math.roundToInt

class Utilities {

    @Autowired
    private val deviceRepository: DeviceRepository? = null
    @Autowired
    private val emissionsDataRepository: EmissionsDataRepository? = null

    fun loadEmissionsDataFromApi(): JSONObject {
        val baseurl = "https://api.co2signal.com/v1/latest?countryCode=DE"
        val params = mapOf("auth-token" to System.getenv("co2signal_apikey"))
        val emissionsApiResult = khttp.get(baseurl, params)
        val emissionsJson = emissionsApiResult.jsonObject
        return emissionsJson["data"] as JSONObject
    }

    fun getCurrentEmissionsData(): EmissionsData {
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

    fun shouldDeviceCharge(device: Device): Boolean {
        // Override active, we should charge
        if(device.immediateChargingActive) return true

        // Determine time until full charge is required
        val remainingChargeTime = device.maxChargingTimeSeconds - device.chargedSeconds

        // Remaining charge time is equal or lower than remaining time to target time
        // We need to charge or we would miss the target
        if(device.chargingDueEpoch - Date().toInstant().epochSecond <= remainingChargeTime) return true

        // Simple charging strategy: Require co2 emissions in g/kWh to be smaller than remaining time to start
        val emissions = getCurrentEmissionsData().carbonIntensity
        return emissions < remainingChargeTime
    }
}