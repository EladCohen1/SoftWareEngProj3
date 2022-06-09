package test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server {

	private volatile boolean stop;

	public Server() {
		stop = false;
	}

	public interface ClientHandler {
		void handleClient(InputStream inFromClient, OutputStream outToClient);
	}

	private void startServer(int port, ClientHandler ch) {
		try {
			ServerSocket server = new ServerSocket(port);
			while (!stop) {
				Socket aClient = server.accept();
				ch.handleClient(aClient.getInputStream(), aClient.getOutputStream());
				aClient.close();
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// runs the server in its own thread
	public void start(int port, ClientHandler ch) {
		new Thread(() -> startServer(port, ch)).start();
	}

	public void stop() {
		stop = true;
	}
}
