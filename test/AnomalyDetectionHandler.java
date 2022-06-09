package test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.io.PrintWriter;

import test.Commands.DefaultIO;
import test.Server.ClientHandler;

public class AnomalyDetectionHandler implements ClientHandler {

	@Override
	public void handleClient(InputStream inFromClient, OutputStream outToClient) {

		SocketIO sio = new SocketIO(inFromClient, outToClient);
		CLI cli = new CLI(sio);
		cli.start();
		sio.close();
	}

	public class SocketIO implements DefaultIO {

		protected Scanner inFromClient;
		protected PrintWriter outToClient;

		SocketIO(InputStream inFromClient, OutputStream outToClient) {
			this.inFromClient = new Scanner(inFromClient);
			this.outToClient = new PrintWriter(new OutputStreamWriter(outToClient));
		}

		@Override
		public String readText() {
			return this.inFromClient.nextLine();
		}

		@Override
		public void write(String text) {
			this.outToClient.print(text);
			this.outToClient.flush();
		}

		@Override
		public float readVal() {
			return this.inFromClient.nextFloat();
		}

		@Override
		public void write(float val) {
			this.outToClient.print(val);
			this.outToClient.flush();
		}

		public void close() {
			try {
				this.inFromClient.close();
				this.outToClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
