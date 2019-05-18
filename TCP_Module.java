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
 * 要求：
 * 		1，可以让许多客户端连接，可以用多线程解决
 * 		2，将客户端发送的数据在发回去可以用
 * 		3，将客户端发送的数据存储在特定的文件
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
	private static Map<Integer,byte[]> data_number;//数据编号
	private static int storage_key=Tool.get_mysql_datalen();//数据编号的key,从数据库中读取
	private static int storage_datalen;
	private static byte[] storage_data;
	private static byte[] froward_data;//转发数据
	private static byte[] IO_data;
	private static int froward_datalen;
	private static int flag=0;//开启线程变量
	private static int closeid=0;
	private static boolean IO_response=false;//判断是IO转发还是数据转发
	private static Map<Integer,Integer> relativelyid;//相对ID
	private static Map<Byte,OutputStream> send_data_output;
	//num用来记录不同客户端
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
				//接收客户端，此程序为阻塞式的
				Socket client=server.accept();
				//只要一有连接就开启一个新的线程，可以使多台客户端连接,使num+1
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
					System.out.println("**********在线名单*******"); 
					while(it.hasNext()) {
						int key=it.next();
						int id=relativelyid.get(key);
						System.out.println("在线ID:"+id+"***");  
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
		//相当于id
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
				//创建连接客户端的输入输出流
				is=socket.getInputStream();
				os=socket.getOutputStream();
				byte[] comm= {metoyou.get(0x5A),metoyou.get(0x80),metoyou.get(0x03)};
				byte[] storage_data_success= {metoyou.get(0x5A),metoyou.get(0x80),metoyou.get(0x06)};
				byte[] storage_data_failure= {metoyou.get(0x5A),metoyou.get(0x80),metoyou.get(0x07)};
				byte[] read_data_failure= {metoyou.get(0x5A),metoyou.get(0x80),metoyou.get(0x08)};
				byte[] buf=new byte[1024];
				int len=0;
				System.out.println("IP:"+socket.getLocalAddress().getHostAddress()+"  ");
				//获取客户端连接时间
				Date day=new Date();    
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
				System.out.print(df.format(day)+"*********");  
//				//开启监听flag线程
				//new Thread(new monitorflag(os,num)).start();
				while((len=is.read(buf))!=-1) {
					//通信测试
					if(buf[0]==metoyou.get(0x5A)&&buf[1]==metoyou.get(0x00)&&
						buf[2]==metoyou.get(0xA5)&&len==3) {
						os.write(comm);
						System.out.println(buf.toString());
					}
					//id绑定
					else if(buf[0]==metoyou.get(0x5A)&&buf[1]==metoyou.get(0x01)&&len==5) {
						//绑定相对id
						bin_relativelyid(buf[4], os);
						send_data_output.put(buf[4], os);
						//绑定绝对Id
						byte[] arrayabsoluteid= {buf[2],buf[3]};
						absoluteid.put(num, arrayabsoluteid);
						
					}
					//数据转发
					else if(buf[0]==metoyou.get(0x5A)&&buf[1]==metoyou.get(0x02)&&len>3) {
						//存储数据位
//						froward_datalen=len;
//						froward_data[0]=metoyou.get(0x5A);
//						froward_data[1]=metoyou.get(0x00);
//						froward_data[2]=metoyou.get(relativelyid.get(num));
//						for(int i=3;i<len;i++) {
//							froward_data[i]=buf[i];
//						}
						send_data_output.get(buf[2]).write(buf, 3, len);
						//flag用来监控服务器給哪个Id发送数据
//						System.out.println("*******"+youtome.get(buf[2])+"*******");
						int ln=getflag(buf[2]);
						IO_response=false;
						flag=ln;
//						System.out.println("给"+relativelyid.get(flag)+"发送信息");
					}
					//数据存储
					else if(buf[0]==metoyou.get(0x5A)&&buf[1]==metoyou.get(0x04)&&len>4) {
						//获取数据编号
						byte[] array_data_number= {buf[2],buf[3]};
						//查询数据编号是否存在
						if(data_number_isexist(array_data_number)) {
							os.write(storage_data_failure);
						}else {
							storage_key++;
							//将数据编号和他的key存储起来,方便后面查询
							data_number.put(storage_key, array_data_number);
							//获取存储的数据
							storage_datalen=len-4;//数据长度
							storage_data=new byte[storage_datalen+1];
							for(int i=0;i<len-3;i++) {
								storage_data[i]=buf[i+4];//数据内容
							}
							if(isstorage_data(storage_key,storage_data,mysql)) {
								os.write(storage_data_success);
							}else {
								os.write(storage_data_failure);
							}
						}
						}
					//读取数据
					else if(buf[0]==metoyou.get(0x5A)&&buf[1]==metoyou.get(0x05)&&len==4) {
						byte[] array_data_number= {buf[2],buf[3]};
						if(data_number_isexist(array_data_number)) {
							byte[] data=getreaddata(array_data_number,mysql);
							os.write(data,0,data.length-1);
						}else {
							//编号不存在
							os.write(read_data_failure);
						}
					}
					//IO转发
					else if(buf[0]==metoyou.get(0x5A)&&buf[1]==metoyou.get(0x03)&&
							len==6&&(buf[5]==metoyou.get(0x01)||buf[5]==metoyou.get(0x00))) {
						IO_response=true;
						IO_data[0]=buf[3];
						IO_data[1]=buf[4];
					}
					}
				closeid=num;
				socket.close();//关闭socket
				//移除ID
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				closeid=num;
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//关闭socket
				//移除ID
				absoluteid.remove(num);
				relativelyid.remove(num);
				System.out.println("id:"+num+"断开连接");
			}
			
		}
		//*************************获取数据库中的字节数组*******************************//
		private byte[] getreaddata(byte[] array_data_number,java.sql.Connection mysql) {
			//得到数据编号的id
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

		//*******************查询数据是否重复******************************//
		private static boolean data_number_isexist(byte[] array_data_number) {
			if(data_number==null) {
				return false;
			}
			Set<Integer> keyset=data_number.keySet();
			Iterator<Integer> it=keyset.iterator();
			while(it.hasNext()) {
				int key=it.next();
				//数据标号存在
				if(data_number.get(key)[0]==array_data_number[0]&&data_number.get(key)[1]==array_data_number[1]) {
					return true;
				}
			}
			return false;
		}

		//*******************数据存储******************************//
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
		//*****************遍历数组找到转发ID*********************************//
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
		
		//*******************绑定相对id********************//
		public void bin_relativelyid(byte a,OutputStream os) throws IOException {
			byte[] repeatid= {metoyou.get(0x5A),metoyou.get(0x81),metoyou.get(0x05)};
			byte[] usableid= {metoyou.get(0x5A),metoyou.get(0x81),metoyou.get(0x00)};
				//绑定相对id
				//判断Id是否重复
				System.out.println("id绑定");
				Set<Integer> keyset=relativelyid.keySet();
				Iterator<Integer> it=keyset.iterator();
				int exp=0;
				//遍历relativelyid中的value
				while(it.hasNext()) {
					int key=it.next();
					if(metoyou.get(relativelyid.get(key))==a) {
						os.write(repeatid);//id重复发送数据给客户端表示绑定失败
						exp=1;
						break;
					}
				}
				if(exp==0) {
					relativelyid.put(num,youtome.get(a));
					os.write(usableid);//id绑定成功发送数据给客户端
				}
					
			
		}
		
	}
	
	//*************************监控接受转发数据***********************************//
	private static class monitorflag implements Runnable{
		
		private OutputStream os1;
		private int num1;
		public monitorflag(OutputStream os,int num) {
			os1=os;
			num1=num;
		}
		
		public void run() {
			System.out.println(num+"无限监听线程开启");
			while(true) {
				try {
					//疑问，为什么这里要sleep否则客户端监听线程只能开一个
					Thread.sleep(1000);
//					System.out.println(num1+"无限线程开启");
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
						System.out.println("转发数据为:"+data);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(closeid==num1) {
					Date day=new Date();    
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
					System.out.println(df.format(day)+"*********"+num1+"无限线程关闭");
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