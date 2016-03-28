# Connect-Four
A Multiplayer GUI Connect Four game made in JAVA, Eclipse IDE. It displays the opponent moves with red colour, and your own moves with blue colour.
PNG images of yellow coin, red coin, board are already stored in the res folder. It is loaded into the memory from there.
How the program works,
The class Connect Four implements Runnable as it runs a thread and requires constant update and sync with the other instances running. Most if not all variables are declared as private as to not accidentally be used by other classes where it may cause a conflict. A string array called spaces[6][7] is used to store the board's state.
	The main method (which is automatically run first).
It calls upon an UI method known as initUI. This method shows the first GUI where the IP address and port numbers are entered.

	initUI()
Two textboxes, two labels and one button. The button has an onClickListener attached to it. Once button is clicked, it makes sure that the IP field isn't empty and the ports are in range of 1<port<65535. As anything other than that is an invalid port. If it doesn't match a message box is displayed and the user can change the input. Once the data has been input correctly it calls upon the method loaded.

	loaded()
This method calls the loadImages(), sets the Painter(), and creates a new window. It checks if a server is already running on the given IP and port, if not then it initializes a server and waits for a client displaying the same.

	loadImages()
It loads the image files into a variable.

	Painter, This is a class.
It creates and sets the UI for the user to play in.

	connect()
This is the method which attempts to connect the server.

	initializeServer()
This method creates a server in case it fails to connect to one. IMPORTANT NOTE: This method will not proceed until a connection has been established. So in theory it can even cause the program to wait forever to get a connection. This is due to the ServerSocket that is called.

	listenForServerRequest()
It waits for requests from other clients and automatically accepts them.

	run()
Since this is a thread this method is constantly called. It resets the window and redraws the images.

	tick()
This method is called upon by run(), it checks for any errors. In case the error count is more than 10 it immediately displays the message "Unable to communicate with opponent". It calls upon the method checkForEnemyWin() and checkForTie(). If they return false then the current player is allowed to take their turn.

	render(Graphics g)
It displays all the images and board figures onto the screen. By checking what moves were made and hence updating the same. If there is a win then a line is drawn showing the win. If a tie then a message is displayed.

	checkForWin()
It checks if the current player has won the game. By using checkWin to send the current player's colour.

	checkForEnemyWin()
It checks if the opponent has won the game. By using checkWin to send the enemy's player colour.

	checkForTie()
It checks if all the spaces are filled. Since the win combinations are already check this is not done again here.

	checkWin(String player)
This returns true if there is a win along with storing the first and fourth coin which caused the win so that a line can be drawn. It has four loops, One to check a vertical win, horizontal win, diagonal down win and diagonal up win.