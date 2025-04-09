package org.mathieu.cleanrmapi.data.local.objects

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.mathieu.cleanrmapi.data.extensions.extractIdsFromUrls
import org.mathieu.cleanrmapi.data.local.RMDatabase
import org.mathieu.cleanrmapi.data.remote.responses.LocationResponse
import org.mathieu.cleanrmapi.data.validators.annotations.MustBeCommaSeparatedIds
import org.mathieu.cleanrmapi.domain.character.models.Character
import org.mathieu.cleanrmapi.domain.location.models.Location


/**
 * Represents a location entity stored in the SQLite database. This object provides fields
 * necessary to represent all the attributes of a location from the data source.
 * The object is specifically tailored for SQLite storage using Realm.
 *
 * @property id Unique identifier of the character.
 * @property name Name of the location.
 * @property type The type or category of the location.
 * @property dimension The dimension or location in which the location is located.
 * @property charactersIds Ids of the characters who appears in this location.
 */
@Entity(tableName = RMDatabase.LOCATION_TABLE)
class LocationObject (
    @PrimaryKey
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    @MustBeCommaSeparatedIds
    val charactersIds: String,
)

internal suspend fun LocationObject.toModel(
    idsToCharactersConverter: suspend (charactersIds: String) -> List<Character> = { emptyList() }
) = Location(
    id = id,
    name = name,
    type = type,
    dimension = dimension,
    residents = idsToCharactersConverter(charactersIds)
)

internal fun LocationResponse.toDBObject() = LocationObject(
    id = id,
    name = name,
    type = type,
    dimension = dimension,
    charactersIds = residents.extractIdsFromUrls()
)