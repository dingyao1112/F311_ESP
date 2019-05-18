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
	private static byte[] froward_data;//ת������
	private static byte[] IO_data;
	private static int froward_datalen;
	private static int flag=0;//�����̱߳���
	private static int closeid=0;
	private static boolean IO_response=false;//�ж���IOת����������ת��
	private static Map<Integer,Integer> relativelyid;//���ID
	private static Map<Byte,OutputStream> send_data_output;
	//num������¼��ͬ�ͻ���
	private static int num;
	public static void main(String[] args) {
		try {
			System.out.println(storage_key);
			num=0;
			froward_data=new byte[200];
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
//			cachedThreadPool.execute(new Threadprintf());
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
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	private static class ServerThread implements Runnable{
		private Socket socket;
		//�൱��id
		private int num;
		OutputStream os;
		InputStream is;
		private ServerThread selfThread;
		public ServerThread(Socket socket,int num) {
			this.socket=socket;
			this.num=num;
			selfThread=this;
		}

		public void run() {
			try {
				java.sql.Connection mysql=Connection_mysql.getmysql();
				//�������ӿͻ��˵����������
				is=socket.getInputStream();
				os=socket.getOutputStream();
				byte[] comm= {metoyou.get(0x5A),metoyou.get(0x80),metoyou.get(0x03)};
				byte[] storage_data_success= {metoyou.get(0x5A),metoyou.get(0x80),metoyou.get(0x06)};
				byte[] storage_data_failure= {metoyou.get(0x5A),metoyou.get(0x80),metoyou.get(0x07)};
				byte[] read_data_failure= {metoyou.get(0x5A),metoyou.get(0x80),metoyou.get(0x08)};
				byte[] buf=new byte[1024];
				int len=0;
				System.out.println("IP:"+socket.getLocalAddress().getHostAddress()+"  ");
				//��ȡ�ͻ�������ʱ��
				Date day=new Date();    
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
				System.out.print(df.format(day)+"*********");  
//				//��������flag�߳�
				//new Thread(new monitorflag(os,num)).start();
				while((len=is.read(buf))!=-1) {
					//ͨ�Ų���
					if(buf[0]==metoyou.get(0x5A)&&buf[1]==metoyou.get(0x00)&&
						buf[2]==metoyou.get(0xA5)&&len==3) {
						os.write(comm);
						System.out.println(buf.toString());
					}
					//id��
					else if(buf[0]==metoyou.get(0x5A)&&buf[1]==metoyou.get(0x01)&&len==5) {
						//�����id
						bin_relativelyid(buf[4], os);
						send_data_output.put(buf[4], os);
						//�󶨾���Id
						byte[] arrayabsoluteid= {buf[2],buf[3]};
						absoluteid.put(num, arrayabsoluteid);
						
					}
					//����ת��
					else if(buf[0]==metoyou.get(0x5A)&&buf[1]==metoyou.get(0x02)&&len>3) {
						//�洢����λ
//						froward_datalen=len;
//						froward_data[0]=metoyou.get(0x5A);
//						froward_data[1]=metoyou.get(0x00);
//						froward_data[2]=metoyou.get(relativelyid.get(num));
//						for(int i=3;i<len;i++) {
//							froward_data[i]=buf[i];
//						}
						send_data_output.get(buf[2]).write(buf, 3, len);
						//flag������ط������o�ĸ�Id��������
//						System.out.println("*******"+youtome.get(buf[2])+"*******");
						int ln=getflag(buf[2]);
						IO_response=false;
						flag=ln;
//						System.out.println("��"+relativelyid.get(flag)+"������Ϣ");
					}
					//���ݴ洢
					else if(buf[0]==metoyou.get(0x5A)&&buf[1]==metoyou.get(0x04)&&len>4) {
						//��ȡ���ݱ��
						byte[] array_data_number= {buf[2],buf[3]};
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
					else if(buf[0]==metoyou.get(0x5A)&&buf[1]==metoyou.get(0x05)&&len==4) {
						byte[] array_data_number= {buf[2],buf[3]};
						if(data_number_isexist(array_data_number)) {
							byte[] data=getreaddata(array_data_number,mysql);
							os.write(data,0,data.length-1);
						}else {
							//��Ų�����
							os.write(read_data_failure);
						}
					}
					//IOת��
					else if(buf[0]==metoyou.get(0x5A)&&buf[1]==metoyou.get(0x03)&&
							len==6&&(buf[5]==metoyou.get(0x01)||buf[5]==metoyou.get(0x00))) {
						IO_response=true;
						IO_data[0]=buf[3];
						IO_data[1]=buf[4];
					}
					}
				closeid=num;
				socket.close();//�ر�socket
				//�Ƴ�ID
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				closeid=num;
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
		private int getflag(byte b) {
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
		
		//*******************�����id********************//
		public void bin_relativelyid(byte a,OutputStream os) throws IOException {
			byte[] repeatid= {metoyou.get(0x5A),metoyou.get(0x81),metoyou.get(0x05)};
			byte[] usableid= {metoyou.get(0x5A),metoyou.get(0x81),metoyou.get(0x00)};
				//�����id
				//�ж�Id�Ƿ��ظ�
				System.out.println("id��");
				Set<Integer> keyset=relativelyid.keySet();
				Iterator<Integer> it=keyset.iterator();
				int exp=0;
				//����relativelyid�е�value
				while(it.hasNext()) {
					int key=it.next();
					if(metoyou.get(relativelyid.get(key))==a) {
						os.write(repeatid);//id�ظ��������ݸ��ͻ��˱�ʾ��ʧ��
						exp=1;
						break;
					}
				}
				if(exp==0) {
					relativelyid.put(num,youtome.get(a));
					os.write(usableid);//id�󶨳ɹ��������ݸ��ͻ���
				}
					
			
		}
		
	}
	
	//*************************��ؽ���ת������***********************************//
	private static class monitorflag implements Runnable{
		
		private OutputStream os1;
		private int num1;
		public monitorflag(OutputStream os,int num) {
			os1=os;
			num1=num;
		}
		
		public void run() {
			System.out.println(num+"���޼����߳̿���");
			while(true) {
				try {
					//���ʣ�Ϊʲô����Ҫsleep����ͻ��˼����߳�ֻ�ܿ�һ��
					Thread.sleep(1000);
//					System.out.println(num1+"�����߳̿���");
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(flag==num1){
					flag=0;
					try {
						if(IO_response) {
							os1.write(IO_data);	
							}else {
							os1.write(froward_data,0,froward_datalen);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						String data=new String(froward_data, 3, froward_datalen, "utf-8");
						Date day=new Date();    
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
						System.out.print(df.format(day)+"*********");  
						System.out.println("ת������Ϊ:"+data);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(closeid==num1) {
					Date day=new Date();    
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
					System.out.println(df.format(day)+"*********"+num1+"�����̹߳ر�");
					closeid=0;
					try {
						os1.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
		}
		
	}

	public static boolean Select_User(Socket socket) {
		// TODO Auto-generated method stub
		return false;
	}

}