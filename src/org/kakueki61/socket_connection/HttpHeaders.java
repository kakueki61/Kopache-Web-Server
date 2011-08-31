package org.kakueki61.socket_connection;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpHeaders {
	InputStream is;
	BufferedInputStream bis;
	BufferedReader br;
	PushbackInputStream pushbackInputStream;
	private String requestPath;
	private String headerStr = "";
	private String[] urlParts;
	private byte[] buf;
	private List<String> headerList;
	private Map<String ,String> headerParamsMap = null;
	private Map<String, List<String>> requestParamsMap = null;
	
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	public enum Header{
		GET, POST
	}
	private Header header;
	
	public HttpHeaders(InputStream is){
		this.is = is;
		this.bis = new BufferedInputStream(is);
		this.br = new BufferedReader(new InputStreamReader(is));
		headerList = new ArrayList<String>();
	}
	
	public void show(){
		byte[] buf = new byte[128];
		int size = 0;
		try {
			headerloop:
			while((size = bis.read(buf, 0, buf.length)) > 0){
				for(int i=0; i < buf.length; i++){
					if(buf[i] == (byte)'\r'){
						if(buf[i+1] == (byte)'\n'){
							if(buf[i+2] == (byte)'\r' && buf[i+3] == (byte)'\n'){
								break headerloop;
							}
						}else if(buf[i+1] == (byte)'\r'){
							break headerloop;
						}
					}else if(buf[i] == (byte)'\n' && buf[i+1] == (byte)'\n'){
						break headerloop;
					}
				}
				System.out.println(new String(buf, "UTF-8"));
			}
			System.out.println("complete !");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//ヘッダを分析する
	int index;
	int dataIndex;
	public Map<String, List<String>> getRequestParamsMap() throws IOException, InvalidHttpProtocolException{
		int size = 0;
		byte[] buf = new byte[256];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		byte[] headerByte = null;
		while((size = bis.read(buf)) > 0){
			baos.write(buf, 0, size);
			headerByte = baos.toByteArray();
			if(isHeaderFinished(headerByte)){
				break;
			}
		}
		headerStr = new String(headerByte, 0, index, "UTF-8");
		byte[] dataByte = new byte[headerByte.length - dataIndex];
		for(int i = 0; i < dataByte.length; i++){
			dataByte[i] = headerByte[dataIndex + i];
		}
		
		headerStr = headerStr.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
		String[] headers = headerStr.split("\n");
		for(int k = 0; k < headers.length; k++){
			headerList.add(headers[k]);
		}
		
		/** GET or POST */
		String[] items = headerList.get(0).split("\\s+");
		urlParts = items[1].split("\\?");
		requestPath = urlParts[0];
		if(urlParts.length > 1){														//GETパラメータがあるなら
			String[] requestParams = urlParts[urlParts.length -1].split("&");
			this.requestParamsMap = parseParams(requestParams);
		}
		if(items.length == 3){
			if("GET".equals(items[0])){
				header = Header.GET;
			}else if("POST".equals(items[0])){
				header = Header.POST;
			}
		}
		
		headerParamsMap = parseHeaderParams(headerList);
		
		if(header == Header.GET) System.out.println("GET !!");
		else if(header == Header.POST) System.out.println("POST !!");
		System.out.println(requestPath);
		
		if(header == Header.POST){
			baos.reset();
			String contentLengthStr = headerParamsMap.get("Content-Length".toLowerCase());
			int contentLength = contentLengthStr == null ? 0 : Integer.parseInt(contentLengthStr);
			System.out.println("contentLength(request): " + contentLengthStr + " : " + contentLength);
			if(contentLength > 0){				
				/** データ部を入れている */
				baos.write(dataByte);
				System.out.println("dataByte: " + new String(dataByte, "UTF-8"));
				/** 残りがあるなら読み込んでbaosに入れる */
				if(baos.size() < contentLength){
					while( (size = bis.read(buf) ) > 0 ){
						baos.write(buf, 0, size);
						if(baos.size() >= contentLength){
							break;
						}
					}
				}
				
				String[] requestParams = (new String(baos.toByteArray(), 0, contentLength, "UTF-8")).split("&");
				requestParamsMap = parseParams(requestParams);
			}
		}
		return requestParamsMap;
	}
	
	private Map<String, List<String>> parseParams(String[] requestParams){
		Map<String, List<String>> requestParamsMap = new HashMap<String, List<String>>();
		for(int i = 0; i < requestParams.length; i++){
			String[] keyValue = requestParams[i].split("=");
			List<String> values = requestParamsMap.get(keyValue[0]);
			if(values==null){
				values = new ArrayList<String>();
			}
			values.add(keyValue[1]);
			requestParamsMap.put(keyValue[0], values);
		}
		return requestParamsMap;
	}
	
	private Map<String, String> parseHeaderParams(List<String> headerLines){
		Map<String, String> headerParamsMap = new HashMap<String, String>();
		for(String headerLine : headerLines){
			String[] keyValue = headerLine.split(":\\s+");
			if(keyValue.length > 1){
				headerParamsMap.put(keyValue[0].trim().toLowerCase(), keyValue[1].trim());
			}
		}
		return headerParamsMap;
	}
	
	public boolean isHeaderFinished(byte[] buf){
		boolean judge = false;
		int size = buf.length;
		int i = 0;
		for(; i < size; i++){
			if(buf[i] == (byte)'\r'){
				if(buf.length > i+1 && buf[i+1] == (byte)'\n'){
					if(buf.length > i+3 && buf[i+2] == (byte)'\r' && buf[i+3] == (byte)'\n'){
						dataIndex = i + 4;
						judge = true;
						break;
					}
				}else if(buf.length > i+1 && buf[i+1] == (byte)'\r'){
					dataIndex = i + 2;
					judge = true;
					break;
				}
			}else if(buf.length > i+1 && buf[i] == (byte)'\n' && buf[i+1] == (byte)'\n'){
				dataIndex = i + 2;
				judge = true;
				break;
			}
		}
		index = i;
		return judge;
	}
	
	public boolean isHeaderFinished(byte[] buf, int size){
		boolean judge = false;
		for(; index < size; index++){
			if(buf[index] == (byte)'\r'){
				if(buf[index+1] == (byte)'\n'){
					if(buf[index+2] == (byte)'\r' && buf[index+3] == (byte)'\n'){
						dataIndex = index + 4;
						judge = true;
						break;
					}
				}else if(buf[index+1] == (byte)'\r'){
					dataIndex = index + 2;
					judge = true;
					break;
				}
			}else if(buf[index] == (byte)'\n' && buf[index+1] == (byte)'\n'){
				dataIndex = index + 2;
				judge = true;
				break;
			}
		}
		return judge;
	}
	
	public String getRequestPath(){
		return requestPath;
	}
	
	public Header getHeader() {
		return header;
	}
}
