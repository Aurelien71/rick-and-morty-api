package org.mathieu.cleanrmapi.data.repositories

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.mathieu.cleanrmapi.data.local.LocationDAO
import org.mathieu.cleanrmapi.data.local.objects.LocationObject
import org.mathieu.cleanrmapi.data.local.objects.toDBObject
import org.mathieu.cleanrmapi.data.local.objects.toModel
import org.mathieu.cleanrmapi.data.remote.LocationApi
import org.mathieu.cleanrmapi.data.validators.annotations.MustBeCommaSeparatedIds
import org.mathieu.cleanrmapi.domain.character.CharacterRepository
import org.mathieu.cleanrmapi.domain.character.models.Character
import org.mathieu.cleanrmapi.domain.location.LocationRepository
import org.mathieu.cleanrmapi.domain.location.models.Location

/**
 * Implementation of the [LocationRepository] interface that retrieves location data from the local database
 * and the remote API.
 *
 * @param characterRepository The repository for retrieving character data.
 *
 * @return A [LocationRepository] object that can be used to retrieve location data.
 */
internal class LocationRepositoryImpl(
    private val characterRepository: CharacterRepository
) : LocationRepository {

    override suspend fun getLocation(locationId: Int): Location {

        val episodeLocal = GetLocationObjectIfExists(locationId)

        return episodeLocal.toModel(
            idsToCharactersConverter = ::getCharactersFromIdList
        )
    }

    /**
     * Retrieves a list of characters associated with a given location.
     *
     * @param idList The unique identifier of the character.
     *
     * @return A list of [Character] objects representing the characters associated
     */
    private suspend fun getCharactersFromIdList(@MustBeCommaSeparatedIds idList: String): List<Character> =
        characterRepository.getCharacterFromIdList(idList)

}

/**
 * Retrieves a [LocationObject] from the local database if it exists,
 * otherwise fetches it from the remote API and saves it to the local database.
 */
private object GetLocationObjectIfExists : KoinComponent {

    private val locationApi: LocationApi by inject()
    private val locationLocal: LocationDAO by inject()

    /**
     * Retrieves a [LocationObject] from the local database if it exists,
     * otherwise fetches it from the remote API and saves it to the local database.
     *
     * @param locationId The unique identifier of the location.
     *
     * @return A [LocationObject] object representing the location.
     */
    suspend operator fun invoke(locationId: Int): LocationObject =
        tryToGetLocationLocally(locationId)
            .fetchRemotelyIfNotFound(locationId)
            .throwIfWeCannotFindIt()

    /**
     * Retrieves a [LocationObject] from the local database if it exists,
     * otherwise returns null.
     *
     * @param id The unique identifier of the location.
     *
     * @return A [LocationObject] object representing the location, or null if it does not exist.
     */
    private suspend fun tryToGetLocationLocally(id: Int) = locationLocal.getLocation(id)

    /**
     * Fetches a [LocationObject] from the remote API if it does not exist in the local database,
     * otherwise returns the existing [LocationObject].
     *
     * @param id The unique identifier of the location.
     *
     * @return A [LocationObject] object representing the location.
     */
    private suspend fun LocationObject?.fetchRemotelyIfNotFound(id: Int): LocationObject? {
        if (this != null) return this

        return locationApi.getLocation(id)
            ?.toDBObject()
            ?.also { obj ->
                locationLocal.insert(obj)
            }
    }

    /**
     * Throws an exception if a [LocationObject] cannot be found in the local database and the remote API.
     *
     * @return A [LocationObject] object representing the location.
     */
    private fun LocationObject?.throwIfWeCannotFindIt(): LocationObject {
        if (this != null) return this
        throw Exception("Could not find Location locally and remotely.")
    }

}
