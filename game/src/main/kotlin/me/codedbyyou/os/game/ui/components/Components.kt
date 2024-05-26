package me.codedbyyou.os.game.ui.components
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.software.project.ui.controller.NavigationController
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.core.interfaces.server.toPacket
import me.codedbyyou.os.game.client.Client
import me.codedbyyou.os.game.data.AppSessionData
import me.codedbyyou.os.game.data.GameViewModel
import me.codedbyyou.os.game.resource.AudioPlayer
import me.codedbyyou.os.game.ui.view.SettingsScreen

@Composable
fun ClickyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val buttonShape = RoundedCornerShape(2.dp)
    val buttonColor = ButtonDefaults.buttonColors(
        backgroundColor = Color(0xFF39424E),
        contentColor = Color.White,
        disabledContentColor = Color.Gray,
        disabledBackgroundColor = Color(0xFF39424E)
    )
    val buttonStroke = BorderStroke(1.dp, Color(0xFF333b46))

    OutlinedButton(
        onClick = {
            AudioPlayer.playOneShotSound("click")
            onClick()
        },
        modifier = modifier.padding(4.dp),
        shape = buttonShape,
        colors = buttonColor,
        border = buttonStroke,
        enabled = enabled
    ) {
        Text(text = text, style = MaterialTheme.typography.button)
    }

}


@Composable
fun ShowGameMenu() {
    val showSettings = AppSessionData.showSettings
    val buttonModifier = Modifier
        .fillMaxWidth(0.8f)
        .padding(vertical = 2.dp)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical=16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showSettings.value){
            SettingsScreen { showSettings.value = false }
        }
        Surface(
            color = Color(0xFF39424E),
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.fillMaxWidth(1f / 3)
                .padding(horizontal = 0.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                androidx.compose.material3.Text(
                    text = "Menu",
                    color = Color.White,
                    textDecoration = TextDecoration.None,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )

                ClickyButton(
                    text = "Continue",
                    modifier = buttonModifier,
                    onClick = {
                        AppSessionData.showMenu.value = false
                    }
                )

                ClickyButton(
                    text = "Settings",
                    modifier = buttonModifier,
                    onClick = {
                        showSettings.value = true
                    }
                )

                if(NavigationController.currentView.value!!.title == "inGame"){
                    ClickyButton(
                        text = "Leave Game",
                        onClick = {
                            NavigationController.goTo("game")
                            Client.connectionManager.sendPacket(
                                PacketType.GAME_PLAYER_LEAVE.toPacket()
                            )
                        },
                        modifier = buttonModifier
                    )
                }
                ClickyButton(
                    text = "Leave Server",
                    modifier = buttonModifier,
                    onClick = {
                        NavigationController.goTo("mainMenu")
                        Client.connectionManager.disconnect()
                    }
                )
            }
        }
    }
}


@Composable
fun TabList(serverIp: String) {
    val showTabList = AppSessionData.showTabList
    val gameStatus  = remember { GameViewModel.gameStatus }
    val secondMessage = remember { GameViewModel.secondMessage }
    val onlinePlayers = remember { GameViewModel.gamePlayerList }
    val tabMessage : String = if (NavigationController.currentView.value!!.title == "game") {
        serverIp
    } else {
        gameStatus.value
    }

    LaunchedEffect(Unit) {
        showTabList.value = false
    }

    LaunchedEffect(showTabList.value) {
        Client.connectionManager.sendPacket(
            PacketType.SERVER_PLAYER_LIST.toPacket()
        )
    }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            androidx.compose.material3.Text(
                text = tabMessage,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .background(
                        Color(0xFF39424E),
                        RoundedCornerShape(0.dp, 0.dp, 5.dp, 5.dp)
                    ).padding(8.dp),
                color = Color(0xFFFFFFFF),
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (!secondMessage.value.isNullOrEmpty())
                androidx.compose.material3.Text(
                    text = secondMessage.value!!,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .background(
                            Color(0xFF39424E).copy(0.1f)
                        ).padding(8.dp),
                    color = Color(0xFFFFFFFF),
                )
        }
        if (showTabList.value) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(
                        fraction = 1f / 5
                    )
                    .padding(horizontal = 16.dp)
                    .padding(top = 64.dp)
                    .background(Color(0xFF39424E), RoundedCornerShape(5.dp))
            ) {
                androidx.compose.material3.Text(
                    text = "Online Players",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
                LazyColumn {
                    items(onlinePlayers) { player ->
                        androidx.compose.material3.Text(
                            text = player,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}