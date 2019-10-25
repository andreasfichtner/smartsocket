package de.retterdesapok.smartSocketServer

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Device {
    @Id
    @GeneratedValue
    var id: Long? = null
    var type: Long = 0
    var friendlyName: String = ""
    var maxChargingTimeMinutes: Long = 0
    var chargingFinishedHour: Long = 0
    var chargingFinishedMinute: Long = 0
}