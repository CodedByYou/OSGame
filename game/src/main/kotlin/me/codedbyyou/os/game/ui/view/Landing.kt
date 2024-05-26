package me.codedbyyou.os.game.ui.view

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.software.project.ui.controller.NavigationController
import me.codedbyyou.os.game.data.*
import me.codedbyyou.os.game.resource.Config
import me.codedbyyou.os.game.ui.components.ClickyButton

@Composable
fun MainScreen() {
    val isSettingsOpen = remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if(isSettingsOpen.value){
            SettingsScreen { isSettingsOpen.value = false}
        }
        Surface(
            color = Color(0xFF39424E),
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.fillMaxWidth(1f / 3)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(
                        fraction = 1f / 3
                    )
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Main Menu",
                    color = Color.White,
                    textDecoration = TextDecoration.None,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                ClickyButton(
                    text = "Play",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        NavigationController.goTo("serverList")
                    }
                )
                ClickyButton(
                    text = "Settings",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isSettingsOpen.value = true
                    }
                )
                ClickyButton(
                    text = "Exit",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        AppSessionData!!.exitHandle?.let { it() }
                    }
                )
            }
        }
    }
}


@Composable
fun SettingsScreen(onCloseSettings: () -> Unit) {
    var sfxVolume by remember { mutableStateOf(
        Config.sfxMultiplier
    ) }

    var musicVolume by remember {
        mutableStateOf(
            Config.musicMultiplier
        )
    }

    val buttonModifier = Modifier
        .fillMaxWidth(0.8f)
        .padding(vertical = 16.dp)

    val sliderColors = SliderDefaults.colors(
        thumbColor = Color.White.copy(alpha = 0.7f),
        activeTrackColor = Color.White.copy(alpha = 0.9f),
        inactiveTrackColor = Color(0xFF333b46)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = Color(0xFF39424E),
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.fillMaxWidth(1f / 3)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(1f / 3)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Settings",
                    color = Color.White,
                    textDecoration = TextDecoration.None,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                Text("Sfx Volume",
                    textAlign = TextAlign.Left,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Slider(
                    value = sfxVolume,
                    colors = sliderColors,
                    onValueChange = {
                        sfxVolume = it
                        Config.sfxMultiplier = it
                    },
                    modifier = buttonModifier
                )
                Text("Music Volume",
                    textAlign = TextAlign.Left,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(
                        0.8f
                    )
                )
                Slider(
                    value = musicVolume,
                    colors = sliderColors,
                    onValueChange = {
                        musicVolume = it
                        Config.musicMultiplier = it
                    },
                    modifier = buttonModifier
                )
                ClickyButton(
                    text = "Close",
                    onClick = onCloseSettings,
                    modifier = buttonModifier
                )
            }
        }
    }
}


