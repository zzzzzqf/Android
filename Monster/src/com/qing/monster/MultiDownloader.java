package com.qing.monster;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Environment;

public class MultiDownloader {

	private String path;
	private static final String defulat_path = "http://dldir1.qq.com/qqfile/qq/QQ5.4/11058/QQ5.4.exe";
	private int threadNum;
	public static int a = 0;
		
	public MultiDownloader() {
		this(null, 3);
	}
	public MultiDownloader(String url) {
		this(url, 3);
	}

	public MultiDownloader(String url1, int num) {

		System.out.println("MultiDownloader is called.");
		if (url1 == null)
			this.path = defulat_path;
		else
			this.path = url1;
		System.out.println("MultiDownloader::MultiDownloader = " + path);
		this.threadNum = num;
	}

	boolean start() throws IOException {

	//	String filename = android.os.Environment.getExternalStorageDirectory()
	//			.getAbsolutePath() + "/qing";
		URL url = new URL(path);
		System.out.println("MultiDownloader::start = " + url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept-Encoding", "identity"); 
		int filelength = conn.getContentLength();
		System.out.println("conn.url = " + url);
		System.out.println("conn.getContentLength = " + filelength);

		if (filelength > 0) {
			//RandomAccessFile file = new RandomAccessFile(filename, "rw");
		//	file.setLength(filelength);
		//	file.close();
			conn.disconnect();

			int threadLength = filelength % threadNum == 0 ? filelength
					/ threadNum : filelength / threadNum + 1;

			for (int i = 0; i < threadNum; i++) {
				int startPosition = i * threadLength;
				System.out.println("before start thread in for,threadNum = "
						+ threadNum);
				new DownLoadThread(i, path, startPosition, null,
						threadLength).start();
			}
		} else {
			return false;
		}
		return true;
	}

	private class DownLoadThread extends Thread {

		private int threadId;
		private int startPosition;
		private RandomAccessFile file;
		private int threadLength;
		private String path;

		public DownLoadThread(int i, String path, int startPosition,
				RandomAccessFile threadfile, int threadLength) {
			// TODO Auto-generated constructor stub
			this.threadId = i;
			this.path = path;
			this.startPosition = startPosition;
			this.file = threadfile;
			this.threadLength = threadLength;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (WifiCountService.DOWNLOAD) {
				try {
					System.out.println("DownLoadThread" + (threadId + 1)
							+ " is running.");
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(5 * 1000);
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Range", "bytes=" + startPosition+ "-");
					InputStream inStream = conn.getInputStream();
					byte[] buffer = new byte[1024];
					int len = -1;
					int length = 0;
					while (length < threadLength && (len = inStream.read(buffer)) != -1) {
						length = length + len;
						//System.out.println("Thread " + (threadId + 1)+ " had download + " + length);
					}
					if(file != null)
						file.close();
					inStream.close();
					System.out.println("Thread " + (threadId + 1) + " download succeed.");
					add();
				} catch (Exception e) {
					System.out.println("Thread " + (threadId + 1) + " download error." + e);
				}
			}
		}

	}
	
	public synchronized void add() { 
		a++;	
	}

}
