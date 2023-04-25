
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
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.net.Socket;

public class Client extends JFrame implements ActionListener {
    // create basic list of songs
    private String songList[] = { "song1", "song2", "song3", "song4", "song5" };

    // GUI stuff
    private JFrame frame = new JFrame("Music Streamer");
    private JList musicList = new JList(songList);
    private JScrollPane musicPanel = new JScrollPane(musicList);

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
        frame.setLayout(new BorderLayout());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400, 300));
        frame.pack();
        
        musicList.setLayoutOrientation(JList.VERTICAL);
        musicList.setVisibleRowCount(-1);

        frame.add(musicPanel);

        frame.setVisible(true);

        // enteredText.setEditable(false);
        // enteredText.setBackground(Color.LIGHT_GRAY);
        // typedText.addActionListener(this);

        // Container content = getContentPane();
        // content.add(new JScrollPane(enteredText), BorderLayout.CENTER);
        // content.add(typedText, BorderLayout.SOUTH);

        // display the window, with focus on typing box
        // typedText.requestFocusInWindow();
        frame.setVisible(true);

    }

    // process TextField after user hits Enter
    public void actionPerformed(ActionEvent e) {
        // out.println("[" + screenName + "]: " + typedText.getText());
        // typedText.setText("");
        // typedText.requestFocusInWindow();
    }

    // listen to socket and print everything that server broadcasts
    // public void listen() {
    // String s;
    // while ((s = in.readLine()) != null) {
    // enteredText.insert(s + "\n", enteredText.getText().length());
    // enteredText.setCaretPosition(enteredText.getText().length());
    // }
    // out.close();
    // in.close();
    // try { socket.close(); }
    // catch (Exception e) { e.printStackTrace(); }
    // System.err.println("Closed client socket");
    // }

    public static void main(String[] args) {
        Client client = new Client(args[0], args[1]);
        // client.listen();
    }
}