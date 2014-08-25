package com.qing.monster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import android.widget.Toast;

public class TxtHelper {
	public final static String SD_PATH = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath();
	public static final String FILE_INPUT_PATH = SD_PATH + "/INPUT";
	public static final String FILE_OUTPUT_PATH = SD_PATH + "/OUTPUT";
	public static final String FILE_CONFIG_PATH = SD_PATH + "/CONFIG";
	public static final String ftxt = SD_PATH + FILE_INPUT_PATH + "/abc.txt";
	private String TxtPath;

	// 设定TXT的目录方式
	enum TxtDir {
		INPUT, OUTPUT, CONFIG, DEFAULT
	};

	/*
	 * @Type Type=0,表示输入目录；Type=1,表示输出目录;Type=3,表示程序配置目录
	 */
	TxtHelper(String filename, TxtDir Type) {
		CheckDir();
		if (Type == TxtDir.INPUT || Type == TxtDir.DEFAULT) {
			TxtPath = FILE_INPUT_PATH + "//" + filename;
		} else if (Type == TxtDir.OUTPUT) {
			TxtPath = FILE_OUTPUT_PATH + "//" + filename;
		} else if (Type == TxtDir.CONFIG) {
			TxtPath = FILE_CONFIG_PATH + "//" + filename;
		}
	}

	private void CheckDir() {
		File myFilePath;
		myFilePath = new File(FILE_INPUT_PATH);
		if (!myFilePath.exists()) {
			myFilePath.mkdirs();
		}
		myFilePath = new File(FILE_OUTPUT_PATH);
		if (!myFilePath.exists()) {
			myFilePath.mkdirs();
		}
		myFilePath = new File(FILE_CONFIG_PATH);
		if (!myFilePath.exists()) {
			myFilePath.mkdirs();
		}
	}

	public boolean IsExsit() {
		File file;
		file = new File(TxtPath);
		if (!file.exists())
			return false;
		return true;
	}

	public void CreateTxt() {
		File file;
		file = new File(TxtPath);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void WriteToTxt(String content) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(TxtPath, true)));
			try {
				out.write(content);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String ReadTxt() {
		// System.out.println("start to read TXT file!");
		StringBuffer sb = new StringBuffer();
		File f = new File(TxtPath);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = "";
		try {
			while ((line = br.readLine()) != null) {
				sb.append(line);
				//System.out.println("line---->  " + line);
				// 对读出来的字符串进行处理
				/*
				 * System.out.println("line---->  " + line);
				 * System.out.println("line.length()----> " + line.length());
				 * String[] s = line.split("##"); for (String a : s) {
				 * System.out.println("a----> " + a); //
				 * System.out.println("s["+ i+ "]----> " + s[i]); }
				 */
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		line = sb.toString();
		return line;
	}

	public void DeleteTxt() {
		File file;
		file = new File(TxtPath);
		file.delete();
	}
	public void  ReadParams(List<String> list) {
		// System.out.println("start to read TXT file!");
		StringBuffer sb = new StringBuffer();
		File f = new File(TxtPath);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = "";
		try {
			while ((line = br.readLine()) != null) {
				sb.append(line);
				list.add(line);
				// 对读出来的字符串进行处理
				/*
				 * System.out.println("line---->  " + line);
				 * System.out.println("line.length()----> " + line.length());
				 * String[] s = line.split("##"); for (String a : s) {
				 * System.out.println("a----> " + a); //
				 * System.out.println("s["+ i+ "]----> " + s[i]); }
				 */
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
