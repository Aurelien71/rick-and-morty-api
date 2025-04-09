package org.mathieu.cleanrmapi.domain.location

import org.mathieu.cleanrmapi.domain.location.models.Location

interface LocationRepository {

    /**
     * Fetches the characters of a specific location.
     *
     * @param locationId The unique identifier of the location.
     *
     * @return Characters that have been known to reside or appear in this location.
     */
    suspend fun getLocation(locationId: Int): Location

}