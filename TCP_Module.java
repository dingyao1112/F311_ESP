package fristdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/*
 * Ҫ��
 * 		1�����������ͻ������ӣ������ö��߳̽��
 * 		2�����ͻ��˷��͵������ڷ���ȥ������
 * 		3�����ͻ��˷��͵����ݴ洢���ض����ļ�
 * */
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tool.Connection_mysql;
import tool.Tool;





public class TCP_Module {
	private static final int PORT=8080;
	private static ServerSocket server;
	private static Map<Integer,Byte> metoyou;
	private static Map<Byte,Integer> youtome;
	private static Map<Integer,byte[]> absoluteid;
	private static Map<Integer,byte[]> data_number;//���ݱ��
	private static int storage_key=Tool.get_mysql_datalen();//���ݱ�ŵ�key,�����ݿ��ж�ȡ
	private static int storage_datalen;
	private static byte[] storage_data;
	private static byte[] IO_data;
	private static Map<Integer,Integer> relativelyid;//���ID
	private static Map<Byte,OutputStream> send_data_output;
	//num������¼��ͬ�ͻ���
	private static int num;
	public static void main(String[] args) {
		try {
			System.out.println(storage_key);
			num=0;
			IO_data=new byte[2];
			metoyou=new HashMap<Integer,Byte>();
			youtome=new HashMap<Byte,Integer>();
			absoluteid=new HashMap<Integer, byte[]>();
			relativelyid=new HashMap<Integer,Integer>();
			data_number=new HashMap<Integer, byte[]>();
			send_data_output=new HashMap<Byte,OutputStream>();
			for(int i=0;i<=0xff;i++)
			{
				metoyou.put(i, (byte)i);
			}
			
			for(int i=0;i<=0xff;i++)
			{
				youtome.put((byte)i,i);
			}
			server=new ServerSocket(PORT);
			ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
			cachedThreadPool.execute(new Threadprintf());
			while(true) {
				//���տͻ��ˣ��˳���Ϊ����ʽ��
				Socket client=server.accept();
				//ֻҪһ�����ӾͿ���һ���µ��̣߳�����ʹ��̨�ͻ�������,ʹnum+1
				num++;
				cachedThreadPool.execute(new ServerThread(client,num));
			}
		} catch (IOException e) {
			e.printStackTrace();
			if(server!=null) {
				try {
					server.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	private static class Threadprintf implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true) {
				if(absoluteid!=null) {
					Set<Integer> keyset=relativelyid.keySet();
					Iterator<Integer> it=keyset.iterator();
					System.out.println("**********��������*******"); 
					while(it.hasNext()) {
						int key=it.next();
						int id=relativelyid.get(key);
						System.out.println("����ID:"+id+"***");
					}
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	public static class isrepeatid implements Runnable{
		private byte id;
		private OutputStream os;
		byte[] repeatid= {metoyou.get(0x01),metoyou.get(0x05)};
		public isrepeatid(byte id,OutputStream os) {
			this.id=id;
			this.os=os;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int num=getflag1(id);
			try {
				send_data_output.get(id).write("���ID���������".getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				relativelyid.remove(num);
				send_data_output.remove(id);
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				relativelyid.remove(num);
				send_data_output.remove(id);
			}finally {
				try {
					os.write(repeatid);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		private int getflag1(byte id2) {
			Set<Integer> keyset=relativelyid.keySet();
			Iterator<Integer> it=keyset.iterator();
			while(it.hasNext()) {
				int key=it.next();
				if(metoyou.get(relativelyid.get(key))==id2){
					return key;
				}
			}
			return 0;
		}
		
	}
	private static class ServerThread implements Runnable{
		private Socket socket;
		//�൱��id
		private int num;
		OutputStream os;
		InputStream is;
		public ServerThread(Socket socket,int num) {
			this.socket=socket;
			this.num=num;
		}

		public void run() {
			try {
				java.sql.Connection mysql=Connection_mysql.getmysql();
				//�������ӿͻ��˵����������
				is=socket.getInputStream();
				os=socket.getOutputStream();
				byte[] comm= {metoyou.get(0x80),metoyou.get(0x03)};
				byte[] storage_data_success= {metoyou.get(0x80),metoyou.get(0x06)};
				byte[] storage_data_failure= {metoyou.get(0x80),metoyou.get(0x07)};
				byte[] senddata_error= {metoyou.get(0x02),metoyou.get(0x04)};
				byte[] read_data_failure= {metoyou.get(0x80),metoyou.get(0x08)};
				byte[] no_bind_id= {metoyou.get(0x01),metoyou.get(0x03)};
				byte[] repeatid= {metoyou.get(0x01),metoyou.get(0x05)};
				byte[] usableid= {metoyou.get(0x01),metoyou.get(0x00)};
				byte[] buf=new byte[1024];
				boolean bind_id=false;
				int len=0;
				System.out.println("IP:"+socket.getLocalAddress().getHostAddress()+"  ");
				//��ȡ�ͻ�������ʱ��
				Date day=new Date();    
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
				System.out.print(df.format(day)+"*********");  
				while((len=is.read(buf))!=-1) {
					//ͨ�Ų���
					if(buf[0]==metoyou.get(0x00)&&buf[1]==metoyou.get(0xA5)&&len==2) {
						os.write(comm);
						System.out.println(buf.toString());
					}
					//id��
					else if(buf[0]==metoyou.get(0x31)&&len==4) {
						//�����id
						if(relativelyid.containsValue(youtome.get(buf[3]))) {
							new Thread(new isrepeatid(buf[3],os)).start();
							os.write(repeatid);
						}else {
							relativelyid.put(num, youtome.get(buf[3]));
							os.write(usableid);
							send_data_output.put(buf[3], os);
							//�󶨾���Id
							byte[] arrayabsoluteid= {buf[1],buf[2]};
							absoluteid.put(num, arrayabsoluteid);
							bind_id=true;//��ID�ɹ�
						}
					}
					//����ת��
					else if(buf[0]==metoyou.get(0x32)&&len>2) {
						if(relativelyid.containsValue(youtome.get(buf[1]))) {
							if(bind_id) {
								byte id=buf[1];
								buf[0]=metoyou.get(0x00);
								buf[1]=metoyou.get(relativelyid.get(num));
								send_data_output.get(id).write(buf,0, len);//ת������
								String data=new String(buf,2,len);
								System.out.println("ID��Ϊ:"+relativelyid.get(num)+"��ID��Ϊ:"+youtome.get(id)
								+"ת������,��������Ϊ:"+data);
							}
							else {
								os.write(no_bind_id);//�豸û�а�ID
							}
						}else {
							os.write(senddata_error);//ID������
						}
					}
					//���ݴ洢
					else if(buf[0]==metoyou.get(0x34)&&len>3) {
						//��ȡ���ݱ��
						byte[] array_data_number= {buf[1],buf[2]};
						//��ѯ���ݱ���Ƿ����
						if(data_number_isexist(array_data_number)) {
							os.write(storage_data_failure);
						}else {
							storage_key++;
							//�����ݱ�ź�����key�洢����,��������ѯ
							data_number.put(storage_key, array_data_number);
							//��ȡ�洢������
							storage_datalen=len-4;//���ݳ���
							storage_data=new byte[storage_datalen+1];
							for(int i=0;i<len-3;i++) {
								storage_data[i]=buf[i+4];//��������
							}
							if(isstorage_data(storage_key,storage_data,mysql)) {
								os.write(storage_data_success);
							}else {
								os.write(storage_data_failure);
							}
						}
						}
					//��ȡ����
					else if(buf[0]==metoyou.get(0x35)&&len==3) {
						byte[] array_data_number= {buf[1],buf[2]};
						if(data_number_isexist(array_data_number)) {
							byte[] data=getreaddata(array_data_number,mysql);
							os.write(data,0,data.length-1);
						}else {
							//��Ų�����
							os.write(read_data_failure);
						}
					}
					//IOת��
					else if(buf[0]==metoyou.get(0x33)&&
							len==5&&(buf[4]==metoyou.get(0x31)||buf[4]==metoyou.get(0x00))) {
						IO_data[0]=buf[2];
						IO_data[1]=buf[3];
					}
					}
				//�Ƴ�ID
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//�ر�socket
				//�Ƴ�ID
				absoluteid.remove(num);
				relativelyid.remove(num);
				System.out.println("id:"+num+"�Ͽ�����");
			}
			
		}
		//*************************��ȡ���ݿ��е��ֽ�����*******************************//
		private byte[] getreaddata(byte[] array_data_number,java.sql.Connection mysql) {
			//�õ����ݱ�ŵ�id
			int num=0;
			Set<Integer> keyset=data_number.keySet();
			Iterator<Integer> it=keyset.iterator();
			while(it.hasNext()) {
				int key=it.next();
				if(data_number.get(key)[0]==array_data_number[0]&&data_number.get(key)[1]==array_data_number[1]) {
					num=key;
				}
			}
			PreparedStatement ps=null;
			ResultSet rs=null;
			byte[] b=null;
			try {
				ps=mysql.prepareStatement("select data from data where num=?");
				ps.setInt(1, num);
				rs=ps.executeQuery();
				rs.next();
				b=rs.getBytes("data");
			} catch (SQLException e) {
				e.printStackTrace();
			}finally {
				try {
					rs.close();
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return b;
		}

		//*******************��ѯ�����Ƿ��ظ�******************************//
		private static boolean data_number_isexist(byte[] array_data_number) {
			if(data_number==null) {
				return false;
			}
			Set<Integer> keyset=data_number.keySet();
			Iterator<Integer> it=keyset.iterator();
			while(it.hasNext()) {
				int key=it.next();
				//���ݱ�Ŵ���
				if(data_number.get(key)[0]==array_data_number[0]&&data_number.get(key)[1]==array_data_number[1]) {
					return true;
				}
			}
			return false;
		}

		//*******************���ݴ洢******************************//
		private boolean isstorage_data(int storage_key,byte[] storage_data,java.sql.Connection mysql) {
			PreparedStatement ps=null;
			try {
				mysql.setAutoCommit(false);
				ps=mysql.prepareStatement("insert into data(num,data)values(?,?)");
				ps.setInt(1, storage_key);
				ps.setBytes(2,storage_data);
				ps.execute();
				mysql.commit();
			} catch (SQLException e) {
				return false;
			}finally {
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return true;
		}
		//*****************���������ҵ�ת��ID*********************************//
		public int getflag(byte b) {
			Set<Integer> keyset=relativelyid.keySet();
			Iterator<Integer> it=keyset.iterator();
			while(it.hasNext()) {
				int key=it.next();
				if(metoyou.get(relativelyid.get(key))==b){
					return key;
				}
			}
			return 0;
		}
		
	}
}