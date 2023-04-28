
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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.net.Socket;

public class Client extends JFrame {
    // create basic list of songs
    private String songList[] = { "song1", "song2", "song3"};

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
    private JPanel song1 = new JPanel();
    private JPanel song2 = new JPanel();
    private JPanel song3 = new JPanel();
    

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

        // action listeners for switching cards
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

        // Code listens to mouse events on list of songs, if song is clicked it switches
        // to show that card
        ListSelectionListener listSelectionListener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent){
                String song = (String) musicList.getSelectedValue();
                CardLayout cl = (CardLayout) cards.getLayout();
                cl.show(cards, song);
            }
        };
        musicList.addListSelectionListener(listSelectionListener);

        // hardcoding 3 songs to test functionality need to create better way
        // to do this automatically to get rid of redundancy
        song1.add(new JLabel("Song 1"));
        song2.add(new JLabel("Song 2"));
        song3.add(new JLabel("Song 3"));

        // adding components to panels
        buttons.add(homeButton, BorderLayout.WEST);
        buttons.add(chatButton, BorderLayout.EAST);
        home.add(musicPanel, BorderLayout.CENTER);

        // chat functionality
        enteredText.setEditable(false);
        enteredText.setBackground(Color.LIGHT_GRAY);
        typedText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println("[" + screenName + "]: " + typedText.getText());
                typedText.setText("");
                typedText.requestFocusInWindow();
            }
        });

        // adding components to chat panel
        chat.add(new JScrollPane(enteredText), BorderLayout.CENTER);
        chat.add(typedText, BorderLayout.SOUTH);

        // cardlayout allowing to switch between panels
        CardLayout cl = new CardLayout();
        cards.setLayout(cl);

        // adding JPanels to cards
        cards.add(home, "home");
        cards.add(chat, "chat");
        cards.add(song1, "song1");
        cards.add(song2, "song2");
        cards.add(song3, "song3");

        // adding buttons panel and card container to main frame
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