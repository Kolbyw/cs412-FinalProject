
/******************************************************************************
 *  Compilation:  javac ChatClient.java
 *  Execution:    java ChatClient name host
 *  Dependencies: In.java Out.java
 *
 *  Connects to host server on port 4444, enables an interactive
 *  chat client.
 *  
 *  % java ChatClient alice localhost
 *
 *  % java ChatClient bob localhost
 *  
 ******************************************************************************/

import java.awt.*;
import java.awt.event.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.net.Socket;

public class Client extends JFrame {
    // create basic list of songs
    private String songList[] = { "song1", "song2", "song3", "song4", "song5" };

    // GUI stuff
    // private JFrame frame = new JFrame("Music Streamer");
    private JPanel cards = new JPanel();
    private JPanel buttons = new JPanel();
    private JTextArea enteredText = new JTextArea(10, 32);
    private JTextField typedText = new JTextField(32);
    private JList musicList = new JList(songList);
    private JScrollPane musicPanel = new JScrollPane(musicList);
    private JButton chatButton = new JButton("Chat", null);
    private JButton homeButton = new JButton("Home", null);
    

    // private JTextArea enteredText = new JTextArea(10, 32);
    // private JTextField typedText = new JTextField(32);

    // socket for connection to chat server
    public Socket socket;

    // for writing to and reading from the server
    public Out out;
    public In in;

    // screen name
    public String screenName;

    public Client(String screenName, String hostName) {
        this.screenName = screenName;

        // connect to server
        try {
            socket = new Socket(hostName, 4444);
            out = new Out(socket);
            in = new In(socket);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // close output stream - this will cause listen() to stop and exit
        addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        out.close();
                        in.close();
                        try {
                            socket.close();
                        } catch (Exception ioe) {
                            ioe.printStackTrace();
                        }
                    }
                });

        // create GUI stuff
        setTitle("Music Streamer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(400, 300));
        setLayout(new BorderLayout());
        pack();

        JPanel home = new JPanel(new BorderLayout());
        JPanel chat = new JPanel();

        musicList.setLayoutOrientation(JList.VERTICAL);
        // musicList.setVisibleRowCount(3);

        homeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout) cards.getLayout();
                cl.show(cards, "home");
            }
        });
        chatButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout) cards.getLayout();
                cl.show(cards, "chat");
            }
        });

        buttons.add(homeButton, BorderLayout.WEST);
        buttons.add(chatButton, BorderLayout.EAST);
        home.add(musicPanel, BorderLayout.CENTER);

        enteredText.setEditable(false);
        enteredText.setBackground(Color.LIGHT_GRAY);
        typedText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println("[" + screenName + "]: " + typedText.getText());
                typedText.setText("");
                typedText.requestFocusInWindow();
            }
        });

        chat.add(new JScrollPane(enteredText), BorderLayout.CENTER);
        chat.add(typedText, BorderLayout.SOUTH);

        // display the window, with focus on typing box
        // typedText.requestFocusInWindow();
        CardLayout cl = new CardLayout();
        cards.setLayout(cl);

        cards.add(home, "home");
        cards.add(chat, "chat");

        add(buttons, BorderLayout.NORTH);
        add(cards);
        setVisible(true);
    }

    // listen to socket and print everything that server broadcasts
    public void listen() {
        String s;
        while ((s = in.readLine()) != null) {
            enteredText.insert(s + "\n", enteredText.getText().length());
            enteredText.setCaretPosition(enteredText.getText().length());
        }
        out.close();
        in.close();
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println("Closed client socket");
    }

    public static void main(String[] args) {
        Client client = new Client(args[0], args[1]);
        client.listen();
    }
}