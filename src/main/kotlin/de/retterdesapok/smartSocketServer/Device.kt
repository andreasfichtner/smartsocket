package de.retterdesapok.smartSocketServer

import java.sql.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Device {
    @Id
    @GeneratedValue
    var id: Int? = null
    @Column(unique=true)
    var name: String = ""
    var type: String = "default"
    var maxChargingTimeSeconds: Int = 0
    var chargedSeconds: Int = 0
    var chargingFinishedHour: Int = 0
    var chargingFinishedMinute: Int = 0
    var immediateChargingActive: Boolean = false
    var chargingState: String = "unplugged"
    var pluggedInEpoch: Long = 0
    var chargingStartedEpoch: Long = 0
    var chargingDueEpoch: Long = 0
}