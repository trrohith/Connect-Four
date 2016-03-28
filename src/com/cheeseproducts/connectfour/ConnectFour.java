package com.cheeseproducts.connectfour;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ConnectFour implements Runnable {

	private String ip = "localhost";
	private int port = 22222;
	private Scanner scanner = new Scanner(System.in);
	private JFrame frame;
	private final int WIDTH = 436;
	private final int HEIGHT = 397;
	private Thread thread;

	private Painter painter;
	private Socket socket;
	private DataOutputStream dos;
	private DataInputStream dis;

	private ServerSocket serverSocket;

	private BufferedImage board;
	private BufferedImage red;
	private BufferedImage yellow;

	private String[][] spaces = new String[6][7];

	private boolean yourTurn = false;
	private boolean Yellow = true;
	private boolean accepted = false;
	private boolean unableToCommunicateWithOpponent = false;
	private boolean won = false;
	private boolean enemyWon = false;
	private boolean tie = false;

	private int lengthOfSpace = 50;
	private int errors = 0;
	private int x1 = -1;
	private int y1 = -1;
	private int x2 = -1;
	private int y2 = -1;

	private Font font = new Font("Verdana", Font.BOLD, 28);
	private Font smallerFont = new Font("Verdana", Font.BOLD, 20);
	private Font largerFont = new Font("Verdana", Font.BOLD, 38);

	private String waitingString = "Waiting for another player";
	private String unableToCommunicateWithOpponentString = "Unable to communicate with opponent.";
	private String wonString = "You won!";
	private String enemyWonString = "Opponent won!";
	private String tieString = "Game ended in a tie.";

	/**
	 * <pre>
	 *00 01 02 03 04 05 06 
	 *10 11 12 13 14 15 16 
	 *20 21 22 23 24 25 26 
	 *30 31 32 33 34 35 36 
	 *40 41 42 43 44 45 46 
	 *50 51 52 53 54 55 56
	 * </pre>
	 */

	JButton b1 = new JButton("Done!");
	JButton b2 = new JButton("Play Again!");
	JTextField ipField = new JTextField(20);
	JTextField portField = new JTextField(20);

	protected void initUI() throws MalformedURLException {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Connect Four Made by T.R.Rohith");
		frame.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel(new GridBagLayout());
		JPanel panel = new JPanel(new GridLayout(0, 2));
		panel.add(new JLabel("IP: "));

		panel.add(ipField);
		panel.add(new JLabel("Port: "));

		panel.add(portField);

		b1.setVerticalTextPosition(AbstractButton.CENTER);
		b1.setHorizontalTextPosition(AbstractButton.LEADING);
		b1.setMnemonic(KeyEvent.VK_D);
		b1.setActionCommand("done");
		panel.add(b1);
		panel.setBorder(BorderFactory.createTitledBorder("Enter the details"));
		mainPanel.add(panel);
		frame.add(mainPanel);
		frame.pack();
		frame.setVisible(true);

		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String temp = ipField.getText();
				while (temp == null) {
					JOptionPane.showMessageDialog(frame, "IP is empty");
					return;
				}
				ip = temp;

				temp = portField.getText();
				port = Integer.parseInt(temp);
				while (port < 1 || port > 65535) {
					JOptionPane.showMessageDialog(frame, "Port is out of range try again");
					return;
				}

				frame.setVisible(false);
				loaded();
			}
		});

	}

	public ConnectFour() {

	}

	public void loaded() {
		loadImages();

		painter = new Painter();
		painter.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		if (!connect())
			initializeServer();

		frame = new JFrame();
		frame.setTitle("Connect Four");
		frame.setContentPane(painter);
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);

		thread = new Thread(this, "ConnectFour");
		thread.start();
	}

	public void run() {
		while (true) {
			tick();
			painter.repaint();

			if (!Yellow && !accepted) {
				listenForServerRequest();
			}
		}
	}

	private void render(Graphics g) {
		g.drawImage(board, 0, 0, null);
		if (unableToCommunicateWithOpponent) {
			g.setColor(Color.RED);
			g.setFont(smallerFont);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth(unableToCommunicateWithOpponentString);
			g.drawString(unableToCommunicateWithOpponentString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
			return;
		}

		if (accepted) {
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 7; j++) {
					if (spaces[i][j] != null) {
						if (spaces[i][j].equals("red")) {
							if (Yellow) {
								g.drawImage(red, 10 + (j * 60), 10 + (i * 60), null);
							} else {
								g.drawImage(yellow, 10 + (j * 60), 10 + (i * 60), null);
							}
						} else if (spaces[i][j].equals("yellow")) {
							if (Yellow) {
								g.drawImage(yellow, 10 + (j * 60), 10 + (i * 60), null);
							} else {
								g.drawImage(red, 10 + (j * 60), 10 + (i * 60), null);
							}
						}
					}
				}
			}
			if (won || enemyWon) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(10));
				g.setColor(Color.BLACK);
				g.drawLine(35 + (x1 * 60), 35 + (y1 * 60), 35 + (x2 * 60), 35 + (y2 * 60));

				g.setColor(Color.RED);
				g.setFont(largerFont);
				if (won) {
					int stringWidth = g2.getFontMetrics().stringWidth(wonString);
					g.drawString(wonString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);

				} else if (enemyWon) {
					int stringWidth = g2.getFontMetrics().stringWidth(enemyWonString);
					g.drawString(enemyWonString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);

				}
			}
			if (tie) {
				Graphics2D g2 = (Graphics2D) g;
				g.setColor(Color.BLACK);
				g.setFont(largerFont);
				int stringWidth = g2.getFontMetrics().stringWidth(tieString);
				g.drawString(tieString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);

			}
		} else {
			g.setColor(Color.RED);
			g.setFont(font);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth(waitingString);
			g.drawString(waitingString, WIDTH / 2 - (stringWidth / 2), HEIGHT / 2);
		}

	}

	private void tick() {
		if (errors >= 10)
			unableToCommunicateWithOpponent = true;

		if (!yourTurn && !unableToCommunicateWithOpponent) {
			try {
				int x = dis.readInt();
				for (int i = 5; i >= 0; i--) {
					if (spaces[i][x] == null) {
						if (Yellow) {
							spaces[i][x] = "red";
							yourTurn = true;
							break;
						} else {
							spaces[i][x] = "yellow";
							yourTurn = true;
							break;
						}
					}
				}
				checkForEnemyWin();
				checkForTie();
				yourTurn = true;
			} catch (IOException e) {
				e.printStackTrace();
				errors++;
			}
		}
	}

	private void checkForWin() {
		if (Yellow) {
			if (checkWin("yellow")) {
				won = true;
			}
		} else {
			if (checkWin("red")) {
				won = true;
			}
		}
	}

	private void checkForEnemyWin() {
		if (!Yellow) {
			if (checkWin("yellow")) {
				enemyWon = true;
			}
		} else {
			if (checkWin("red")) {
				enemyWon = true;
			}
		}
	}

	private void checkForTie() {
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 7; j++) {
				if (spaces[i][j] == null) {
					return;
				}
			}
		}
		tie = true;
	}

	private void listenForServerRequest() {
		Socket socket = null;
		try {
			socket = serverSocket.accept();
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			accepted = true;
			System.out.println("CLIENT HAS REQUESTED TO JOIN, AND WE HAVE ACCEPTED");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean connect() {
		try {
			socket = new Socket(ip, port);
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			accepted = true;
		} catch (IOException e) {
			System.out.println("Unable to connect to the address: " + ip + ":" + port + " | Starting a server");
			return false;
		}
		System.out.println("Successfully connected to the server.");
		return true;
	}

	private void initializeServer() {
		try {
			serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
		} catch (Exception e) {
			e.printStackTrace();
		}
		yourTurn = true;
		Yellow = false;
	}

	private void loadImages() {
		try {
			board = ImageIO.read(getClass().getResourceAsStream("/board.png"));
			red = ImageIO.read(getClass().getResourceAsStream("/red.png"));
			yellow = ImageIO.read(getClass().getResourceAsStream("/yellow.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					new ConnectFour().initUI();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private boolean checkWin(String player) {
		// Vertical Checking
		for (int col = 0; col < 7; col++) {
			for (int row = 0; row < 3; row++) {
				if (spaces[row][col] == player) {
					if (spaces[row + 1][col] == player) {
						if (spaces[row + 2][col] == player) {
							if (spaces[row + 3][col] == player) {
								x1 = col;
								y1 = row;
								x2 = col;
								y2 = row + 3;
								return true;
							}
						}
					}
				}
			}
		}

		// Horizontal Checking
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 4; col++) {
				if (spaces[row][col] == player) {
					if (spaces[row][col + 1] == player) {
						if (spaces[row][col + 2] == player) {
							if (spaces[row][col + 3] == player) {
								x1 = col;
								y1 = row;
								x2 = col + 3;
								y2 = row;
								return true;
							}
						}
					}
				}
			}
		}

		// Diagonal Down Checking
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 4; col++) {
				if (spaces[row][col] == player) {
					if (spaces[row + 1][col + 1] == player) {
						if (spaces[row + 2][col + 2] == player) {
							if (spaces[row + 3][col + 3] == player) {
								x1 = col;
								y1 = row;
								x2 = col + 3;
								y2 = row + 3;
								return true;
							}
						}
					}
				}
			}
		}

		// Diagonal Up Checking
		for (int row = 0; row < 3; row++) {
			for (int col = 3; col < 7; col++) {
				if (spaces[row][col] == player) {
					if (spaces[row + 1][col - 1] == player) {
						if (spaces[row + 2][col - 2] == player) {
							if (spaces[row + 3][col - 3] == player) {
								x1 = col;
								y1 = row;
								x2 = col - 3;
								y2 = row + 3;
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	private class Painter extends JPanel implements MouseListener {
		private static final long serialVersionUID = 1L;

		public Painter() {
			setFocusable(true);
			requestFocus();
			setBackground(Color.WHITE);
			addMouseListener(this);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			render(g);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (accepted) {
				if (yourTurn && !unableToCommunicateWithOpponent && !won && !enemyWon) {
					int x = e.getX() / (lengthOfSpace + 10);

					for (int i = 5; i >= 0; i--) {

						if (spaces[i][x] == null) {
							if (!Yellow) {
								spaces[i][x] = "red";
								yourTurn = false;
								yourTurn = false;
								repaint();
								Toolkit.getDefaultToolkit().sync();

								try {
									dos.writeInt(x);
									dos.flush();
								} catch (IOException e1) {
									errors++;
									e1.printStackTrace();
								}

								System.out.println("DATA WAS SENT");
								checkForWin();
								checkForTie();
								break;
							} else {
								spaces[i][x] = "yellow";
								yourTurn = false;
								yourTurn = false;
								repaint();
								Toolkit.getDefaultToolkit().sync();

								try {
									dos.writeInt(x);
									dos.flush();
								} catch (IOException e1) {
									errors++;
									e1.printStackTrace();
								}

								System.out.println("DATA WAS SENT");
								checkForWin();
								checkForTie();
								break;
							}

						}
					}

				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {

		}

	}

}
