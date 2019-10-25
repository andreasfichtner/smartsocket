package de.retterdesapok.smartSocketServer

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Device {
    @Id
    @GeneratedValue
    var id: Int? = null
    var type: Int = 0
    var friendlyName: String = ""
    var maxChargingTimeMinutes: Int = 0
    var chargingFinishedHour: Int = 0
    var chargingFinishedMinute: Int = 0
    var immediateChargingActive: Boolean = false
    var chargingState: Int = 0
}