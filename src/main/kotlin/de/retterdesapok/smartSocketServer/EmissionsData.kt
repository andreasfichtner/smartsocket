package de.retterdesapok.smartSocketServer

import java.util.Date
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class EmissionsData {
    @Id
    @GeneratedValue
    var id: Int? = null
    var epochSecond = Date().toInstant().epochSecond
    var carbonIntensity = 0
    var fossilFuelPercentage = 0

}