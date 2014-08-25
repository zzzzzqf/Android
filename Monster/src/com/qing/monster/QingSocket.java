package com.qing.monster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

public class QingSocket {
	public static final String ACTION = "com.qing.monster.QingSocket";
	private Socket socket = null;
	private OutputStream os = null;
	private InputStream in = null;
	private ReadThread readThread = null;
	private ServerThread serverThread = null;
	private ClientThread clientThread = null;
	private HeartBeatThread HBThread = null;
	private CheckTimeOutThread checkTimeOutThread = null;
	private static Context context = null;
	private String IP;
	private int Port;
	private boolean IsServer = false;
	// 震动器
	private Vibrator vibrator;
	private static final String HBSend = "@@";
	private static final String HBReply = "##";
	private static final int MsgHBSend = 100;
	private static final int MsgHBReply = 101;
	private static final int MsgConnected = 102;
	private static final int MsgHBReStart = 103;
	//超时时间
	private static final long HBTimeOut = 5*1000;
	private long HBSendTime = 0;
	private long HBReplyTime = 0;
	private boolean CheckingHB = false;
	private boolean IsServerRunning = false;
	//线程池
    //存储所有客户端Socket连接对象 7   
	private static List<Socket> mClientList = new ArrayList<Socket>(); 
	//线程池 
	private ExecutorService mExecutorService;
	//ServerSocket对象 
	private ServerSocket mServerSocket;  
	private StartServerPoolThread serverPoolThread = null;
	//开启服务器
	QingSocket(Context context, String ip, int port) {
		IP = ip;
		Port = port;
		this.context = context;
		boolean a = isIPAddress(IP);
		// System.out.println("ip --> " + IP +" , "+ a);
		if (IP.equalsIgnoreCase("")) {
			IsServer = true;
			//StartServer();
			serverPoolThread = new StartServerPoolThread();
			IsServerRunning = true;
			serverPoolThread.start();
		} else if (a) {
			IsServer = false;
			StartClient();
		} else {
			Intent intent = new Intent();
			intent.putExtra("ReceivedMsg", "IP地址不合法！");
			intent.setAction(ACTION);
			context.sendBroadcast(intent);
		}
		// System.out.println("socket init succeed!");
		vibrator = (Vibrator) context
				.getSystemService(context.VIBRATOR_SERVICE);
	}

	private void StartServer() {
		if (serverThread == null) {
			serverThread = new ServerThread();
			serverThread.start();
		}
	}

	private void CloseServer() {
		System.out.println("Closing Server...");
		Close();
	}

	private void CloseClient() {
		System.out.println("Closing Client...");
		Close();
	}

	private void StartClient() {
		System.out.println("StartClient");
		if (clientThread == null) {
			clientThread = new ClientThread();
			clientThread.start();
		}
	}

	private void ReStart() {
		if (IsServer) {
			System.out.println("服务端Socket关闭！！尝试重启服务端中...");
			CloseServer();
			StartServer();
			SendMsg("服务器重启成功！");
		} else {
			System.out.println("客户端与服务端失去联系！！尝试重连中...");
			CloseClient();
			StartClient();
			SendMsg("重新连接服务器成功！");
		}
	}

