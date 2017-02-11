package serverPck;

import java.net.*;
import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Date;
import java.text.*;

public class Server {
	private static ArrayList<Connection> connections = new ArrayList<Connection>();
	private SimpleDateFormat date = new SimpleDateFormat("'['hh:mm a'] '");
	private Scanner sc;
	private static String ENCODING="UTF-8";

	//метод проверяет кодировку консоли, в которой запущен сервер.
	//необходимо, чтобы все клиенты и сервер работали в одной кодировке.
	void checkCode(){ //метод определяет кодировку консоли пользователя (по стандартным кодировкам систем)
		String sys = System.getProperty("os.name");
		if (sys.contains("Windows")) ENCODING="CP866";
		else ENCODING="UTF-8";
		System.out.println("Your OS is "+sys+". Using encoding: "+ENCODING+
			"\nPlease, restart your console with this encoding if wrong.");
	}

	public Server () throws IOException{ //в этом конструкторе создается сокет по заданному порту и запускаются потоки обслуживания
		//пользователей и сервера
		checkCode();//Проверяем кодировку консоли сервера.
		InputStreamReader isr = new InputStreamReader(System.in, ENCODING);
		sc=new Scanner(isr);
		System.out.println("===WELCOME TO SERVER SIDE===");
		int port;
		while (true){
			String ports="";
			try{
				System.out.println("Input Port: (type '-1' for exit; 0<port<65534)");
				ports=sc.nextLine();
				port = Integer.parseInt(ports);
				if (port==-1) System.exit(0);
				if ( (port<65536) && (port>0) ) break;
			}catch (Exception e){
				System.out.println("Port format error, try again!");
				continue;
			}
		}

		System.out.println(":::Waiting for the client...");

		ServerControl srvCon = new ServerControl(); //запускаем модуль администрирования 

		try {
			ServerSocket srvSocket = new ServerSocket(port);//Открываем сервер на константном порту
			System.out.println(":::Local port is "+srvSocket.getLocalPort()+":::");
			while (true){
				Socket socket = srvSocket.accept();//ждем подключения к этому порту сокета
				//после этого передаем получившийся сокет вместе с принятым
				//именем пользователя в Connection
				Connection con = new Connection(socket);
				connections.add(con);//добавляем полученного пользователя в массив подключенных
				con.start(); //запускаем поток обслуживания пользователя - "соединение" 
			}

		} catch (Exception e){
			System.out.println(":::Server connector has been stopped: "+e);
			System.exit(-1);
		}
	}//end of Server constructor

	private class ServerControl extends Thread{ //класс-поток для управления сервером, 
												//пока он в то же время принимает запросы от пользователей
		//Scanner sc = new Scanner (System.in);
		ServerControl(){this.start();}//запускаем поток при создании экземпляра ласса

		public void run (){ //метод используется только для осуществления контроля за сервером
			try {
				while (true){
					String command = "";
					System.out.println(":COMMANDS:    userlist message kick exit    :COMMANDS:");
					command = sc.nextLine();
					command = command.trim();
					
					if (command.equalsIgnoreCase("exit")) { //команда закрывает сервер, перед этим уведомляя пользователей
						System.out.println();
						for (Connection c: connections)
							c.serverExit(0);
						System.exit(0);
					}

					if (command.equalsIgnoreCase("userlist")){ //команда показывает список пользователей
						System.out.println();
						if (connections.isEmpty()) {
							System.out.println("Server is empty!");
							continue;
						}
						for (Connection c : connections){
							System.out.println("["+connections.indexOf(c)+"]"+c.NAME);
						}
					}

					if (command.equalsIgnoreCase("kick")){
						try {
						System.out.println("Enter index from userlist (-1 for exit):");
						int index = sc.nextInt();
						if (index == -1) continue;
							(connections.get(index) ).userExit(1);
						} catch(Exception e) {
							System.out.println("Input error: "+e);
						}
						continue;
					}
					if (command.equalsIgnoreCase("message")){
						
						String serverMessage="";
						System.out.println("::: input message -");
						serverMessage=sc.nextLine();

						if (serverMessage.length()>2) {
							Date time = new Date();
							String times = date.format(time);
							for (Connection c : connections){
								c.out.println(times+"SERVER SAYS: "+serverMessage);
							}
							System.out.println(times+"SERVER SAYS: "+serverMessage);
						}
						continue;
					}
				}//end of while - command line

			} catch (Exception e){
				System.out.println(":::Error in Server Control module: "+e);
				try {
					for (Connection c: connections)
						c.serverExit(-1);
				}catch(IOException er){System.out.println("Error closing streams after crashing Server Control module: "+er);}
				System.exit(-1);
			}
		}//end of run() method
	}//end of ServerControl class


