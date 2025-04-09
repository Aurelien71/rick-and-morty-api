package org.mathieu.cleanrmapi.data.repositories

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.mathieu.cleanrmapi.common.toList
import org.mathieu.cleanrmapi.data.local.LocationDAO
import org.mathieu.cleanrmapi.data.local.objects.LocationObject
import org.mathieu.cleanrmapi.data.local.objects.toDBObject
import org.mathieu.cleanrmapi.data.local.objects.toModel
import org.mathieu.cleanrmapi.data.remote.CharacterApi
import org.mathieu.cleanrmapi.data.remote.LocationApi
import org.mathieu.cleanrmapi.data.validators.annotations.MustBeCommaSeparatedIds
import org.mathieu.cleanrmapi.domain.character.models.Character
import org.mathieu.cleanrmapi.domain.location.LocationRepository
import org.mathieu.cleanrmapi.domain.location.models.Location

internal class LocationRepositoryImpl(
    private val characterApi: CharacterApi
) : LocationRepository {

    override suspend fun getLocation(locationId: Int): Location {

        val episodeLocal = GetLocationObjectIfExists(locationId)

        return episodeLocal.toModel(
            idsToCharactersConverter = ::getCharactersFromIdList
        )
    }

    /**
     * Retrieves a list of characters associated with a given location.
     * @param idList The unique identifier of the character.
     * @return A list of [Character] objects representing the characters associated
     */
    private suspend fun getCharactersFromIdList(@MustBeCommaSeparatedIds idList: String): List<Character> {
        return if (idList.contains(",")) {
            val characterResponse = characterApi.getCharactersFromIds(idList)
            characterResponse.map { it.toDBObject().toModel() }
        } else {
            val characterResponse = characterApi.getCharacter(idList.toInt())
            characterResponse?.toDBObject()?.toModel()?.toList() ?: emptyList()
        }
    }
}

private object GetLocationObjectIfExists : KoinComponent {

    private val locationApi: LocationApi by inject()
    private val locationLocal: LocationDAO by inject()


    suspend operator fun invoke(locationId: Int): LocationObject =
        tryToGetLocationLocally(locationId)
            .fetchRemotelyIfNotFound(locationId)
            .throwIfWeCannotFindIt()


    private suspend fun tryToGetLocationLocally(id: Int) = locationLocal.getLocation(id)

    private suspend fun LocationObject?.fetchRemotelyIfNotFound(id: Int): LocationObject? {
        if (this != null) return this

        return locationApi.getLocation(id)
            ?.toDBObject()
            ?.also { obj ->
                locationLocal.insert(obj)
            }
    }

    private fun LocationObject?.throwIfWeCannotFindIt(): LocationObject {
        if (this != null) return this
        throw Exception("Could not find Location locally and remotely.")
    }

}
