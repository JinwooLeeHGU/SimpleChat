// https://github.com/JinwooLeeHGU/SimpleChat.git
import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;


public class ChatServer {

	public static void main(String[] args) {
				
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);   // key: value: 
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread {
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	
	
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();  // Ŭ���̾�Ʈ�� ���ۿ��� �ִ°� ���� ���� �ִ°� �ڱ� ID�̴�, (string)
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);   // hashmap�� id�� ���� --- key. pw�� ��� ������ ���پ� �����°�. chatThread�� 
										// hashmap�� �����ϴ°�. 
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		String bad[] = {"fuck","shit","damn","ass","idiot"};  // ��Ӿ� ����
		
		try{
			String line = null;
			
			while((line = br.readLine()) != null){
				boolean warning = false;  	// ��Ӿ� ���� ���� Ȯ�� 
				for(int i=0;i<bad.length;i++){
					if(line.contains(bad[i])){
						warning = true;			
						PrintWriter ID = (PrintWriter)hm.get(id);
						ID.println("Warning!!: Please do not use bad words!!");
						ID.flush();
					}
				}
				if(line.equals("/quit"))
					break;
				if(line.indexOf("/to ") == 0) {
					if(warning == true)
						continue;
					sendmsg(line);
				}else if(line.indexOf("/userlist") == 0) {
					send_userlist();
				}else {
					if(warning == true)
						continue;
					broadcast(id + " : " + line);
				}
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){
		
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			synchronized(hm){
				Object obj = hm.get(to);
				if(obj != null){
					PrintWriter pw = (PrintWriter)obj;
					pw.println(id + " whisphered. : " + msg2);
					pw.flush();
				} // if
			}
		}
	} // sendmsg
	// userlist �޼���� ������ ����ڵ��� id �� �� ����� ���� �����ش�.
	public void send_userlist(){    
		int userCount = 0;
		
		synchronized(hm){			
			Set set = hm.keySet();
			Iterator iter = set.iterator();
			
			//Collection collection = hm.KeySet();
			//Iterator iter = collection.iterator();
			PrintWriter ID = (PrintWriter)hm.get(id);
			while(iter.hasNext()){
				String next = (String) iter.next();
				    ID.println(next);
				    ID.flush();
				    userCount++;
			}
			    ID.println("Total number of Clients: "+ userCount);
				ID.flush();										
				//for (String key : hm.keySet()) {
					//pw.println(key);
					//pw.flush();	
			}
	}// userlist
	
 	public void broadcast(String msg){
 	synchronized(hm){
		Collection collection = hm.values();
		Iterator iter = collection.iterator();
		PrintWriter ID = (PrintWriter)hm.get(id);
		
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				if(pw.equals(ID))		// broadcast�Ҷ� ������ pw�� skip�����ν� �ڽ��� ���� ä�� ������ �ڽſ��Դ� ��Ÿ���� �ʵ��� ��
					continue;
				pw.println(msg);
				pw.flush();
			}
		}
	} // broadcast
}
