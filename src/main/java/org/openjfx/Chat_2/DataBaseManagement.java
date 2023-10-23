package org.openjfx.Chat_2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseManagement {
	private Connection connection;
	private Statement statement;
	public DataBaseManagement(Connection connection) throws SQLException {
		this.connection = connection;
		statement = connection.createStatement();
	}
	
	public void createTable() throws SQLException {
		statement.executeUpdate("CREATE TABLE clients_Chat2 ( int idClients primary key auto_increment, varchar(10) nameClinets )");
	}
	
	public void addClient(ClientManagement clientManagement) throws SQLException {
		statement.executeUpdate("INSERT clients_Chat2(nameClients) VALUES ('"+clientManagement.getName()+"')");
	}
	
	public void deleteClient(ClientManagement clientManagement) throws SQLException {
		statement.executeUpdate("DELETE FROM clients_Chat2 WHERE nameClients = '"+clientManagement.getName()+"'");
	}
	
	public void deleteTable() throws SQLException {
		statement.executeUpdate("TRUNCATE TABLE clients_Chat2");
		statement.executeUpdate("DROP TABLE clients_Chat2");
	}
}
