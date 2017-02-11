package mvcPck;
/**
*MVC - Module of Verification Construct - ��������� ������ ����������� � �������, ����������� � �����
*��������� ������ ������� � ���� �� ��������� ���������� - ��� ��������, ��� � ����������� ���������������� �����������.
*����� ������ �����: �� ���� ����������� ������������ ������ �������� ��������� �� ������. � ����� �������� �����������
*���������� �����. ���� �� - �������� ����� ������ ������ ������������� ������.
*����� ������ ������ ���������� ������� ����� end();
*/
import java.io.*;
import java.net.Socket;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MVC {
	static private String ip,answer;
	static private int port=0;
	static private BufferedReader in;
	static private PrintWriter out;
	static private Socket socket;
	static private Boolean isConnect=false;
	static private Boolean goodName=false;

	static public String checkIp(String ipin){ //����� ��������� ��������� ������������� ip � ���� null, ���� ��������.
		if (ipin.length()<9) return ("IP must be 9 or more characters!");
		else {
			ip=ipin;
			return (null);
		}
	}

	static public String checkPort(String portin){ //����� ��������� ��������� ������������� ip � ���� null, ���� ��������.
		try {
			port=Integer.parseInt(portin);
		}catch (Exception e) {
			return "Port format error! Input numbers";
		}
		if ( (port<65535) && (port>0) ) {
			return (null);
		}
		else {
			port=0;
			return ("Port must be >0 and <65535!");
		}

	}

	static public String checkSocket(){ //���������, �������� �� ����������� � ������, � ������� ������
		try{							//����� ���������� ������� ��� ��������� ������
			socket=new Socket(ip,port);

			in = new BufferedReader (
				new InputStreamReader ( socket.getInputStream() ) 
			);

			out = new PrintWriter (
				socket.getOutputStream(), true
			);

			isConnect=true;
			return(null);
		}catch (Exception e){
			socket=null;
			return "An error occurred while connecting socket. Server may be offline.";
		}
	}

	static public String checkName(String name){ //����� ���������, �������� �� ������ ��� �� �������
		if (!isConnect) return "You don't have a connected socket yet!";
		if (name.length()<3 | name.length()>15) {
			/**
			 *������ ����� ������ ��� GUI �������, ��������� � ��
			 *��� ���� ����������� ������������, � ����� ����������� �� ����� �����������
			 *�����
			 */
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
			return "Username must be more than 3 and less than 15 chars!";
		}
		//��� ������� ������ �� ��������, ����, ������������� � ������
		Pattern p = Pattern.compile("^[A-Za-z0-9_-]{3,15}$");
		Matcher m=p.matcher(name);
		if (!m.matches()) return "Username must contains only latin characters, hyphen, underline and numbers!";

		try{
			out.println(name);
			if (( answer=(in.readLine()) ).equals("1") ) {
				goodName=true;
				return null;
			} else return "Error from server: "+answer;
		}catch (IOException e){return "Unexpected stream error! ";}
	}


	public static String getEncode(){
		try{
			String ENCODE = in.readLine();
			return ENCODE;
		} catch(Exception e){
			System.out.println("Can't giveout Encode: "+e);
			return "no";
		}
	}

	//����� ���������� ��������� �����, �������� ������������� ����� end()
	public static Socket getSocket(){ 
		if (isConnect & goodName) {		
			return socket;
		}
		else return null;
	}

	//����� ������ �������� ��� ���������� ������������ �������, ����������� ��������, ����� �� ������� ��� ���� ������� �� ����� ������!!!
	static public void end() {
		out=null; in=null;
		ip=null; 
		socket=null;
	}

}//end of MVC class