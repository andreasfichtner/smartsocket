package de.retterdesapok.smartSocketServer

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController;

@RestController
class MainController {

    @Autowired
    private val deviceRepository: DeviceRepository? = null

    @RequestMapping(value = ["/test"])
    fun testPage(): String {
        return "Test"
    }

    @RequestMapping(value = ["/initTestDevice"])
    fun initTestDevice(): Device {
        val device = Device()
        device.chargingFinishedHour = 8
        device.chargingFinishedMinute = 0
        device.friendlyName = "Testgerät"
        device.maxChargingTimeMinutes = 60
        deviceRepository?.save(device)
        return device
    }

    @RequestMapping(value = ["/device"], method = [RequestMethod.POST])
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
    fun emissionsCurrent(): Int {
        return 500
    }

    @RequestMapping(value = ["/emissions/score"])
    fun emissionsScore(): Int {
        return 1
    }

    @RequestMapping(value = ["/devices"])
    fun getDevices(): Iterable<Device>? {
        return deviceRepository?.findAll()
    }
}