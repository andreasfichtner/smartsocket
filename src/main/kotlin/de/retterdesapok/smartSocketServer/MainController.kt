package de.retterdesapok.smartSocketServer

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping;
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

    @RequestMapping(value = ["/registerDevice"])
    fun registerDevice(): Device {
        val device = Device()
        device.chargingFinishedHour = 8
        device.chargingFinishedMinute = 0
        device.friendlyName = "Testgerät"
        device.maxChargingTimeMinutes = 60
        deviceRepository?.save(device)
        return device
    }

    @RequestMapping(value = ["/devices"])
    fun getDevices(): Iterable<Device>? {
        return deviceRepository?.findAll()
    }
}