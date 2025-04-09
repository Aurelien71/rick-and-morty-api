package org.mathieu.cleanrmapi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.mathieu.cleanrmapi.data.local.objects.LocationObject

/**
 * Represents a location entity stored in the SQLite database. This object provides fields
 * necessary to represent all the attributes of a location from the data source.
 * The object is specifically tailored for SQLite storage using Realm.
 */
@Dao
interface LocationDAO {

    @Query("select * from ${RMDatabase.LOCATION_TABLE} where id = :id")
    suspend fun getLocation(id: Int): LocationObject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationObject)

}