package org.mathieu.cleanrmapi.ui.screens.locationdetails

import org.koin.core.component.inject
import org.mathieu.cleanrmapi.domain.character.models.Character
import org.mathieu.cleanrmapi.domain.location.LocationRepository
import org.mathieu.cleanrmapi.ui.core.Destination
import org.mathieu.cleanrmapi.ui.core.ViewModel

/**
 * ViewModel class for the Location Details screen.
 * Extends the ViewModel class with a LocationDetailsState.
 * Inherits from KoinComponent for dependency injection.
 *
 * @property locationRepository The repository for accessing location data.
 * @property init Initializes the ViewModel with the provided location ID.
 * @property handleAction Handles the provided action.
 * @property selectedCharacter Handles the selection of a character.
 * @property sendEvent Sends an event to the navigation stack.
 */
class LocationDetailsViewModel :
    ViewModel<LocationDetailsState>(LocationDetailsState.Loading) {

    private val locationRepository: LocationRepository by inject()

    fun init(locationId: Int) {

        fetchData(
            source = { locationRepository.getLocation(locationId = locationId) }
        ) {
            onSuccess { details ->
                updateState {
                    LocationDetailsState.Loaded(
                        name = details.name,
                        type = details.type,
                        dimension = details.dimension,
                        residents = details.residents
                    )
                }
            }

            onFailure {
                updateState {
                    LocationDetailsState.Error(message = it.message ?: it.toString())
                }
            }

        }

    }

    fun handleAction(action: LocationDetailsAction) {
        when(action) {
            is LocationDetailsAction.SelectedCharacter -> selectedCharacter(action.character)
        }
    }

    private fun selectedCharacter(character: Character) =
        sendEvent(Destination.CharacterDetails(character.id.toString()))
}

/**
 * Sealed interface representing the state of the Location Details screen.
 * Contains the following states:
 * - Loading: The screen is currently loading.
 * - Error: An error occurred while loading the screen.
 * - Loaded: The screen has been loaded successfully.
 */
sealed interface LocationDetailsState {
    data object Loading : LocationDetailsState

    data class Error(val message: String) : LocationDetailsState

    data class Loaded(
        val name: String,
        val type: String,
        val dimension: String,
        val residents: List<Character>
    ) : LocationDetailsState

}

/**
 * Sealed interface representing the actions that can be performed on the Location Details screen.
 * Contains the following actions:
 * - SelectedCharacter: The user has selected a character.
 */
sealed interface LocationDetailsAction {
    data class SelectedCharacter(val character: Character): LocationDetailsAction
}
