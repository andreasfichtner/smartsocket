package de.retterdesapok.smartSocketServer

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import java.util.*

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
        if (deviceRepository != null) {
            Utilities().createTestDevices(deviceRepository)
        }
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
       return Utilities().getCurrentEmissionsData(emissionsDataRepository)
    }

    @RequestMapping(value = ["/devices"])
    fun getDevices(): Iterable<Device>? {
        return deviceRepository?.findAll()
    }

    @RequestMapping(value = ["test/dueDate"])
    fun getDevices(deviceId : Device): Iterable<Device>? {
        return deviceRepository?.findAll()
    }

    @RequestMapping(value = ["setSmartSocketInfo"])
    fun setSmartSocketInfo(deviceID: Int) : Boolean {
        val device = deviceRepository?.findById(deviceID)
        if(device != null) {
            var shouldCharge = false

            if(device.chargingState == "unplugged") {
                deviceConnected(device)
                shouldCharge = Utilities().shouldDeviceCharge(device, emissionsDataRepository)
            } else if(device.chargingState == "plugged_in") {
                shouldCharge = Utilities().shouldDeviceCharge(device, emissionsDataRepository)
            } else if(device.chargingState == "charging") {
                shouldCharge = Utilities().shouldDeviceCharge(device, emissionsDataRepository)
                if(shouldCharge) {

                }
            }

            return shouldCharge
        }

        return false
    }

    fun deviceConnected(device: Device) {
        device.chargingState = "plugged_in"
        device.pluggedInSince = Date().toInstant().epochSecond
        device.accountedChargedSeconds = 0
        device.unaccountedChargingSince = 0
        device.chargingDueEpoch = Utilities().getChargingDueDateForDevice(device)
        deviceRepository?.save(device)
    }
}