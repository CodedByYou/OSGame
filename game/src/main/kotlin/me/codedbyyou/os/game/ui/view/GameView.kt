package me.codedbyyou.os.game.ui.view

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.codedbyyou.os.core.enums.RoomStatus
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.core.interfaces.server.toPacket
import me.codedbyyou.os.core.models.GameRoomInfo
import me.codedbyyou.os.game.client.Client
import me.codedbyyou.os.game.data.AppSessionData
import me.codedbyyou.os.game.data.GameViewModel
import me.codedbyyou.os.game.data.LobbyViewModel
import me.codedbyyou.os.game.ui.*
import me.codedbyyou.os.game.ui.components.*
import me.codedbyyou.os.game.utils.getResource


@Composable
fun GameLobbyScreen(
    serverIp: String,
    onJoinGame: (GameRoomInfo) -> Unit,
    onStartNewGame: () -> Unit
) {
    val showSettingsMenu = AppSessionData.showMenu
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = loadImageBitmap(getResource("bg.png")),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            contentAlignment = Alignment.TopStart,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF333b46))
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        "Profile",
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = Client.user!!.nickTicket,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF333b46),
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .alignByBaseline()
                )
            }
        }

        GameListTabbedPane(
            onStartNewGame = onStartNewGame,
            onJoinGame = onJoinGame
        )
        TabList(serverIp)
        ChatBox()
        if (showSettingsMenu.value) {
            ShowGameMenu()
        }
    }
}

@Composable
fun InGameScene(
    playerName: String,
    serverIp: String
) {
    val showSettingsMenu = AppSessionData.showMenu
    val chancesLeft = remember { GameViewModel.chancesLeft }
    val canGuess = remember { GameViewModel.canGuess }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Image(
            bitmap = loadImageBitmap(getResource("bg.png")),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            contentAlignment = Alignment.TopStart,
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF333b46))
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            "Profile",
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = playerName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF333b46),
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .alignByBaseline()
                    )
                }

                // chances left / lives left
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF333b46))
                    ) {
                        Icon(
                            Icons.Outlined.Gamepad,
                            "Profile",
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (chancesLeft.value == -1) {
                        Text(
                            text = "Chances Left: ∞",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF333b46),
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .alignByBaseline()
                        )
                    } else {
                        Text(
                            text = "Chances Left: ${chancesLeft.value}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF333b46),
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .alignByBaseline()
                        )
                    }
                }
            }
        }

        if (canGuess.value) {
            GuessingWheel { guess ->
                Client.connectionManager.sendPacket(
                    PacketType.GAME_PLAYER_GUESS.toPacket(
                        mapOf("guess" to guess)
                    )
                )
                GameViewModel.canGuess.value = false
            }
        }
        TabList(serverIp)
        ChatBox()
        if (showSettingsMenu.value)
            ShowGameMenu()
    }
}

@Composable
fun GuessingWheel(
    onNumberSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(fraction = 1f / 6f),
        color = Color(0xFF39424E)
    ) {
        val values = remember { (0..100).map { it.toString() } }
        val valuesPickerState = rememberPickerState()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Guess!",
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Picker(
                    state = valuesPickerState,
                    items = values,
                    visibleItemsCount = 3,
                    modifier = Modifier.weight(0.3f),
                    textModifier = Modifier.padding(8.dp),
                    textStyle = TextStyle(color = Color.White)
                )
            }

            ClickyButton(
                text = "Guess: ${valuesPickerState.selectedItem}",
                onClick = {
                    onNumberSelected(valuesPickerState.selectedItem.toInt())
                }
            )

        }

    }
}


