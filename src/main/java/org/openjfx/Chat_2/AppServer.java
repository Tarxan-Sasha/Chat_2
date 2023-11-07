package org.openjfx.Chat_2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.stage.Stage;
/*
 * JavaFX App
 */
public class AppServer extends Application implements Runnable{

	public static int PORT = 8000;
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	public static List<ServerManagment> listOfClients = new LinkedList<ServerManagment>();
	private static TextArea textArea;
	private static Button btn1 = new Button("Старт Сервера");
	private static Button btn2 = new Button("Получить список клиентов БД");
	private static Button btn3 = new Button("Остановить Сервер");
	private static Ellipse ellipseDataBase;
	private static Ellipse ellipseServer;
	private static Thread tAppServer;//Поток который будет отвечать за сервер
	private static DataBaseManagement dataBaseManagement;//Обьект класса управления БД
	public static volatile int amountClients = 0;//Число подключенных к серверу клиентов 
	public static final String ACTIVE_BUTTON_STYLE="-fx-background-color: #008000; -fx-border-width: 5px; -fx-border-color:#006400; -fx-text-fill: #FFFFFF";//Стиль для активной кнопки
	public static final String INACTIVE_BUTTON_STYLE="-fx-background-color: #B22222; -fx-border-width: 5px; -fx-border-color:#8B0000; -fx-text-fill: #FFFFFF";//Стиль для неактивной кнопки
	
	public static boolean checkWorking=true;//Отвечает за отключение сервера
	
    public static void main(String[] args) {
    	launch();
    }