	private class Connection extends Thread{ //класс-поток для управления подключенными пользователями
		private String NUM="";
		private String NAME="";
		private String ID=null;
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;
		
		Connection(Socket insocket) throws IOException{
			this.socket=insocket;
			//Создаем потоки для принятого сокета.
			in=new BufferedReader (
				new InputStreamReader ( socket.getInputStream(), "UTF-8") 
			);

			out = new PrintWriter (
				new OutputStreamWriter( socket.getOutputStream(), "UTF-8"),
				true
			);
		}//end of Connection constructor

		void auth(){ //метод для получения имени на сервере
			checkArrayPlace();
			System.out.println("Someone is connected to ... ["+connections.indexOf(this)+"]");

			while(true){ //просим пользователя ввести имя
				boolean taken=false;
				try {
					String nametry=in.readLine();
					if (nametry.equalsIgnoreCase("exit")) {//если пользователь желает выйти
						userExit(0);
					}

					for (Connection c:connections){
						if ((c.NAME).equalsIgnoreCase(nametry)){//Проверяем, не занято ли имя
							out.println("This name is taken!");
							taken=true;
							break;
						}
					}
					if (taken) continue;

					out.println("1");//возвращаем единицу, чтобы пользователь получил доступ к чату
					NAME=nametry;
					break;
				} catch (Exception e){
					System.out.println("Name input error: "+e);
					userExit(-1);
					break;
				}//end of try-catch block with name
			}//end of while name block

			ID = "["+NUM+"]"+NAME; //выдаем пользователю ID

			//сообщения приветствия и уведомление пользователей
			out.println("SERVER:: Welcome "+ID+"! Type '\\exit' when youre done");
			out.println("SERVER:: Type '.ID' to enter whisper mode!");
			System.out.println("\n:::USER "+ID + " has connected!"); //Уведомляем пользователей о входе кого-либо
			for (Connection c : connections){
				if ( (c.NAME).length()<2 ) continue;
				c.out.println("\n:::USER "+ID + " has connected!");
			}
		}


		public void run(){
			try {

				auth(); //метод получения имени на сервере

				//в этом блоке мы получаем сообщения ЭТОГО КОНКРЕТНОГО пользователя
				String message="";
				Connection whisperAdress=null;
				while ( (message=in.readLine())!=null ){ 

					Date time = new Date();
					String times = date.format(time);
					checkArrayPlace();//перед отправкой сообщения в чат сверяем место пользователя в массиве

					if (message.equalsIgnoreCase("\\exit")) { //уведомляем сервер о выходе пользователя
						userExit(0);
						continue;
					}
					
					if (message.equalsIgnoreCase("\\userlist")){ //показываем список пользователей тут
						out.println(":::USERLIST:::");
						for (Connection c : connections){
							if ( (c.NAME).length()>=2 )
								out.println("["+connections.indexOf(c)+"]"+c.NAME);
						}
						continue;
					}

					int wh = checkWhisper(message); //проверяем, не пытается ли пользователь шептать
					if (wh==1) { //если да - шепчем
						goWhisper(message, times);
						continue;
					} else if (wh==0) continue; //если нет - обнуляем сообщение, он мудак и пишет с точки

					//Выводиим сообщение в том числе в окно сервера
					System.out.println(times+ID+": "+message);
					for (Connection c : connections){
						//не будем печатать сообщение тем, кто ещё не залогинился с именем
						if ( ((c.NAME).length()<3) ) continue;
						c.out.println(times+ID+": "+message);
					}
				}
				userExit(0);
			} catch (Exception e){
				System.out.println("Exited with error: "+e);
				userExit(0);
			}

		}//end of run() method






