# Pentago

The project comprises a fully functional implementation in Java of the board game "Pentago" in both client and server form. 

## Installation
Open IntelliJ IDEA and create a new Java project \
Set JDK version 11

## Usage

**1. Running and using the Server:** The server GameServer is located in package server, nested in package network. The server is started by RunGameServer.java, with the server prompting the user to input a port between 1 and 65355. If the port is outside the valid range or is already used, the user is prompted to input a different port. When accepted, the server prints the port that it is going to use and is ready to accept client connections. Command “quit” can be used to shut the server down.

**2. Running and using the Client via the TUI:** The Client is used via a TUI, located in TUI.java in package client, nested in package network. Upon start-up, the TUI prompts the user to input a server IP address, server port and chosen username. After that, the user is asked whether they want to play a game manually or if they want to have an AI computer player play instead of them. If the user wants an AI player to play instead of them, the TUI prompts the user to select an AI difficulty level. Next, the user is asked whether they want to enable the auto-queueing function. Afterwards, the user is always automatically queued the first time, regardless of the auto-queue setting. Refer to **Commands** for available commands.

**3. Playing a manual game:** After starting the client and connecting to a server per **Usage 2.**, the user is always queued up after start-up. If the first game is already finished and the auto-queue is not on, the user needs to use command “queue” to enter the game queue. Refer to **Commands** for more information. The user is shown a visual representation of the board with all currently placed Marbles and is also shown who’s turn it is via the messages “It’s your turn!” if it is the users turn or “It’s the opponent’s turn!” if it is the other player’s turn. The user needs to use the “move” command (see **Commands**) in order to perform a move. The user also has access to a hint from the in-built AI, accessible using command “hint” (see **Commands** for command usage). When the game has a winner, the final board situation is displayed alongside the winner’s name. If auto-queue is on, the user is automatically queued. If auto-queue is off, the user needs to use command “queue” to reenter the game queue.

**Playing a game using the AI:**
If the user has chosen the option for a computer player to play on their behalf during start-up no further intervention is required in order to have the bot play a game. If the user has chosen the option for a computer player to play on their behalf during start-up, AI play can be activated while not in a game using command “play ai”. See **Commands** for command usage. For the bot to play multiple games automatically, auto-queue must be enabled either during start-up or using command “autoqueue” (see **Commands**).  If both AI play and auto-queue are enabled the bot will automatically play and queue until the Enter key is pressed by the user. If the Enter key is pressed during a game, the AI player will finish the game before stopping. When stopped and the game ends, the user is returned to the server “lobby”.


## Commands
The TUI and client support the following commands after successful connection and start-up: \
**autoqueue** - Toggles automatically queueing after a game is finished. Command not usable while in-game. This option is given as a choice during start-up. \
**help** - Prints all commands.\
**queue** - Join the game queue. When used a second time the user leaves the queue. Not usable while in-game. \
**list** - Lists all online users.\
**rank** - Displays users ranked by wins.\
**move** <field> <rotation> - Places marble on the specified field and performs rotation. Only valid while in-game, and it is not the other player’s turn.\
**quit** - Disconnects from the server, quits the game.\
**send to** <username:> <message> - Sends a message to a specific user.\
**send everyone** <message> - Sends a message to every online user.\
**hint** <easy/hard> - Have the AI suggest an easy (random) or hard (intelligent) move while playing manually. Only usable while in-game.\
**ai play** <easy/hard> - Have the AI play the next games. This option is given as a choice during start-up.*