  @Override
    public void start(Stage stage) {

	  btn1.setPrefWidth(200);
	  btn1.setPrefHeight(100);
	  btn1.setStyle(ACTIVE_BUTTON_STYLE);
	  
	  btn2.setPrefWidth(200);
	  btn2.setPrefHeight(100);
	  btn2.setDisable(true);
	  btn2.setStyle(INACTIVE_BUTTON_STYLE);
	  
	  btn3.setPrefWidth(200);
	  btn3.setPrefHeight(100);
	  btn3.setStyle(INACTIVE_BUTTON_STYLE);
	  btn3.setDisable(true);
 
	  FlowPane fpBTNS = new FlowPane(btn1,btn2,btn3);//FlowPane с кнопочками
	  fpBTNS.setOrientation(Orientation.VERTICAL);//Устанавливает вертикальную ориентацию
	  fpBTNS.setVgap(30);//Устанавливает вертикальные отступы между элементами
	  FlowPane.setMargin(fpBTNS, new Insets(50,0,0,-40));//Отступ
	  
	  Label labelServer = new Label("Сервер:");
	  labelServer.setMaxSize(50, 50);//Максимальный развер label первое значение ширина, второе значение высота
	  
	  ellipseServer = new Ellipse();
	  ellipseServer.setCenterX(2);
	  ellipseServer.setCenterY(3);
	  ellipseServer.setRadiusX(4);
	  ellipseServer.setRadiusY(5);
	  ellipseServer.setFill(Color.RED);
	  
	  Label labelDataBase = new Label("  DataBase:");
	  labelDataBase.setMaxSize(60, 50);//Максимальный развер label первое значение ширина, второе значение высота
	  
	  ellipseDataBase = new Ellipse();
	  ellipseDataBase.setCenterX(2);
	  ellipseDataBase.setCenterY(3);
	  ellipseDataBase.setRadiusX(4);
	  ellipseDataBase.setRadiusY(5);
	  ellipseDataBase.setFill(Color.RED);
	 
	  FlowPane fpLabels_Eliipses = new FlowPane(labelServer,ellipseServer, labelDataBase, ellipseDataBase);
	  fpLabels_Eliipses.setOrientation(Orientation.HORIZONTAL);
	  fpLabels_Eliipses.setMaxWidth(120);
	  
	  textArea = new TextArea();
	  textArea.setPrefWidth(300);//Устанавливает предпочитаемую ширину
	  textArea.setPrefHeight(420);//Устанавливает предпочитаемую висоту
	  textArea.setEditable(false);//Устанавливает можно ли писать в поле или нет
	  textArea.setWrapText(true);//Текст переноситься на новую строчку, после того как доходит края, а не идет и идет и идет...	  
	  
	  FlowPane fpLabel_TextArea = new FlowPane(fpLabels_Eliipses, textArea);
	  fpLabel_TextArea.setOrientation(Orientation.VERTICAL);
	  FlowPane.setMargin(fpLabel_TextArea, new Insets(1,0,0,4));//Отступ
	  
	  FlowPane flowPane = new FlowPane(fpLabel_TextArea, fpBTNS);
	  flowPane.setHgap(50);
	  flowPane.setPrefWidth(300);
	  flowPane.setPrefHeight(900);
	  flowPane.setAlignment(Pos.TOP_LEFT);
	  
	  Scene scene = new Scene(flowPane);
	  /*
	   * Запуск сервера
	   */
	  btn1.setOnAction(e -> {
		  checkWorking=true;
		  tAppServer = new Thread(new AppServer());
		  tAppServer.start();
	  });
	 
	  btn2.setOnAction(e -> {
//		  if (ConnectionToDataBase.CONNECT.isConnectToDataBase() && !(dataBaseManagement == null)) {
			  try {
				  textArea.appendText(dataBaseManagement.getAllClients());
			  } catch (SQLException e1) {
				  // TODO Auto-generated catch block
				  e1.printStackTrace();
			  }
//		  }
	  });
	  //Нажатие на эту кнопку останавливает сервер
	  btn3.setOnAction(e -> {
		  //tAppServer.interrupt();
		  checkWorking = false;
		  
		  ClientManagement clientManagement = new ClientManagement(true);
		  Thread t = new Thread(clientManagement);
		  t.start();
		  //Удаляет таблицу в БД
		  if(ConnectionToDataBase.CONNECT.isConnectToDataBase()) {
			try {
				dataBaseManagement.deleteTable();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		  }
	  });
	  
	  stage.setScene(scene);
	  stage.setTitle("Chat_2");
	  stage.setMaxWidth(700);
	  stage.setMaxHeight(500);
	  stage.setMinWidth(700);
	  stage.setMinHeight(500);
	  
	  stage.show();
	  
	  stage.setOnCloseRequest(e ->{//Что произойдет если нажать на крестик
		  if(ConnectionToDataBase.CONNECT.isConnectToDataBase() && !(dataBaseManagement==null)) {//Проверяет есть ли подключение и null ли dataBaseManagement
			try {
				dataBaseManagement.deleteTable();
				dataBaseManagement.stopConnect();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		  }
		  System.exit(0);//Этот метод завершает программу
	  });
    }
  
	@Override
	public void run() {
		 startServer();
	}
  
	public void startServer() {
		try {
			//Меняем цвет кнопкам
			btn1.setDisable(true);//Выключает кнопку
			btn1.setStyle(INACTIVE_BUTTON_STYLE);
			btn3.setDisable(false);//Включает кнопку
			btn3.setStyle(ACTIVE_BUTTON_STYLE);
			////////////////////////
			//Устанавливаем соединение
			serverSocket = new ServerSocket(PORT);//Встановили порт
			textArea.appendText("Сервер запущений\n");
			ellipseServer.setFill(Color.GREEN);
			
			//Я перенес подключениек БД из кнопки сюда(в кнопке закоментирвоал) теперь ошибка идет реже....может дело в потоках и что что то не успевает закрыться/открыться
			if (ConnectionToDataBase.CONNECT.isConnectToDataBase()) {
				dataBaseManagement = new DataBaseManagement();
				dataBaseManagement.createTable();
				textArea.appendText("База данних - включена \n");
				ellipseDataBase.setFill(Color.GREEN);
				btn2.setDisable(false);//Включает кнопку
				btn2.setStyle(ACTIVE_BUTTON_STYLE);
			} else {
				textArea.appendText("База данних - вимкнена \n");
			}
			
			textArea.appendText("Чекаю підключення\n");
			
			while (checkWorking) {//Проверяет на прерывание потока сервера !(tAppServer.isInterrupted())
				clientSocket = serverSocket.accept();// Чекає підключення
				if(checkWorking) {//Проверяет на прерывание потока сервера
					try {
						listOfClients.add(new ServerManagment(clientSocket));//Добавляет в колекцию, временная замена БД
						textArea.appendText("Підключень: " + ++amountClients + "\n");//Вывод на экран сервера количество подключенных клиентов
						listOfClients.forEach(x -> textArea.appendText(x.getName()+ "\n"));//Вывод на экран сервера всей колекции
					
					} catch (IOException e) {
						clientSocket.close();
					}
				}	
			}
			
			ServerManagment.sendToAll("Сервер закончил работу");//Отправляет всем клиентам "Exit" что заставляет их остановить диалог		
			while(listOfClients.size()>0) {//Проверяем закрыли ли мы всех клиентов, если нет, тогда ждем 100 милисикунд, если снова не полностью очистили клиентов, еще ждем
				Thread.sleep(500);
			}
			System.out.println(listOfClients.size());
			//Меняем цвет кнопкам
			btn3.setStyle(INACTIVE_BUTTON_STYLE);
			btn3.setDisable(true);//Выключает кнопку
			btn2.setStyle(INACTIVE_BUTTON_STYLE);
			btn2.setDisable(true);//Выключает кнопку
			btn1.setStyle(ACTIVE_BUTTON_STYLE);
			btn1.setDisable(false);//Включает кнопку

			textArea.appendText("Сервер закрыт\n");//Отправляет на экран сервера сообщение о его закрытии	
			ellipseServer.setFill(Color.RED);//Перключает цвет обозначения работы сервера 
			ellipseDataBase.setFill(Color.RED);//Перключает цвет обозначения работы Базы Данных
			serverSocket.close();
			
		} catch (IOException | InterruptedException | SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				clientSocket.close();
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static class ServerManagment extends Thread {

		private Socket socket;
		private BufferedReader reader;
		private BufferedWriter writer;

		public ServerManagment(Socket socket) throws IOException {
			this.socket = socket;
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			start();
		}

		@Override
		public void run() {
			String request;
			while (true) {
				try {
					request = reader.readLine();
					//Если приходящее сообщение это "Вихід" и "Exit", тогда сервер отправляет сообщение обратно и вычеркивая клиента из списка, закрывает соединение
					if (request.equals("Вихід") || request.equals("Exit")) {
						send("Вы вышли");
						listOfClients.remove(this);
						textArea.appendText("Підключень: " + --amountClients +" \n");
						//System.out.println(listOfClients);
						stopConnectionToClient();
						break;
					}
					/*
					 * Если приходящее сообщение не то что выше, тогда сервер отправляет всем пользователям сообщение, 
					 * вместе с именем которое автоматически даеться при написании сообщения в ClientManagement
					 */
					sendToAll(request);
					
				} catch (IOException e) {
					stopConnectionToClient();
					e.printStackTrace();
				}
			}
		}
		//Отправляет сообщение всем
		public static void sendToAll(String request)  {
			if(listOfClients.size() > 0){
				listOfClients.forEach(x -> {
					try {
						x.send(request);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			}
		}

		//Отправляет сообщение
		public void send(String request) throws IOException {	
			writer.write(request + "\n");
			writer.flush();
		}
		
		//Остановка сервера
		private void stopConnectionToClient() {
			try {
				reader.close();
				writer.close();
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