	public synchronized void SendMsg(String msg) {
		System.out.println("SendMsg() Function --->!");
		if (socket == null) {
			Intent intent = new Intent();
			intent.putExtra("ReceivedMsg", "Socket为空！");
			intent.setAction(ACTION);
			context.sendBroadcast(intent);
			System.out.println("Socket为空！");
		} else {
			try {
				os.write(msg.getBytes());
				System.out.println("SendMsg() Function ---> Msg--->" + msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("SendMsg() Function ---> Msg Send Failed!!");
			}
		}
		// out.println(msg);

	}

	public void Close() {
		if (readThread != null) {
			readThread.interrupt();
			readThread = null;
		}
		if (serverThread != null) {
			serverThread.interrupt();
			serverThread = null;
		}
		if (clientThread != null) {
			clientThread.interrupt();
			clientThread = null;
		}
		if(HBThread != null)
		{
			CheckingHB = false;
			HBThread.interrupt();
			HBThread = null;
			System.out.println("HeartBeatThread closed succeed!");
		}
		if(checkTimeOutThread!=null)
		{
			checkTimeOutThread.interrupt();
			checkTimeOutThread = null;
			System.out.println("checkTimeOutThread closed succeed!");
		}
		if(serverPoolThread != null)
		{
			IsServerRunning = false;
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			serverPoolThread.interrupt();
			serverPoolThread = null;
		}
		try {
			os.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("os Close Exception");
		}
		try {
			in.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("in Close Exception");
		}

		try {
			socket.close();
			socket = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Socket closed succeed!");
	}

	// 读取数据
	private class ReadThread extends Thread {
		public void run() {

			byte[] buffer = new byte[1024];
			int bytes;
			/*
			 * InputStream mmInStream = null;
			 * 
			 * try { mmInStream = socket.getInputStream(); } catch (IOException
			 * e1) { // TODO Auto-generated catch block e1.printStackTrace(); }
			 */
			while (true) {
				try {
					// Read from the InputStream
					if ((bytes = in.read(buffer)) > 0) {
						byte[] buf_data = new byte[bytes];
						for (int i = 0; i < bytes; i++) {
							buf_data[i] = buffer[i];
						}
						String s = new String(buf_data);
						// sendBroadcast(intent);						
						System.out.println("收到数据:" + s);
						if(s.equalsIgnoreCase(HBReply))
							{
								PostMsg(MsgHBReply);
								//System.out.println("收到数据:" + s+" PostMsg(MsgHBReply)");
							}
						else
						{
							// 获得的数据在这里
							Intent intent = new Intent();
							intent.putExtra("ReceivedMsg", s);
							intent.setAction(ACTION);
							context.sendBroadcast(intent);
						}
							//SendMsg(HBReply);
						vibrator.vibrate(100);
						// ed_text_blue_address.setText(s);
						// Toast.makeText(Call_Activity.this, "收到数据:" + s,
						// Toast.LENGTH_SHORT).show();
					}
				} catch (IOException e) {
				//	System.out
					//		.println("Clent in.read() Exception ---> Trying to ReStart Client.");
					//ReStart();
				}
			}
		}
	}

	// ClientThraad
	private class ClientThread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			boolean isSucceed = false;
			while (!isSucceed) {
				try {
					socket = new Socket(IP, Port);
					isSucceed = true;
					// System.out.println("Socket init succeed!");
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					System.out.println("Socket UnknownHostException!");
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					isSucceed = false;
					try {
						sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					System.out
							.println("Socket IOException ---> Trying to Connect Server...");
					e.printStackTrace();
				}
			}

			// init I/O stream
			try {
				os = socket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("getOutputStream IOException");
				e.printStackTrace();
			}
			try {
				in = socket.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Socket Init OK!");
			PostMsg(MsgConnected);
			if (readThread == null) {
				readThread = new ReadThread();
				readThread.start();
			}
		}

	}

	// ServerThread
	private class ServerThread extends Thread {
		public void run() {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(Port);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// private Socket socket = null;
			// private OutputStream os = null;
			// private InputStream in = null;

			try {
				socket = serverSocket.accept();
				serverSocket.close();
				System.out.println(GetRemoteIp() + " is Connected!");
				// System.out.println("Client had connected!");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				os = socket.getOutputStream();
				// System.out.println("os---> !" + os);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("os IOException !");
				e.printStackTrace();
			}
			try {
				in = socket.getInputStream();
				// System.out.println("os---> !" + in);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("in IOException!");
				e.printStackTrace();
			}

			byte[] buffer = new byte[1024];
			int bytes;
			// System.out.println("start reading data !");
			while (true) {
				try {
					// Read from the InputStream
					// System.out.println("in the while!");
					if ((bytes = in.read(buffer)) > 0) {
						byte[] buf_data = new byte[bytes];
						for (int i = 0; i < bytes; i++) {
							buf_data[i] = buffer[i];
						}
						String s = new String(buf_data);
						System.out.println("String s = " + s);
						if (s.equalsIgnoreCase(HBSend))
							SendMsg(HBReply);
						else {
							Intent intent = new Intent();
							intent.putExtra("ReceivedMsg", s);
							intent.setAction(ACTION);
							context.sendBroadcast(intent);
							System.out.println("收到数据:" + s);
						}
						vibrator.vibrate(100);
					}
				} catch (IOException e) {
					// System.out.println("bytes = in.read(buffer)---> Exception");
				//	System.out
						//	.println("Server in.read() Exception ---> Trying to ReStart Server.");
					//ReStart();
				}
			}
		}

	}

	public boolean isIPAddress(String ipaddr) {
		boolean flag = false;
		Pattern pattern = Pattern
				.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
		Matcher m = pattern.matcher(ipaddr);
		flag = m.matches();
		return flag;
	}

	public void SendHeartBeat() throws IOException {
		if (socket != null) {
			System.out.println("SendHeartBeat()-->Send HB Data...");
			SendMsg(HBSend);
		} else
			System.out.println("SendHeartBeat()-->Socket is NULL!");
	}

	public boolean startPing() {
		Log.e("Ping", "startPing...");
		System.out.println("startPing...");
		boolean success = false;
		Process p = null;
		String TempIp = GetRemoteIp();
		if (TempIp != null) {
			try {
				p = Runtime.getRuntime()
						.exec("ping -c 1 -i 0.2 -W 1 " + TempIp);
				int status = p.waitFor();
				if (status == 0) {
					success = true;
				} else {
					success = false;
				}
			} catch (IOException e) {
				success = false;
			} catch (InterruptedException e) {
				success = false;
			} finally {
				p.destroy();
			}
			System.out.println("PingResult: " + TempIp + " --> " + success);
		} else
			System.out.println("PingResult: 远程IP不合法" + " --> " + success);
		return success;
	}

	private String GetRemoteIp() {
		String TempIp = socket.getInetAddress().getHostAddress();
		if (isIPAddress(TempIp))
			return TempIp;
		else {
			System.out.print("远程IP不合法！");
			return null;
		}

	}
	
	private Handler SocketMsgHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MsgHBSend: {
				HBSendTime = System.currentTimeMillis();
				System.out.println("MsgHB<<<Send>>>-->"+ HBSendTime);
				break;
			}
			case MsgHBReply: {				
				HBReplyTime = System.currentTimeMillis();
				System.out.println("MsgHB<<<Reply>>>-->"+ HBReplyTime);
				break;
			}
			case MsgConnected: {
				System.out.println("MsgConnected --> Connect to Server succeed!");
				CheckingHB = true;
				if (HBThread == null) {				
					HBThread = new HeartBeatThread();
					System.out.println("MsgConnected --> HBThread is starting");
					HBThread.start();					
				}
				if(checkTimeOutThread == null)
				{
					HBSendTime = System.currentTimeMillis();
					HBReplyTime = System.currentTimeMillis();
					checkTimeOutThread = new CheckTimeOutThread();
					System.out.println("MsgConnected --> checkTimeOutThread is starting");
					checkTimeOutThread.start();
				}
				break;
			}
			case MsgHBReStart:
			{
				System.out.println("MsgHBReStart --> ReStart()");
				ReStart();
				break;
			}
			default: {
				// do nothing
				break;
			}
			}
		}

	};
	
	private class HeartBeatThread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			System.out.println("HeartBeatThread is running");
			while(CheckingHB)
			{
				try {
					System.out.println("HeartBeatThread--> SendHeartBeat");
					SendHeartBeat();
					PostMsg(MsgHBSend);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					sleep(3*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//HBThread = null;
		}

	}

	private class CheckTimeOutThread extends Thread {

		private long TempTime = 0;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			System.out.println("CheckTimeOutThread is running");
			while(CheckingHB)
			{
				TempTime = HBSendTime - HBReplyTime;
				if(TempTime > HBTimeOut)
					PostMsg(MsgHBReStart);
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private void PostMsg(int what)
	{
		Message msg = new Message();
		msg.what = what;
		SocketMsgHandler.sendMessage(msg);
	}
	
	private class StartServerPoolThread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			try {
				mServerSocket = new ServerSocket(Port);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mExecutorService = Executors.newCachedThreadPool();
			System.out.println("thread pool is starting");
			Socket client = null;
			while (IsServerRunning) {
				try {
					client = mServerSocket.accept();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mClientList.add(client);
				try {
					mExecutorService.execute(new ThreadServer(client));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			mExecutorService.shutdown();
			try {
				mExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	// 每个客户端单独开启一个线程
	private static class ThreadServer implements Runnable {
		private Socket mSocket;
		private BufferedReader mBufferedReader;
		private PrintWriter mPrintWriter;
		private String mStrMSG;
		private Socket socket = null;
		private OutputStream os = null;
		private InputStream in = null;

		public ThreadServer(Socket socket) throws IOException {
			this.mSocket = socket;
			mBufferedReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			mStrMSG = "user:" + this.mSocket.getInetAddress() + " come total:"
					+ mClientList.size();
			try {
				os = socket.getOutputStream();
				// System.out.println("os---> !" + os);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("os IOException !");
				e.printStackTrace();
			}
			try {
				in = socket.getInputStream();
				// System.out.println("os---> !" + in);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("in IOException!");
				e.printStackTrace();
			}
			sendMessage(mStrMSG);
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

			byte[] buffer = new byte[1024];
			int bytes;
			while (true) {
				try {
					if ((bytes = in.read(buffer)) > 0) {
						byte[] buf_data = new byte[bytes];
						for (int i = 0; i < bytes; i++) {
							buf_data[i] = buffer[i];
						}
						String s = new String(buf_data);
						System.out.println("String s = " + s);

						if (s.trim().equals("exit")) {
							mClientList.remove(mSocket);
							mBufferedReader.close();
							s = "user:" + this.mSocket.getInetAddress()
									+ mClientList.size();
							mSocket.close();
							sendMessage(s);
							break;
						} else if (s.equalsIgnoreCase(HBSend))
							SendToSpecifiedClient(HBReply);
						else {
							s = "from " + mSocket.getInetAddress() + ": "+ s;
							Intent intent = new Intent();
							intent.putExtra("ReceivedMsg", s);
							intent.setAction(ACTION);
							context.sendBroadcast(intent);
							System.out.println("收到数据:" + s);
						}
					}
				} catch (IOException e) {
				}
			}

		}

		// 发送消息给所有客户端
		private void sendMessage(String str) throws IOException {
			System.out.println(str);
			for (Socket client : mClientList) {
				// mPrintWriter = new PrintWriter(client.getOutputStream(),
				// true);
				// mPrintWriter.println(mStrMSG);
				try {
					os = client.getOutputStream();
					// System.out.println("os---> !" + os);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("os IOException !");
					e.printStackTrace();
				}
				os.write(str.getBytes());
			}
		}

		// 发送消息给指定Socket
		private void SendToSpecifiedClient(String str) {
			try {
				os = mSocket.getOutputStream();
				// System.out.println("os---> !" + os);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("os IOException !");
				e.printStackTrace();
			}
			try {
				os.write(str.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
		
}
