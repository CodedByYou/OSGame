package me.codedbyyou.os.game.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.software.project.ui.controller.NavigationController
import kotlinx.coroutines.*
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.core.interfaces.server.toPacket
import me.codedbyyou.os.game.client.Client
import me.codedbyyou.os.game.data.AppSessionData
import me.codedbyyou.os.game.data.Server
import me.codedbyyou.os.game.data.ServersViewModel
import me.codedbyyou.os.game.data.User
import me.codedbyyou.os.game.ui.components.ClickyButton
import me.codedbyyou.os.game.utils.ping


@Composable
fun ServerView(sharedData: Map<String, Any>) {
    val serverList = remember { mutableStateListOf<Server>() }
    val selectedServer = remember { mutableStateOf<Server?>(null) }
    var joinDelay = 0L
    fun onServerSelected(server: Server) {
        selectedServer.value = server
    }

    fun onAddServer() {
        NavigationController.goTo("addServer")
    }

    fun onEditServer() {
        selectedServer.value?.let {
            NavigationController.goTo("editServer", mapOf("server" to it))
        }
    }

    fun onJoinServer() {
        if (joinDelay > System.currentTimeMillis()) return
        Client.connectionManager.connectTo(selectedServer.value!!)
        GlobalScope.launch {
            delay(500)
            if (selectedServer.value!!.psuedoName != null) {
                Client.connectionManager.sendPacket(
                    PacketType.SERVER_AUTH.toPacket(
                        mapOf(
                            "nickTicket" to selectedServer.value!!.psuedoName + "#" + selectedServer.value!!.ticket,
                            "macAddress" to Client.macAddress!!
                        )
                    )
                )
                Client.user = User(
                    selectedServer.value!!.psuedoName!!,
                    selectedServer.value!!.ticket!!
                )
            } else {
                NavigationController.goTo("registerToServer", mapOf("server" to selectedServer.value!!))
            }

            joinDelay = System.currentTimeMillis() + 5000L
        }
    }

    fun onRefreshServers() {
        GlobalScope.launch {
            ServersViewModel.servers.forEach { server ->
                withContext(Dispatchers.Default) {
                    server.ping()
                    serverList.clear()
                    serverList.addAll(ServersViewModel.servers)
                }

            }
        }
    }

    fun onBack() {
        NavigationController.goBack()
    }

    // Initial server refresh
    LaunchedEffect(Unit) {
        serverList.clear()
        serverList.addAll(ServersViewModel.servers)
        onRefreshServers()
    }

    ServerListScreen(
        servers = serverList,
        selectedServer = selectedServer.value,
        onServerSelected = ::onServerSelected,
        onAddServer = { onAddServer() },
        onEditServer = { onEditServer() },
        onJoinServer = { onJoinServer() },
        onRefreshServers = { onRefreshServers() },
        onBack = { onBack() }
    )
}


