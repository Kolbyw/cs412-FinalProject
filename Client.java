
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
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
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
import javax.swing.filechooser.FileFilter;

import java.net.Socket;

@SuppressWarnings({ "serial", "unused" })
public class Client extends JFrame {
    // create basic list of songs
    private String songList[] = { "song1", "song2", "song3", "song4", "song5" };

    // GUI stuff
    // private JFrame frame = new JFrame("Music Streamer");
    private JPanel cards = new JPanel();
    private JPanel buttons = new JPanel();
    private JTextArea enteredText = new JTextArea(10, 32);
    private JTextField typedText = new JTextField(32);
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private JList musicList = new JList(songList);
    private JScrollPane musicPanel = new JScrollPane(musicList);
    private JFileChooser fileChooser;
    private JButton chatButton = new JButton("Chat", null);
    private JButton homeButton = new JButton("Home", null);
    private JButton uploadButton = new JButton("Upload", null);
    private JPanel song1 = new JPanel();
    private JPanel song2 = new JPanel();
    private JPanel song3 = new JPanel();
    private JPanel song4 = new JPanel();
    private JPanel song5 = new JPanel();


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
            socket = new Socket(hostName, 2000);
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
        typedText.requestFocusInWindow();

        JPanel home = new JPanel(new BorderLayout());
        JPanel chat = new JPanel();
        
        //to choose mp3 files only
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".mp3") || f.isDirectory();
            }
            public String getDescription() {
                return "MP3 files (*.mp3)";
            }
        });
        

        musicList.setLayoutOrientation(JList.VERTICAL);
        // musicList.setVisibleRowCount(3);

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
        JFileChooser fileChooser = new JFileChooser();
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileFilter() {
                    public String getDescription() {
                        return "MP3 Files (*.mp3)";
                    }

                    public boolean accept(File file) {
                        if (file.isDirectory()) {
                            return true;
                        } else {
                            String filename = file.getName().toLowerCase();
                            return filename.endsWith(".mp3");
                        }
                    }
                });
                int result = fileChooser.showOpenDialog(cards);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    // Do something with the selected file
                    System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                }
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

        // hardcoding 5 songs to test functionality need to create better way
        // to do this automatically to get rid of redundancy
        song1.add(new JLabel("Song 1"));
        song2.add(new JLabel("Song 2"));
        song3.add(new JLabel("Song 3"));
        song4.add(new JLabel("Song 4"));
        song5.add(new JLabel("Song 5"));

        // adding components to panels
        buttons.add(homeButton, BorderLayout.WEST);
        buttons.add(chatButton, BorderLayout.EAST);
        buttons.add(uploadButton, BorderLayout.EAST);
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

        // display the window, with focus on typing box
        // typedText.requestFocusInWindow();
        // cardlayout allowing to switch between panels
        CardLayout cl = new CardLayout();
        cards.setLayout(cl);

        // adding JPanels to cards
        cards.add(home, "home");
        cards.add(chat, "chat");
        cards.add(song1, "song1");
        cards.add(song2, "song2");
        cards.add(song3, "song3");
        cards.add(song4, "song4");
        cards.add(song5, "song5");

        // adding buttons panel and card container to main frame
        add(buttons, BorderLayout.NORTH);
        add(cards);
        setVisible(true);
    }
    
    // listen to socket and print everything that server broadcasts
    public void listen() {
        String s;
        while ((s = in.readLine()) != null) {
            enteredText.insert(s + "\n", enteredText.getCaretPosition());
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
