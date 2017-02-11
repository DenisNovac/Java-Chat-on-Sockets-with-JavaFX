package clientPck;

import java.util.Scanner;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import mvcPck.MVC;

public class Client {
	//инициализируем переменные потоков для считывания с сервера и передачи на сервер
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;
	private static String ENCODING="UTF-8";

	void checkCode(){ //метод определяет кодировку консоли пользователя (по стандартным кодировкам систем)
		String sys = System.getProperty("os.name");
		if (sys.contains("Windows")) ENCODING="CP866";
		else ENCODING="UTF-8";
		System.out.println("Your OS is "+sys+". Using encoding: "+ENCODING+
			"\nPlease, restart your console with this encoding if wrong.");
	}

	public Client () throws UnsupportedEncodingException {
		checkCode();
		System.out.println("===WELCOME TO CLIENT SIDE===");
		InputStreamReader isr = new InputStreamReader(System.in, ENCODING);
		Scanner sc = new Scanner(isr);
		String name, error;
		while (true){
			while (true){
				String ip="";
				System.out.println("Input IP: xxx.xxx.xxx.xxx (commands: 'exit')");
				ip = sc.nextLine();
				if (ip.equals("exit")) System.exit(-1);

				error=MVC.checkIp(ip);
				if (  error==null) break;

				else System.out.println(error);
			}//end of ip while

			while (true){
				String ports="";
				System.out.println("Input Port (0<port<65534): (commands: 'exit', 'back')");
				ports=sc.nextLine();
				if (ports.equals("exit")) System.exit(0);
				if (ports.equals("back")) ports="65534";

				error=MVC.checkPort(ports);
				if ( error==null) break;

				else System.out.println(error);
			} //end of port while

			error=MVC.checkSocket();
			if ( error==null ) {//теперь проверяем, возможно ли создание сокета из данных, что ввели ранее, если нет - то цикл перезапустится
				while (true){//цикл для получения имени пользователя, возможен, если сокет нашел сервер и подключился
					System.out.println("Enter your name (3 or more characters(less than 15), 'exit' for exit)");
					name=sc.nextLine();

					if (name.equals("exit")){
						System.exit(0);
					}

					error=MVC.checkName(name);
					if ( error==null ) break;
					
					else System.out.println(error);	
				}					
			} else {
				System.out.println(error);
				continue;
			}

			socket = MVC.getSocket();//получаем, наконец, наш собранный сокет из конструктора MVC
			MVC.end(); //удаляем всю информацию из конструктора MVC
			break; //Брик происходит, если нет проблем ни с сокетом, ни с портом
			//проще говоря - если всё настроено правильно

		}//end of auth while

		//new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
		
		//Открываем потоки ввода/вывода
		try {
			in = new BufferedReader (
				new InputStreamReader ( socket.getInputStream(), "UTF-8") 
			);

			out = new PrintWriter (
				new OutputStreamWriter( socket.getOutputStream(), "UTF-8"),
				true
			);
		}catch(IOException e){
			System.out.println("An error occured while creating the streams! Try to restart!");
		}

		ChatScan scan = new ChatScan();//запускается поток приёма сообщений

		//тут пользователь пишет и отправляет свои сообщения в чат,
		//цикл бесконечный - это второй поток.
		String message="";
		while (true) {
			message=sc.nextLine();

			if (message.equals("\\exit")) {
				out.println(message);
				System.exit(0);
			}

			if (message.equals("")) continue;
			out.println(message);
		}//end of sending message while
	}//end of Client constructor

	private class ChatScan extends Thread {//поток выводит все сообщения из сервера
		//в фоновом режиме, пока пользователь пишет свои выше.
		ChatScan(){
			this.start(); //запуск при создании
		}

		public void run(){
			try {
				String fromserver;
				while ( (fromserver=in.readLine())!=null ){
					System.out.println(fromserver);
				}
				System.exit(0);

			} catch (IOException e){
				System.out.println("Trouble with taking message: "+e);
				System.out.println("Try to restart client");
				System.exit(-1);
			}

		}//end of run() method
	}//end of ChatScan class
}//end of Client class