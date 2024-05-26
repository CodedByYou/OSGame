package me.codedbyyou.os.game.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.core.interfaces.server.toPacket
import me.codedbyyou.os.game.client.Client
import me.codedbyyou.os.game.data.ChatViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBox(){
    var textMessage by rememberSaveable { mutableStateOf("") }
    val chatMessages = ChatViewModel.chatMessages
    val listState = rememberLazyListState()
    val playerName = Client.user!!.nickTicket

    LaunchedEffect(chatMessages.size) {
        listState.scrollToItem(chatMessages.lastIndex);
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(fraction = 1 / 3f).align(
                Alignment.BottomStart
            )
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(
                        1/3f
                    )
                    .fillMaxHeight(fraction = 0.8f)
                    .padding(8.dp),
                state = listState
            ) {
                items(chatMessages) { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            TextField(
                value = textMessage,
                onValueChange = { textMessage = it },
                label = { Text("Message") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    containerColor = Color(0xFF39424E),
                    disabledTextColor = Color.Gray,
                    disabledBorderColor = Color.Gray,
                    disabledPlaceholderColor = Color.Gray,
                    disabledLeadingIconColor = Color.Gray,
                    disabledTrailingIconColor = Color.Gray,
                    errorCursorColor = Color.Red,
                    errorTextColor = Color.Red,
                    errorPlaceholderColor = Color.Red,
                    errorLeadingIconColor = Color.Red,
                    errorTrailingIconColor = Color.Red,
                    selectionColors = TextSelectionColors(
                        Color.White,
                        Color(0xFF333b46)
                    ),
                    focusedLabelColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .onKeyEvent {
                        if (it.key in listOf(Key.Enter, Key.NumPadEnter) && it.type == KeyEventType.KeyUp) {
                            ChatViewModel.addMessage("[${playerName}] ${textMessage.removeSuffix("\n")}")
                            if(Client.connectionManager.isConnected()){
                                Client.connectionManager.sendPacket(PacketType.MESSAGE.toPacket(mapOf("message" to textMessage)))
                            }
                            textMessage = ""
                            true
                        } else {
                            false
                        }
                    }
            )
        }
    }
}