		int checkWhisper(String s){
			//Если у нас точка с цифрой и больше ничего/тупо точка
			Pattern p = Pattern.compile("^\\.\\d*\\s*$");
			Matcher m = p.matcher(s);
			if (m.matches()) return 0; //выходим и ничего не пишем

			//если похоже на личное сообщение типа ".1 привет", направляем дальше
			p = Pattern.compile("^\\.\\d+\\s+.+$");
			m = p.matcher(s);
			if (m.matches()) return 1;

			return -1; //никогда не выпадает
		}

		void goWhisper(String s, String times){
			char[] mes=null;
			char[] idc=new char[5];

			try {
				for(int i=1; i<s.length();i++){
					idc[i]=s.charAt(i); //Начиная с первого символа точно идет номер
					int n=(int)s.charAt(i);
					if (n==32){
						mes=new char[s.length()-i];
						i++;
						int j=0;
						for (;i<s.length();i++){
							mes[j]=s.charAt(i);
							j++;
						}
						break;
					}
				}
			} catch (Exception e){
				out.println("This ID is too big!");
				return;
			}

			String id = new String(idc);
			id=id.trim(); //срезаем лишние пробелы
			if (id.equals(this.NUM)) {
				out.println("You can't whisper to yourself!");
				return;
			}
			String message= new String(mes);
			//if (((message.trim()).length() )==0) return;
			//составляем финальное сообщение
			message=(times+ID+" (whispers): "+message.trim());
			Connection recipient;
			try {
				recipient = connections.get(Integer.parseInt(id));
				recipient.out.println(message);
			} catch (Exception e){
				out.println("There's no such user!");
				return;
			}
			out.println(message);
			//Выводим сообщение в том числе в окно сервера
			System.out.println(message+" to "+recipient.ID);
		}//end of goWhisper()

		/**
		*Далее идут различные методы для управления подключениями
		*/

		void checkArrayPlace(){ //метод для уточнения индекса пользователей в списке connections
			NUM=Integer.toString( connections.indexOf(this) ); //может меняться из-за выходов и киков
			ID = "["+NUM+"]"+NAME; 
		}

		public void serverExit(int code) throws IOException{ 
			//команда для закрытия потоков
			//и уведомления пользователей о закрытии сервера
			if (code==0) out.println("SERVER:: Server closed by admin.");
			if (code==-1) out.println("SERVER:: Server closed due to error.");
			try{
				this.out.close();
				this.in.close();
			}catch (IOException e){
				System.out.println("Trouble closing streams: "+e);
			}
		}

		public void userExit(int code){ //метод, при котором пользователь сам выходит
			System.out.println("["+connections.indexOf(this)+"]"+this.NAME+" has exited!");

			for (Connection c : connections){
				if (c.NAME.length()>0){
					if (code==0) c.out.println("SERVER:: "+ID+" HAS EXITED");
					if (code==1) c.out.println("SERVER:: "+ID+" KICKED");
				}
			}

			try {
				this.out.close();
				this.in.close();
			}catch(IOException e) {
				System.out.println("Trouble closing streams: "+e);
			}
			connections.remove(this);
			this.stop();
		}


		protected void finalize(){ //метод для сборщика мусора, если что-то пошло не так
			try {
				System.out.println("Garbage Collector: "+this.getName()+" thread has been destroyed.");
				this.out.close();
				this.in.close();
				connections.remove(this);
				this.stop();
			}catch (Exception e){
				System.out.println("Error in finalie method. Recommends to restart");
			}
		}//end of finalize 

	}//end of Connection class
}//end of Server class