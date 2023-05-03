import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.*;

import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.multi.MultiScrollBarUI;

import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

@SuppressWarnings({ "serial", "unused" })
public class Client extends JFrame {
    // GUI stuff
    // private JFrame frame = new JFrame("Music Streamer");
    private JPanel cards = new JPanel();
    private JPanel buttons = new JPanel();
    private JPanel musicControls = new JPanel();
    private JTextArea enteredText = new JTextArea(10, 32);
    private JTextField typedText = new JTextField(32);
    private JFileChooser fileChooser;
    private JButton chatButton = new JButton("Chat", null);
    private JButton homeButton = new JButton("Home", null);
    private JButton uploadButton = new JButton("Upload", null);

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
        setPreferredSize(new Dimension(400, 320));
        setLayout(new BorderLayout());
        pack();
        typedText.requestFocusInWindow();

        JPanel home = new JPanel(new BorderLayout());
        JPanel chat = new JPanel();

        // music controls
        musicControls.setVisible(false);
        Icon pauseIcon = new ImageIcon("icons\\pause-button.png");
        JButton pauseButton = new JButton(pauseIcon);
        Icon playIcon = new ImageIcon("icons\\play-button.png");
        JButton playButton = new JButton(playIcon);
        
        JList musicLists = new JList();
        loadSongList(musicLists);
        JScrollPane musicPanel = new JScrollPane(musicLists);

        JButton refreshList = new JButton("Refresh Song List");

        musicLists.setLayoutOrientation(JList.VERTICAL);
        
        // action listeners
        refreshList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                loadSongList(musicLists);
            }
        });

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
                    Path filePath = Paths.get(selectedFile.getAbsolutePath());
                    // Do something with the selected file
                    System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                    
                    byte[] mp3Bytes = null;
					try {
						mp3Bytes = Files.readAllBytes(filePath);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
                    System.out.println("MP3 file size: " + mp3Bytes.length + " bytes");
                    String filename_temp = selectedFile.getAbsolutePath();
                    String str = "This is a sentence.";
                    
                    String newString = filename_temp.replace("\\", "@@");
                    System.out.println(newString);
                    
                    String[] words = newString.split("@@",5);
                    System.out.println(words[4]);
                    String serverFolder = ("C:\\Users\\kolby\\Documents\\cs412-FinalProject\\songs\\"+(words[4]));
                    try {
						copyDirectory(selectedFile.getAbsolutePath(),serverFolder);
                        loadSongList(musicLists);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                    
                	
                }
            }
        });
        

        // Code listens to mouse events on list of songs, if song is clicked it switches
        // to show that card
        MP3 mp3 = new MP3(null);
        ListSelectionListener listSelectionListener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                String song = "C:\\Users\\kolby\\Documents\\cs412-FinalProject\\songs\\" + (String) musicLists.getSelectedValue() + ".mp3";
                mp3.changeSong(song);
                mp3.play();

                musicControls.setVisible(true);
                playButton.setEnabled(false);
                pauseButton.setEnabled(true);
            }
        };
        listFiles();
        musicLists.addListSelectionListener(listSelectionListener);

        // music control action listeners
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mp3.play();
                pauseButton.setEnabled(true);
                playButton.setEnabled(false);
            }
        });
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mp3.stop();
                playButton.setEnabled(true);
                pauseButton.setEnabled(false);
            }
        });

        // adding components to panels
        buttons.add(homeButton, BorderLayout.WEST);
        buttons.add(chatButton, BorderLayout.EAST);
        buttons.add(uploadButton, BorderLayout.EAST);
        home.add(musicPanel, BorderLayout.CENTER);
        home.add(refreshList, BorderLayout.NORTH);
        musicControls.add(playButton, BorderLayout.CENTER);
        musicControls.add(pauseButton, BorderLayout.CENTER);

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

        // adding buttons panel and card container to main frame
        add(buttons, BorderLayout.NORTH);
        add(cards, BorderLayout.CENTER);
        add(musicControls, BorderLayout.SOUTH);
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
    
    //Method for taking the selected file from the upload and moving it to the server's directory
    public void copyDirectory(String sourceLocation , String targetLocation)
    	    throws IOException {

    	File sourceFile = new File(sourceLocation);
    	File destinationFile = new File(targetLocation);

    	FileInputStream fileInputStream = new FileInputStream(sourceFile);
    	FileOutputStream fileOutputStream = new FileOutputStream(
    	                destinationFile);

    	int bufferSize;
    	byte[] buffer = new byte[512];
    	while ((bufferSize = fileInputStream.read(buffer)) > 0) {
    	    fileOutputStream.write(buffer, 0, bufferSize);
    	}
    	fileInputStream.close();
    	fileOutputStream.close();
    	    }
    
     public ArrayList<String> listFiles()
     {
    	// Creates an array in which we will store the names of files and directories
         String[] pathnames;

         // Creates a new File instance by converting the given pathname string
         // into an abstract pathname
         File f = new File("C:\\Users\\kolby\\Documents\\cs412-FinalProject\\songs");

         // Populates the array with names of files and directories
         pathnames = f.list();
         ArrayList<String> glb_pathname=new ArrayList<String>();  
         //String glb_pathname[];
         // For each pathname in the pathnames array
         String global_str = "";
         for (String pathname : pathnames) {
             // Print the names of files and directories
        	 System.out.print("list files");
             System.out.println(pathname);
             glb_pathname.add(pathname.replace(".mp3", ""));
             
         }
         System.out.print(glb_pathname);
         return glb_pathname;
     }

     public void loadSongList(JList musicList){
        ArrayList<String> x = listFiles();
        String[] stringArray = x.toArray(new String[0]);

        musicList.setListData(stringArray);
     }

     public static void main(String[] args) {
        Client client = new Client(args[0], args[1]);
        client.listen();
    }
}