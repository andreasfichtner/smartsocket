package de.retterdesapok.smartSocketServer

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Device {
    @Id
    @GeneratedValue
    var id: Int? = null
    @Column(unique=true, nullable=true)
    var tag: String? = null
    @Column(unique=true)
    var name: String? = null
    var type: String? = "default"
    var maxChargingTimeSeconds: Int? = null

    var chargingFinishedHour: Int? = null
    var chargingFinishedMinute: Int? = null
    var immediateChargingActive: Boolean? = false
    var chargingState: String = "unplugged"
    var chargingDueEpoch: Long? = null
    var pluggedInSince: Long? = null
    val chargedSeconds get() = computeChargedSeconds()

    @JsonIgnore
    var unaccountedChargingSince: Long? = null

    @JsonIgnore
    var accountedChargedSeconds: Long = 0

    private fun computeChargedSeconds(): Long {

        unaccountedChargingSince?.let {
            val unaccountedChargeSeconds = if (it > 0) java.util.Date().toInstant().epochSecond - it else 0
            return accountedChargedSeconds + unaccountedChargeSeconds
        }

        return accountedChargedSeconds
    }
}