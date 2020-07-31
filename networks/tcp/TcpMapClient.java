/*
This is a TcpMapClient that takes commands to send to the server.
The port number defaults to 30123.
The server recieves commands that store, retrieves, and modifies key
value pairs within the server map. 

The four commands are get, put, remove, and get all.
The first argument is the host name of the server and the second
is the port number.
*/


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpMapClient {

	public static void main(String args[]) throws Exception {
		if(args.length < 1){
			System.out.println("Incorrect Usage");
			return;
		}
		//default port if none provided
		int port_number = 30123;
		String host_name = args[0];
		if (args.length > 1){
			port_number = Integer.parseInt(args[1]);
		}
		//creates the socket
		Socket socket = new Socket(host_name, port_number);
		//the reader and writer for the input output stream
		BufferedReader input_reader = new BufferedReader(new InputStreamReader(
				    	 socket.getInputStream(),"US-ASCII"));
		BufferedWriter output_writer = new BufferedWriter(
			new OutputStreamWriter(socket.getOutputStream(),"US-ASCII"));
		
		BufferedReader sysin = new BufferedReader(new InputStreamReader(
					   System.in));

		String line = "";
		//waits for the server and prints out the response
		while (true) {
			System.out.print("Type a command for the server: ");
			line = sysin.readLine();
			if (line == null || line.length() == 0){
				break;
			}
			output_writer.write(line);
			output_writer.newLine();
			output_writer.flush();
			System.out.println(input_reader.readLine());
		}
		socket.close();
	}
}