@Composable
fun ServerListScreen(
    servers: List<Server>,
    selectedServer: Server?,
    onServerSelected: (Server) -> Unit,
    onAddServer: () -> Unit,
    onEditServer: () -> Unit,
    onJoinServer: () -> Unit,
    onRefreshServers: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Surface(
            color = Color(0xFF39424E),
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Server List",
                    color = Color.White,
                    textDecoration = TextDecoration.None,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Name",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "IP Address",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Online Players",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "Description",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "Status",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if(servers.isEmpty()){
                        item {
                            Text(
                                text = "No servers found",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }else {
                        items(servers) { server ->
                            ServerRow(
                                server = server,
                                isSelected = selectedServer == server,
                                onServerSelected = { onServerSelected(server) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClickyButton(
                        "Add Server",
                        modifier = Modifier.weight(1f),
                        onClick = onAddServer
                    )

                    ClickyButton(
                        "Delete Server",
                        modifier = Modifier.weight(1f),
                        onClick = onEditServer,
                        enabled = selectedServer != null
                    )


                    ClickyButton(
                        "Edit Server",
                        modifier = Modifier.weight(1f),
                        onClick = onEditServer,
                        enabled = selectedServer != null
                    )

                    ClickyButton(
                        "Join Server",
                        modifier = Modifier.weight(1f),
                        onClick = onJoinServer,
                        enabled = selectedServer != null
                    )

                    ClickyButton(
                        "Refresh",
                        modifier = Modifier.weight(1f),
                        onClick = onRefreshServers
                    )

                    ClickyButton(
                        "Back",
                        modifier = Modifier.weight(1f),
                        onClick = onBack
                    )
                }
            }
        }
    }
}

@Composable
fun ServerRow(server: Server, isSelected: Boolean, onServerSelected: () -> Unit) {
    if(server.name == null){
        Row(
            modifier = Modifier
                .clickable { onServerSelected()}
                .fillMaxWidth()
                .background(Color(0xFF39424E))
                .border(BorderStroke(1.dp, Color.Transparent))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Unknown Server", color = Color.White, modifier = Modifier.weight(1f))
            Text(text = "[IP HIDDEN]", color = Color.White, modifier = Modifier.weight(1f))
            Text(text = "Unknown", color = Color.White, modifier = Modifier.weight(1f))
            Text(text = "Unknown", color = Color.White, modifier = Modifier.weight(1f))
            Text(text = "Offline", color = Color.Red, modifier = Modifier.weight(1f))
        }
        return
    }

    val backgroundColor = if (isSelected) Color(0xFF333b46) else Color(0xFF39424E)
    val borderColor = if (isSelected) Color.White else Color.Transparent

    Row(
        modifier = Modifier
            .clickable { onServerSelected()}
            .fillMaxWidth()
            .background(backgroundColor)
            .border(BorderStroke(1.dp, borderColor))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = server.name!!, color = Color.White, modifier = Modifier.weight(1f))
        Text(text = server.ip, color = Color.White, modifier = Modifier.weight(1f))
        Text(text = "${server.onlinePlayers}/${server.maxPlayers}", color = Color.White, modifier = Modifier.weight(1f))
        Text(text = server.description!!, color = Color.White, modifier = Modifier.weight(1f))
        Text(text = server.status!!, color = Color.Magenta, modifier = Modifier.weight(1f))
    }
}


@Composable
fun AddServerScreen(onAddServer: (String, String) -> Unit, onBack: () -> Unit) {
    ServerForm(
        title = "Add Server",
        initialIp = "",
        initialPort = "",
        onSubmit = onAddServer,
        onBack = onBack
    )
}

@Composable
fun EditServerScreen(
    server: Server,
    onEditServer: (String, String, String) -> Unit,
    onBack: () -> Unit
) {
    ServerForm(
        title = "Edit Server",
        initialIp = server.ip,
        initialPort = server.port.toString(),
        onSubmit = {ip, port -> onEditServer(server.ip, ip, port)},
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerForm(
    title: String,
    initialIp: String,
    initialPort: String,
    onSubmit: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var serverIp by remember { mutableStateOf(initialIp) }
    var serverPort by remember { mutableStateOf(initialPort) }

    val textfieldColor = TextFieldDefaults.outlinedTextFieldColors(
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
        focusedLabelColor = Color.White,
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Surface(
            color = Color(0xFF39424E),
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.fillMaxWidth(
                fraction = 1f / 3
            )
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
                    text = title,
                    color = Color.White,
                    textDecoration = TextDecoration.None,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                OutlinedTextField(
                    value = serverIp,
                    onValueChange = { serverIp = it },
                    label = { Text("Server IP") },
                    colors = textfieldColor,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = serverPort,
                    onValueChange = { serverPort = it },
                    label = { Text("Server Port") },
                    colors = textfieldColor,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClickyButton(
                        text = "Submit",
                        modifier = Modifier.weight(1f),
                        onClick = { onSubmit(serverIp, serverPort) }
                    )
                    ClickyButton(
                        text = "Back",
                        modifier = Modifier.weight(1f),
                        onClick = onBack
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerRegistrationForm(
    title: String,
    initialUsername: String,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit
) {
    val errorMessage = remember { AppSessionData.registerErrorMessage.value }
    var username by remember { mutableStateOf(initialUsername) }

    val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
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
        focusedLabelColor = Color.White,
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Surface(
            color = Color(0xFF39424E),
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.fillMaxWidth(
                fraction = 1f / 3
            )
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
                    text = title,
                    color = Color.White,
                    textDecoration = TextDecoration.None,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClickyButton(
                        text = "Submit",
                        modifier = Modifier.weight(1f),
                        enabled = username.isNotEmpty(),
                        onClick = {
                            onSubmit(username)
                        }
                    )
                    ClickyButton(
                        text = "Back",
                        modifier = Modifier.weight(1f),
                        onClick = onBack
                    )
                }
            }
        }
    }
}