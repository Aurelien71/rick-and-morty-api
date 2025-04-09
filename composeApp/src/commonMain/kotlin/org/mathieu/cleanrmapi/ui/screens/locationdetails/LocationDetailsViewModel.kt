package org.mathieu.cleanrmapi.ui.screens.locationdetails

import org.koin.core.component.inject
import org.mathieu.cleanrmapi.domain.character.models.Character
import org.mathieu.cleanrmapi.domain.location.LocationRepository
import org.mathieu.cleanrmapi.ui.core.Destination
import org.mathieu.cleanrmapi.ui.core.ViewModel
import org.mathieu.cleanrmapi.ui.screens.episodedetails.EpisodeDetailsAction

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

sealed interface LocationDetailsAction {
    data class SelectedCharacter(val character: Character): LocationDetailsAction
}
