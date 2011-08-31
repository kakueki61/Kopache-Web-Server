package org.kakueki61.socket_connection.util;

import java.util.HashMap;

public class MIME {
	private String MIMEtype;
	private HashMap<String, String> MIMEmap;
	
	public MIME(){
		MIMEmap = new HashMap<String, String>();
		MIMEmap.put("txt", "text/plain");
		MIMEmap.put("htm", "text/html; charset=UTF-8");
		MIMEmap.put("html", "text/html; charset=UTF-8");
		MIMEmap.put("js", "text/javascript");
		MIMEmap.put("css", "text/css");
		MIMEmap.put("gif", "image/gif");
		MIMEmap.put("jpg", "image/jpeg");
		MIMEmap.put("jpeg", "image/jpeg");
		MIMEmap.put("png", "image/png");
	}
	
	public String getMIMEtype(String filepath){
		System.out.println(filepath);
		String[] fileSegment = filepath.split("\\.");
		for(int i=0; i<fileSegment.length; i++){
			System.out.println("fileSegment : " + fileSegment[i]);
		}
		String extension = fileSegment[fileSegment.length-1];
		
		MIMEtype = MIMEmap.get(extension);
		System.out.println("MIMEtype : " + MIMEtype);
		return MIMEtype;
	}
}
