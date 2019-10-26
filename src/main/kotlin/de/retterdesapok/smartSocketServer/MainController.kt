package de.retterdesapok.smartSocketServer

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

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
    fun postDevice(@RequestBody sentDevice: Device): Device? {
        if(sentDevice.id ?: -1 < 0) {
            // Existing device
            sentDevice.id = null
            deviceRepository?.save(sentDevice)
            return sentDevice
        } else {
            // Updated device
            val device = deviceRepository?.findById(sentDevice.id!!)
            if (device != null) {
                device.name = sentDevice.name ?: device.name
                device.type = sentDevice.type ?: device.type
                device.immediateChargingActive = sentDevice.immediateChargingActive ?: device.immediateChargingActive
                device.chargingFinishedHour = sentDevice.chargingFinishedHour ?: device.chargingFinishedHour
                device.chargingFinishedMinute = sentDevice.chargingFinishedMinute ?: device.chargingFinishedMinute
                deviceRepository?.save(device)
            }

            return device
        }
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
                Utilities().deviceConnected(device, deviceRepository)
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
}