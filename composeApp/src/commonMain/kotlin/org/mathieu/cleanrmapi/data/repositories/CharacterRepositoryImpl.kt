package org.mathieu.cleanrmapi.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.mathieu.cleanrmapi.common.mapElement
import org.mathieu.cleanrmapi.common.toList
import org.mathieu.cleanrmapi.data.local.CharacterDAO
import org.mathieu.cleanrmapi.data.local.DataStore
import org.mathieu.cleanrmapi.data.local.objects.CharacterObject
import org.mathieu.cleanrmapi.data.local.objects.toDBObject
import org.mathieu.cleanrmapi.data.local.objects.toDetailedModel
import org.mathieu.cleanrmapi.data.local.objects.toModel
import org.mathieu.cleanrmapi.data.remote.CharacterApi
import org.mathieu.cleanrmapi.data.remote.EpisodeApi
import org.mathieu.cleanrmapi.data.remote.responses.CharacterResponse
import org.mathieu.cleanrmapi.data.validators.annotations.MustBeCommaSeparatedIds
import org.mathieu.cleanrmapi.domain.character.CharacterRepository
import org.mathieu.cleanrmapi.domain.character.models.Character
import org.mathieu.cleanrmapi.domain.character.models.CharacterDetails
import org.mathieu.cleanrmapi.domain.episode.models.Episode
import kotlin.text.map
import kotlin.text.split
import kotlin.text.trim


private const val CHARACTER_PREFS = "character_repository_preferences"
private const val nextPage = "next_characters_page_to_load"

internal class CharacterRepositoryImpl(
    private val dataStore: DataStore,
    private val episodeApi: EpisodeApi,
    private val characterApi: CharacterApi,
    private val characterDAO: CharacterDAO
) : CharacterRepository {

    override suspend fun getCharacters(): Flow<List<Character>> =
        characterDAO
            .getCharacters()
            .mapElement(transform = CharacterObject::toModel)
            .also { if (it.first().isEmpty()) fetchNext() }


    /**
     * Fetches the next batch of characters and saves them to local storage.
     *
     * This function works as follows:
     * 1. Reads the next page number from the data store.
     * 2. If there's a valid next page (i.e., page is not -1), it fetches characters from the API for that page.
     * 3. Extracts the next page number from the API response and updates the data store with it.
     * 4. Transforms the fetched character data into their corresponding realm objects.
     * 5. Saves the transformed realm objects to the local database.
     *
     * Note: If the `next` attribute from the API response is null or missing, the page number is set to -1, indicating there's no more data to fetch.
     */
    private suspend fun fetchNext() {

        val page: Int? = dataStore.retrieveFromInt(nextPage)

        if (page != -1) {

            val response = characterApi.getCharacters(page)

            val nextPageToLoad = response.info.next?.split("?page=")?.last()?.toInt() ?: -1

            dataStore.storeInto(nextPage, nextPageToLoad)

            val objects = response.results.map(transform = CharacterResponse::toDBObject)

            characterDAO.saveCharacters(objects)
        }

    }


    override suspend fun loadMore() = fetchNext()

    override suspend fun getCharacterDetailed(id: Int): CharacterDetails {

        val characterLocal = GetCharacterObjectIfExists(characterId = id)

        return characterLocal.toDetailedModel(
            idsToEpisodesConverter = ::getEpisodesFromIdList
        )

    }

    override suspend fun getCharacterFromIdList(@MustBeCommaSeparatedIds idList: String): List<Character> {

        val charactersLocal = GetCharacterObjectsIfExists(idList)

        return charactersLocal.map { it.toModel() }
    }

    override suspend fun getEpisodesWhere(characterId: Int): List<Episode> {
        val characterLocal = GetCharacterObjectIfExists(characterId)

        return getEpisodesFromIdList(idList = characterLocal.episodesIds)

    }

    private suspend fun getEpisodesFromIdList(@MustBeCommaSeparatedIds idList: String): List<Episode> {

        //TODO: fetch system

        return if (idList.contains(",")) {
            val episodesResponse = episodeApi.getEpisodesFromIds(ids = idList)
            episodesResponse.map { it.toDBObject().toModel() }
        } else {
            val episodeResponse = episodeApi.getEpisode(idList.toInt())
            episodeResponse?.toDBObject()?.toModel()?.toList() ?: emptyList()
        }

    }

}
/**
 * Orchestrates the retrieval of a CharacterObject by attempting to fetch it locally first,
 * then remotely if it's not found in the local storage.
 * @throws Exception when the character cannot be found both locally and remotely.
 *
 */
