package org.openjfx.Chat_2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.time.LocalTime;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;


public class ClientManagement extends Application implements Runnable{
	private static Socket clientSocket;
	private static BufferedReader reader;
	private static BufferedWriter writer;
	private static String name;
	private static TextArea textAreaOutput;
	private static TextField textFieldInput;
	private static Button btn1;
	private boolean isShadowClient;
	
	public ClientManagement() {
	}
	public ClientManagement(String name) {
		this.name = name;
	}
	public ClientManagement(boolean shadowClient) {
		this.isShadowClient = shadowClient;
	}
	
	@Override
	public void run() {
		if(isShadowClient==true) {
			startClient();
		}else {
			launch();
		}
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		textAreaOutput = new TextArea();
		textAreaOutput.setMaxWidth(300);
		textAreaOutput.setMaxHeight(300);
		textAreaOutput.setWrapText(true);
		textAreaOutput.setEditable(false);
		
		FlowPane fpTextArea = new FlowPane(textAreaOutput);//Первый блок с полем вывода
		FlowPane.setMargin(fpTextArea, new Insets(5, 0, 0, 20));
		
		textFieldInput = new TextField();
		textFieldInput.setPrefWidth(215);
		btn1 = new Button("Отправить");
		
		FlowPane fpInputFields = new FlowPane(Orientation.HORIZONTAL,10,0,textFieldInput, btn1);//Второй Блок с кнопкой и поелм для ввода
		FlowPane.setMargin(fpInputFields, new Insets(5, 0, 0, 20));
		
		FlowPane fpTextArea_InputFields = new FlowPane(fpTextArea, fpInputFields);//Соединение блоков в один
		
		Scene scene = new Scene(fpTextArea_InputFields);
		
		primaryStage.setScene(scene);
		primaryStage.setTitle(name);
		primaryStage.setMinWidth(500);
		primaryStage.setMinHeight(300);
		primaryStage.setMaxWidth(500);
		primaryStage.setMaxHeight(300);
		
		primaryStage.show();
		
		primaryStage.setOnCloseRequest(e ->{//Что произойдет если нажать на крестик
			writeToServer("Exit");
			stopClient();
		});
		
		startClient();
		
	}
	
	private void startClient() {
		try {
			clientSocket = new Socket("localhost", 8000);
			System.out.println(name + ", впишіть будь яке речення чи слово");

			reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

			WriteMsg wm = new WriteMsg();
			wm.start();// Запуск процесса записи
			ReadMsg rm = new ReadMsg();
			rm.start();// Запуск процесса чтения


		} catch (IOException e) {
			stopClient();
			e.printStackTrace();
		}
	}	

	// Метод що зупняє Клиент
	private static void stopClient() {
		try {
			reader.close();
			writer.close();
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// Метод отправляет сообщение на сервер
	private void writeToServer(String message) {
		try {
			//Если сообщение это "Вихід" или "Exit" тогда отправляет только это сообщение, нечего лишнего не отправляет
			if (message.equals("Вихід") || message.equals("Exit")) {

				System.out.println("Ви вийшли");
				writer.write(message+"\n");
				writer.flush();
				
			//Если сообщение не соответсвует тому сверху, тогда к обычному сообщению добавляетсья еще и имя ионо спокойно отправляеться
			} else {
				writer.write(name + ": " + message + "\n");
				writer.flush();
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public String getName() {
		return name;
	}
	
	// внутрішній клас для читання за серверу(з чату)
	private class ReadMsg extends Thread {
		private String msg;
		private String lastMsg = "Початок діалогу ";

		@Override
		public void run() {

			while (true) {
				try {
					msg = reader.readLine();
					LocalTime localTime = LocalTime.now();//Получаем текущее время час минута и секунда
					lastMsg = lastMsg + "\n"+msg+"   "+localTime.getHour()+":"+localTime.getMinute();//собираем все в кучу
					textAreaOutput.setText(lastMsg);
					// System.out.println(msg); //отображение на консоле
					if (msg.equals("Вихід") || msg.equals("Exit")) {

						break;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

	}

	// Внутрішній клас для запису на сервер(у чат)
	private class WriteMsg extends Thread {
		private String words;//Слова которые пишутсья клиентом и отправляються
		@Override
		public void run() throws NullPointerException {
			while (true) {
				btn1.setOnAction( e -> {//Действие кнопки "Отправить"
					if(isShadowClient==true) {
						System.out.println("ShadowClient");
						words="Exit";
					}else {
						words = textFieldInput.getText();// получение с поля во фрейме
						textFieldInput.deleteText(0, words.length());
					}
					writeToServer(words);
				});
				
				if (words.equals("Вихід") || words.equals("Exit")) {
					System.out.println(1);
					stopClient();
					break;
				}

			}

		}

	}

}
