package org.openjfx.Chat_2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseManagement {
	private Statement statement;
	private Connection connection;
	private boolean isCreateTable=false;//Перменная которая фиксирует создалась ли таблица в Бд
	
	public DataBaseManagement() throws SQLException {
		connection = ConnectionToDataBase.CONNECT.getConnectToDataBase();
		statement = connection.createStatement();
	}
	
	public void stopConnect() throws SQLException {
		connection.close();
	}
	
	public void createTable() throws SQLException {
		isCreateTable=true;
		statement.executeUpdate("CREATE TABLE clients ( idClients int primary key auto_increment, nameClients varchar(10), arrivalTime datetime)");
	}
	
	public void addClient(ClientManagement clientManagement) throws SQLException {
		statement.executeUpdate("INSERT clients(nameClients, arrivalTime) VALUES ('"+clientManagement.getName()+"', '"+clientManagement.getLocalDateTime()+"')");
	}
	
	public void deleteClient(ClientManagement clientManagement) throws SQLException {
		statement.executeUpdate("DELETE FROM clients WHERE nameClients = '"+clientManagement.getName()+"'");
	}
	
	public void deleteTable() throws SQLException {
		if(isCreateTable==true) {//Если таблица создана тогда произведеться удаление, если нет, тогда нечего не произойдет
			statement.executeUpdate("TRUNCATE TABLE clients");
			statement.executeUpdate("DROP TABLE clients");
		}
	}
}