private object GetCharacterObjectIfExists : KoinComponent {

    private val characterApi: CharacterApi by inject()
    private val characterLocal: CharacterDAO by inject()


    suspend operator fun invoke(characterId: Int): CharacterObject =
        tryToGetCharacterLocally(characterId)
            .fetchRemotelyIfNotFound(characterId)
            .throwIfWeCannotFindIt()


    private suspend fun tryToGetCharacterLocally(id: Int) = characterLocal.getCharacter(id)

    private suspend fun CharacterObject?.fetchRemotelyIfNotFound(id: Int): CharacterObject? {
        if (this != null) return this

        return characterApi.getCharacter(id = id)
            ?.toDBObject()
            ?.also { obj ->
                characterLocal.insert(obj)
            }
    }

    private fun CharacterObject?.throwIfWeCannotFindIt(): CharacterObject {
        if (this != null) return this
        throw Exception("Could not find Character locally and remotely.")
    }

}

/**
 * Retrieves a list of CharacterObjects from the local database if they exist,
 * otherwise fetches them from the remote API and saves them to the local database.
 */
private object GetCharacterObjectsIfExists : KoinComponent {

    private val characterApi: CharacterApi by inject()
    private val characterLocal: CharacterDAO by inject()


    /**
     * Retrieves a list of CharacterObjects from the local database if they exist,
     * otherwise fetches them from the remote API and saves them to the local database.
     *
     * @param idList A comma-separated string of character IDs.
     *
     * @return A list of CharacterObjects corresponding to the provided IDs.
     */
    suspend operator fun invoke(@MustBeCommaSeparatedIds idList: String): List<CharacterObject> =
        tryToGetCharactersLocally(idList.split(",").map { it.trim() })
            .fetchRemotelyIfNotFound(idList)
            .throwIfWeCannotFindIt()

    /**
     * Retrieves a list of CharacterObjects from the local database if they exist,
     * otherwise returns null.
     *
     * @param ids A list of character IDs.
     *
     * @return A list of CharacterObjects corresponding to the provided IDs, or null if they do not exist.
     */
    private suspend fun tryToGetCharactersLocally(ids: List<String>): List<CharacterObject> = characterLocal.getCharactersByIds(ids)

    /**
     * Fetches a list of CharacterObjects from the remote API if they do not exist in the local database,
     * otherwise returns the existing CharacterObjects.
     *
     * @param idList A comma-separated string of character IDs.
     *
     * @return A list of CharacterObjects corresponding to the provided IDs.
     */
    private suspend fun List<CharacterObject>?.fetchRemotelyIfNotFound(idList: String): List<CharacterObject> {
        if (this != null) return this

        return if (idList.contains(",")) {
             characterApi
                .getCharactersFromIds(idList)
                .map { it.toDBObject() }
                .also { characterLocal.saveCharacters(it) }
        } else {
             characterApi.getCharacter(idList.toInt())
                ?.toDBObject()?.toList() ?: emptyList()
        }
    }

    /**
     * Throws an exception if a list of CharacterObjects cannot be found in the local database and the remote API.
     *
     * @return A list of CharacterObjects corresponding to the provided IDs.
     */
    private fun List<CharacterObject>?.throwIfWeCannotFindIt(): List<CharacterObject> {
        if (this != null) return this
        throw Exception("Could not find Characters locally and remotely.")
    }

}
