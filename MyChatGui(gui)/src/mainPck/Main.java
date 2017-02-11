package mainPck;

import java.io.*;
import java.net.Socket;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import mainPck.gui.controllers.chatViewController;
import mvcPck.MVC;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;

public class Main extends Application {
	static public Stage primaryStage;
	private static AnchorPane chatRoot;
	private static BufferedReader in;
	private static PrintWriter out;
	
	public static void main (String[] args) throws IOException{
		launch();
	}//end of main method
	
	public void start(Stage ps) {//рисует PrimaryStage и ставит на нее LoginScene.
		primaryStage=ps;
		
		primaryStage.setMinWidth(350);
		primaryStage.setMinHeight(300);
		
		primaryStage.setOnCloseRequest((e)->{
			closeStreams();
		});
		
		primaryStage.setTitle("MyChat");
		primaryStage.setResizable(false);
		setLoginScene();
		primaryStage.show();		
	}
		
	/**
	 * Далее идут методы для перестановки сцен.
	 * 
	 */
	
	public static void setLoginScene(){ //Ставит на primaryStage окошко логина
		primaryStage.setResizable(false);
		AnchorPane loginRoot = new AnchorPane();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("gui/LoginView.fxml"));
		try {
			loginRoot=loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		} //создали рут
		Scene loginScene = new Scene(loginRoot);
		primaryStage.setScene(loginScene);
		primaryStage.centerOnScreen();
	}
	
	
	
	public static void setChatScene(){ //Ставит на primaryStage чат
		try {
			primaryStage.setResizable(true);
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("gui/ChatInterface.fxml"));
			chatRoot=new AnchorPane();
			chatRoot=loader.load();
			//Попытаться переписать это так, чтобы не пришлось вызывать дополнительных вещей!
			//setChatScanner(); //в потоке этого узла будут читаться сообщения из чата
			//помещаем дополнительный узел на сцену:
			Scene chatScene = new Scene(chatRoot);
			primaryStage.setScene(chatScene);
			primaryStage.centerOnScreen();
			
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * И простой метод для активации потоков
	 * И ещё более простой для их отключения.
	 */
	
	
	public static void setupStreams(Socket socket){
		try {
			in=new BufferedReader (
				new InputStreamReader ( socket.getInputStream(), "UTF-8") 
			);

			out = new PrintWriter (
				new OutputStreamWriter( socket.getOutputStream(), "UTF-8"),
				true
			);
			MVC.end();
			//закидываем потоки в контроллеры. С этими ссылками они ждут, когда их инициализируют
			chatViewController.getStream(out,in);
		}catch(IOException e){
			System.out.println("An error occured while creating the streams! Try to restart!");
		}
	}
	
	public static void closeStreams(){
		try {
			if (in!=null) in.close();
			if (out!=null) out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}//end of Main class
