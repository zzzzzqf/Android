package com.qing.monster;


public class Util {
	
	public static void log() {
		log(null);
	}
	
	public static void log(String info) {
		if (info == null)
			System.out.println();
		else
			System.out.println(info);
	}
}
