package de.retterdesapok.smartSocketServer

import com.fasterxml.jackson.annotation.JsonIgnore
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

    var chargingFinishedHour: Int = 0
    var chargingFinishedMinute: Int = 0
    var immediateChargingActive: Boolean = false
    var chargingState: String = "unplugged"
    var chargingDueEpoch: Long = 0
    var pluggedInSince: Long = 0
    val chargedSeconds get() = computeChargedSeconds()

    @JsonIgnore
    var unaccountedChargingSince: Long = 0

    @JsonIgnore
    var accountedChargedSeconds: Long = 0

    fun computeChargedSeconds(): Long {
        val unaccountedChargeSeconds = if(unaccountedChargingSince > 0) java.util.Date().toInstant().epochSecond - unaccountedChargingSince else 0
        return accountedChargedSeconds + unaccountedChargeSeconds
    }
}