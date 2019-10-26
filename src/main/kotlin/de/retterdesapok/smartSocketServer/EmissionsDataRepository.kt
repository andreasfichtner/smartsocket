package de.retterdesapok.smartSocketServer

import org.springframework.data.repository.CrudRepository

interface EmissionsDataRepository : CrudRepository<EmissionsData, Long> {
    public fun findById(id: Int) : EmissionsData
    public fun findTopByOrderByIdDesc() : EmissionsData?
}
