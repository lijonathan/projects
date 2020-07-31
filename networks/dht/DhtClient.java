/*
This is a simple DHT Client program. 
The program takes between 3-5 command line arguements:
The first is the IP address used to bind the DatagramSocket to.
The second is the name of the configuration file.
The third is the operation("get" or "put").
The fourth is an optional key and the fifth an optional value.

usage: java DhtClient IP_address cfg_file operation [key] [value]

The client does no error handling.
It sends and recieves packets usingthe packet send and recieve interfaces.
*/


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;


public class DhtClient {

	public static void main(String[] args) throws IOException {
		
		//reads from the command line and sets the values
		if(args.length < 3) {
			System.out.println("Incorrect Usage");
			return;
		}
		String client_IP_address = args[0];
		String config_file = args[1];
		String operation = args[2];
		String key = null;
		String value = null;
		if(args.length > 3) {
			key = args[3];
			if(args.length > 4) {
				value = args[4];
			}
		}
		
		//creates IP addresses and objects to create the socket and read in
		//the config file
		InetAddress local_IP = InetAddress.getByName(client_IP_address);
		DatagramSocket datagram_socket = new DatagramSocket(0, local_IP);
		FileInputStream file_stream_reader = new FileInputStream(
							config_file);
		InputStreamReader reader = new InputStreamReader(file_stream_reader,
					 "US-ASCII");
		BufferedReader read_config_file = new BufferedReader(reader);

		
		//reads and parses the config file for operations, keys, values,
		//port numbers, and hostname
		String data = read_config_file.readLine();
		String[] data_split = data.split(" ");
		
		String hostname = data_split[0];
		int port_number = Integer.parseInt(data_split[1]);

		InetAddress server_IP = InetAddress.getByName(hostname);
		InetSocketAddress server_address= new InetSocketAddress(server_IP,
			port_number);
		
		Packet send_packet = new Packet();
		//sets the send packet values
		send_packet.type = operation;
		send_packet.key = key;
		send_packet.val = value;
		send_packet.tag = 12345;
		//sends through the Packet class send interface
		send_packet.send(datagram_socket, server_address, true);
		//receives the response
		Packet receive_packet = new Packet();
		receive_packet.receive(datagram_socket, true);
		file_stream_reader.close();
		reader.close();
		read_config_file.close();
	}
}
