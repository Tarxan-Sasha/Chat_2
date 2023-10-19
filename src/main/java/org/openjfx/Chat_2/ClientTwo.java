package org.openjfx.Chat_2;

public class ClientTwo {

	public static void main(String[] args) {
		ClientManagement clientManagement = new ClientManagement();
		Thread t = new Thread(clientManagement);
		t.start();

	}

}