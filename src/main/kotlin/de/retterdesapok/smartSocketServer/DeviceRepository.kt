package de.retterdesapok.smartSocketServer

import org.springframework.data.repository.CrudRepository

interface DeviceRepository : CrudRepository<Device, Long> {
    public fun findById(id: String) : Device
    public fun existsById(id: String) : Boolean
}