@Composable
fun GameListTabbedPane(
    onStartNewGame: () -> Unit,
    onJoinGame: (GameRoomInfo) -> Unit
) {
    val tabItems = listOf("Available Games", "Leaderboard")
    var selectedTabIndex by remember { mutableStateOf(0) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(fraction = 9 / 10f)
                .fillMaxHeight(0.85f)
                .padding(16.dp)
        ) {
            androidx.compose.material.TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxWidth(fraction = 1 / 3f),
                backgroundColor = Color.Black.copy(alpha = 0.2f)
            ) {
                tabItems.forEachIndexed { index, title ->
                    Tab(
                        text = {
                            Row(
                            ) {
                                if (index == 0) {
                                    Icon(
                                        Icons.Outlined.Gamepad,
                                        "Games",
                                        tint = Color.White
                                    )
                                } else {
                                    Icon(
                                        Icons.Outlined.Leaderboard,
                                        "Dashboard",
                                        tint = Color.White
                                    )
                                }
                                Text(title, color = Color.White)
                            }
                        },
                        modifier = Modifier.background(
                            Color.Black.copy(alpha = 0.2f),
                            shape = if ((index == 0) or (index == tabItems.size)) {
                                //left upper corner round for index 0
                                if (index == 0) {
                                    RoundedCornerShape(5f, 0f, 0f, 0f)
                                } else {
                                    RoundedCornerShape(0f, 5f, 0f, 0f)
                                }
                            } else {
                                RoundedCornerShape(0f)
                            }

                        ),
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> GameList(onStartNewGame = onStartNewGame, onJoinGame = onJoinGame)
                1 -> {
                    LaunchedEffect(Unit){
                        Client.connectionManager
                            .sendPacket(PacketType.LEADERBOARD.toPacket())
                    }
                    LeaderBoard()
                }
            }
        }
    }
}



@Composable
fun GameList(
    onStartNewGame: () -> Unit,
    onJoinGame: (GameRoomInfo) -> Unit
) {
    val availableGames = remember {  LobbyViewModel.gameRooms }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                Color.Black.copy(alpha = 0.2f)
            )
            .padding(16.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(
                500.dp
            ),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableGames) { game ->
                GameItem(game = game, onJoinGame = onJoinGame)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        ClickyButton(
            text = "Start New Game",
            onClick = onStartNewGame,
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun GameItem(game: GameRoomInfo, onJoinGame: (GameRoomInfo) -> Unit) {
    val theme = androidx.compose.material.MaterialTheme
    Row(
        modifier = Modifier
            .clickable { onJoinGame(game) }
            .fillMaxWidth()
            .background(
                Color(0xFF39424E)
                    .copy(0.1f),
                RoundedCornerShape(5.dp)
            )
            .border(BorderStroke(0.2.dp, Color.Black.copy(0.2f)))
            .padding(vertical = 7.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
        ) {

            Row {
                Text(
                    text = game.roomName,
                    style = theme.typography.h6,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${game.roomPlayerCount} ",
                    style = theme.typography.body2,
                    color = Color.Magenta
                )
                Text(
                    text = "/",
                    style = theme.typography.body2,
                    color = Color.Black
                )
                Text(
                    text = " ${game.roomMaxPlayers}",
                    style = theme.typography.body2,
                    color = Color.Yellow
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = game.roomDescription,
                style = theme.typography.body2,
                color = Color.Magenta.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = game.roomStatus.name,
                style = theme.typography.body2,
                color = when (game.roomStatus) {
                    RoomStatus.NOT_STARTED -> Color.Green
                    RoomStatus.STARTING -> Color.Red
                    RoomStatus.STARTED -> Color.Yellow
                    RoomStatus.ENDED -> Color.Gray
                    else -> Color.White
                }
            )
        }
    }
}


@Composable
fun LeaderBoard(){
    val leaderboard = remember { LobbyViewModel.leaderboard }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                Color.Black.copy(alpha = 0.2f)
            )
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                leaderboard
            ) {
                LeaderBoardItem(it.first, it.second)
            }
        }
    }
}

@Composable
fun LeaderBoardItem(
    playerName: String,
    score: String
){
    val theme = androidx.compose.material.MaterialTheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF39424E)
                    .copy(0.1f),
                RoundedCornerShape(5.dp)
            )
            .border(BorderStroke(0.2.dp, Color.Black.copy(0.2f)))
            .padding(vertical = 7.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Row {
                Text(
                    text = playerName,
                    style = theme.typography.h6,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = score,
                    style = theme.typography.body2,
                    color = Color.Magenta
                )
            }
        }
    }
}
