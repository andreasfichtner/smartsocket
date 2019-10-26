package de.retterdesapok.smartSocketServer

import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import java.util.*
import kotlin.math.roundToInt


@RestController
class MainController {

    @Autowired
    private val deviceRepository: DeviceRepository? = null
    @Autowired
    private val emissionsDataRepository: EmissionsDataRepository? = null

    @EventListener(ApplicationReadyEvent::class)
    fun doSomethingAfterStartup() {
        initTestDevices()
    }

    @RequestMapping(value = ["/test"])
    fun testPage(): String {
        return "Test"
    }

    @RequestMapping(value = ["/initTestDevices"])
    fun initTestDevices() {
        val juliasDevice = Device()
        juliasDevice.chargingFinishedHour = 8
        juliasDevice.chargingFinishedMinute = 0
        juliasDevice.name = "Julias Rasenmäher"
        juliasDevice.maxChargingTimeSeconds = 10000
        juliasDevice.chargedSeconds = 8000
        deviceRepository?.save(juliasDevice)

        val alexDevice = Device()
        alexDevice.chargingFinishedHour = 8
        alexDevice.chargingFinishedMinute = 0
        alexDevice.name = "Alex' iPhone"
        alexDevice.maxChargingTimeSeconds = 7200
        alexDevice.chargedSeconds = 2000
        deviceRepository?.save(alexDevice)

        val andisDevice = Device()
        andisDevice.chargingFinishedHour = 16
        andisDevice.chargingFinishedMinute = 10
        andisDevice.name = "Andis QuickCharging Test"
        andisDevice.maxChargingTimeSeconds = 360
        andisDevice.chargedSeconds = 0
        deviceRepository?.save(andisDevice)
    }

    @RequestMapping(value = ["/device"], method = [RequestMethod.POST], consumes = ["application/json"])
    fun postDevice(@RequestBody device: Device): Device {
        if(device.id ?: -1 < 0) {
            device.id = null
        }
        deviceRepository?.save(device)
        return device
    }

    @RequestMapping(value = ["/device"], method = [RequestMethod.DELETE])
    fun deleteDevice(id: Int): Boolean {
        val device = deviceRepository?.findById(id)
        if (device != null) {
            deviceRepository?.delete(device)
            return true
        }
        return false
    }

    @RequestMapping(value = ["/emissions/current"])
    fun emissionsCurrent(): EmissionsData {
       return Utilities().getCurrentEmissionsData()
    }

    @RequestMapping(value = ["/devices"])
    fun getDevices(): Iterable<Device>? {
        return deviceRepository?.findAll()
    }

    @RequestMapping(value = ["test/dueDate"])
    fun getDevices(deviceId : Device): Iterable<Device>? {
        return deviceRepository?.findAll()
    }

    @RequestMapping(value = ["test/deviceConnected"])
    fun testDeviceConnected(deviceID: Int) : Boolean {
        val device = deviceRepository?.findById(deviceID)
        if(device != null) {
            deviceConnected(device)
            return Utilities().shouldDeviceCharge(device)
        }

        return false
    }

    fun deviceConnected(device: Device) {
        if(device.chargingState == "unplugged") {
            device.chargingState = "plugged_in"
            device.pluggedInEpoch = Date().toInstant().epochSecond
            device.chargedSeconds = 0
            device.chargingStartedEpoch = 0
            device.chargingDueEpoch = Utilities().getChargingDueDateForDevice(device)
            deviceRepository?.save(device)
        }
    }
}