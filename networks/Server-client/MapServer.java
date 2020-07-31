/*
This serves as the server that receives messages from the client and
returns a response.

MapServer portNumber(optional - default to 30123)

Packets are received, and commands with "get", "remove", or "put" along
with (key, value) pairs are parsed and the server map is queried.

Resulting values(if applicable) and operations are performed on the 
server map.

Error checking for incorrectly formatted packets occurs.

*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

public class MapServer {


	public static void main(String args[]) throws IOException{

		int port_number = 30123;
		HashMap<String, String> server_map = new HashMap<String, String>();
		if (args.length > 0) {
			port_number = Integer.parseInt(args[0]);
		}

		byte[] buffer = new byte[1000];
		DatagramSocket socket = new DatagramSocket(port_number);
		DatagramPacket data_packet = new DatagramPacket(buffer,
				buffer.length);

		while (true) {
			data_packet.setData(buffer);
			socket.receive(data_packet);
			String data = new String(buffer, 0, data_packet.getLength(),
					"US-ASCII");
			String send = "";
			String[] data_split = data.split(":", 2);
			if (data_split.length != 2) {
				send = "error:unrecognizable input:" + data;
			} 
			else {
				String key = data_split[1];
				if (data_split[0].equals("get")) {
					String value = server_map.get(data_split[1]);
					if (server_map.get(key) != null) {
						send = "ok:" + value;
					}
					else {
						send = "no match";
					}
				}
				else if(data_split[0].equals("remove")) {
					key = data_split[1];
					if(server_map.containsKey(key)) {
						send = "ok";
						server_map.remove(key);
					}
					else {
						send = "no match";
					}
				}
				else if (data_split[0].equals("put")) {
					String[] put_parts = data_split[1].split(":",2);
					key = put_parts[0];
					String value = put_parts[1];
					if(server_map.get(key) != null) {
						send = "updated:" + key;
					}
					else {
						send = "ok";
					}
					server_map.put(key, value);
				}
				else {
					send = "error:unrecognizable input:" + data;
				}
			}
			data_packet.setData(send.getBytes("US-ASCII"));
			socket.send(data_packet);
		}
	}
}