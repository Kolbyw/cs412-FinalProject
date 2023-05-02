import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
/******************************************************************************
 *  Compilation:  javac ChatServer.java 
 *  Execution:    java ChatServer
 *  Dependencies: In.java Out.java Connection.java ConnectionListener.java
 *
 *  Creates a server to listen for incoming connection requests on 
 *  port 4444.
 *
 *  % java ChatServer
 *
 *  Remark
 *  -------
 *    - Use Vector instead of ArrayList since it's synchronized.
 *  
 ******************************************************************************/

import java.net.Socket;
import java.net.ServerSocket;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


@SuppressWarnings("unused")
public class Server{

    public static void main(String[] args) throws Exception {
        Vector<Connection> connections        = new Vector<Connection>();
        @SuppressWarnings("resource")
		ServerSocket serverSocket             = new ServerSocket(2000);
        ConnectionListener connectionListener = new ConnectionListener(connections);
        ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

        //thread that broadcasts messages to clients
        connectionListener.start();

        System.err.println("ChatServer started");

        while (true) {

            // wait for next client connection request
            Socket clientSocket = serverSocket.accept();
            System.err.println("Created socket with client");

            // listen to client in a separate thread
            Connection connection = new Connection(clientSocket);
            connections.add(connection);
            pool.execute(connection);
            connection.start();
        }
    }

}