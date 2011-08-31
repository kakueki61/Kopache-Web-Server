package org.kakueki61.socket_connection;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.kakueki61.socket_connection.HttpHeaders.Header;
import org.kakueki61.socket_connection.app.KodDispatcher;
import org.kakueki61.socket_connection.util.MIME;

public class PrintFileContentThread implements Runnable{
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
	static	{
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	private MIME mime = new MIME();
	
	private ServerSocket serverSocket;
	private Socket socket;
	private int number;
	private List<String> headerList = new ArrayList<String>();
	
	public static final String BR = System.getProperty("line.separator");
	public static String DocumentRoot = "c://webserver";
	
	private String documentRoot;
	private Dispatcher dispatcher;
	
	public PrintFileContentThread(ServerSocket serverSocket, Socket socket, String documentRoot, int number){
		this.serverSocket = serverSocket;
		this.socket = socket;
		this.number = number;
		this.dispatcher = new KodDispatcher();
		this.documentRoot = documentRoot;
		DocumentRoot = documentRoot;
	}
	
	@Override
	public void run() {
		String[] item = null;
		Map<String, List<String>> requestParamsMap = new HashMap<String, List<String>>();
		
		try{
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			
			//ヘッダを見る
			HttpHeaders httpheaders = new HttpHeaders(socket.getInputStream());
			requestParamsMap = httpheaders.getRequestParamsMap();
			
			if(requestParamsMap != null){
				for(String key : requestParamsMap.keySet()){
					System.out.println("キー：" + key + ", 値：" + requestParamsMap.get(key));
				}
			}
			
			String requestPath = httpheaders.getRequestPath();
			
			if(requestPath.endsWith(".kod")){
				
				System.out.println("KOD !!");
				
				String result = dispatcher.getHtmlPage(requestPath, requestParamsMap);
				
				Date date = new Date();
				
				List<String> header = new ArrayList<String>();
				header.add("HTTP/1.0 200 OK");
				header.add("Date: " + simpleDateFormat.format(date));
				header.add("Server: Kopache_HTTP_Server");
				header.add("Content-Length: " + result.getBytes("UTF-8").length);
				System.out.println("contentLength=" + result.getBytes("UTF-8").length);
				header.add("Connection: close");
				header.add("Content-Type: " + "text/html; charset=UTF-8");
				
				
				try{
					OutputStream os = socket.getOutputStream();
					BufferedWriter osw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
					
					//ヘッダ部
					for(int i=0; i<header.size(); i++){
						osw.write(header.get(i) + BR);
					}
					//空行
					osw.write(BR);
					osw.flush();
					
					osw.write(result);

					osw.flush();		
				}catch(FileNotFoundException e){
					System.err.println("NotFound !");
				}catch(IOException e){
					e.printStackTrace();
				}
			}else{
			
				
				String filepath = DocumentRoot + requestPath;
					System.out.println("filepath : " + filepath);
				File file = new File(filepath);
				long contentLength = file.length();
				Date date = new Date();
				String MIMEtype = mime.getMIMEtype(filepath);
				
				
				
				List<String> header = new ArrayList<String>();
				header.add("HTTP/1.0 200 OK");
				header.add("Date: " + simpleDateFormat.format(date));
				header.add("Server: Kpache_HTTP_Server");
				header.add("Content-Length: " + contentLength);
				System.out.println("contentLength=" + contentLength);
				header.add("Connection: close");
				header.add("Content-Type: " + MIMEtype);
				
				
				
				try{
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
					OutputStream os = socket.getOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
					
					//ヘッダ部
					for(int i=0; i<header.size(); i++){
						osw.write(header.get(i) + BR);
					}
					//空行
					osw.write(BR);
					osw.flush();
					//データ部
					byte[] buf = new byte[256];
					int size = 0;
					while((size = bis.read(buf) )>0){
						os.write(buf, 0, size);
					}
					os.flush();		
				}catch(FileNotFoundException e){
					System.err.println("NotFound !");
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}catch(InvalidHttpProtocolException e){
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}finally{
				try{
					if(socket != null){
						socket.close();
						System.out.println("接続が切れました(Socket)");
					}
				}catch(IOException e){
					e.printStackTrace();
				}
		}
	}
	
	public int read(byte[] buf){
		String x = "abcde";
		byte[] xBytes = x.getBytes();
		for(int i = 0 ; i<xBytes.length ; i++){
			buf[i] = xBytes[i];
		}
		return buf.length;
	
	}
}