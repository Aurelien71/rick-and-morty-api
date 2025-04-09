package org.mathieu.cleanrmapi.ui.screens.locationdetails

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mathieu.cleanrmapi.ui.core.composables.BackArrow
import org.mathieu.cleanrmapi.ui.core.composables.CharacterCard
import org.mathieu.cleanrmapi.ui.core.composables.PreviewContent
import org.mathieu.cleanrmapi.ui.core.composables.Screen
import org.mathieu.cleanrmapi.ui.core.theme.PrimaryColor
import org.mathieu.cleanrmapi.ui.core.theme.SurfaceColor
import org.mathieu.cleanrmapi.shared.*

@Composable
fun LocationDetailsScreen(
    navController: NavController,
    id: Int
) {
    val soundManager = rememberSoundManager()

    Screen(
        viewModel = viewModel { LocationDetailsViewModel() },
        navController = navController
    ) { state, viewModel ->

        LaunchedEffect(key1 = Unit) {
            viewModel.init(locationId = id)
            soundManager.playSound("https://static.wikia.nocookie.net/minecraft_gamepedia/images/d/d3/Cave1.ogg/revision/latest?cb=20190815201857")
        }

        Content(
            state = state,
            onClickBack = navController::popBackStack,
            onAction = viewModel::handleAction
        )

    }

}

@Composable
private fun Content(
    state: LocationDetailsState = LocationDetailsState.Loading,
    onAction: (LocationDetailsAction) -> Unit = { },
    onClickBack: () -> Unit = { }
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .padding(),
    contentAlignment = Alignment.Center
) {

    BackArrow(
        modifier = Modifier
            .align(Alignment.TopStart)
            .zIndex(1f),
        onClick = onClickBack
    )

    Crossfade(targetState = state) {
        when (it) {
            is LocationDetailsState.Error -> ErrorView(error = it.message)
            is LocationDetailsState.Loaded -> CharacterDetailsContent(
                state = it,
                onAction = onAction
            )
            LocationDetailsState.Loading -> {

            }
        }
    }
}


@Composable
private fun ErrorView(error: String) {
    Text(
        modifier = Modifier.padding(16.dp),
        text = error,
        textAlign = TextAlign.Center,
        color = PrimaryColor,
        fontSize = 32.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 36.sp
    )
}


private object CharacterDetailsContent {

    @Composable
    operator fun invoke(
        state: LocationDetailsState.Loaded,
        onAction: (LocationDetailsAction) -> Unit = { }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Header(state = state)

            LazyVerticalGrid(
                columns = GridCells.Fixed(2)
            ) {



                items(state.residents) { character ->

                    CharacterCard(
                        modifier = Modifier
                            .clickable {
                                onAction(LocationDetailsAction.SelectedCharacter(character))
                            },
                        character = character
                    )

                }

            }

        }


    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Header(state: LocationDetailsState.Loaded) {

        Column(
            modifier = Modifier
                .background(SurfaceColor)
                .fillMaxWidth()
                .height(72.dp)
                .padding(start = 48.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            Text(text = state.type, fontSize = 14.sp)

            Text(
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                text = "${state.dimension} - ${state.name}",
                maxLines = 1,
                fontSize = 24.sp,
            )

        }

    }

}

@Preview
@Composable
private fun CharacterDetailsPreview() = PreviewContent {
    Content()
}

