package org.kakueki61.socket_connection.app;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.kakueki61.socket_connection.PrintFileContentThread;

public class DataStoreManager {
	private static final String DOCUMENT_ROOT = PrintFileContentThread.DocumentRoot;
	private static final String RESULT_DATA_DIRECTORY = "/ALM/";
	private static final String RESULT_DATA_FILE = "result.dat";
	
	public static synchronized void put(String voter, String candidate) throws IOException, ClassNotFoundException, EOFException{
		ObjectInputStream ois = null;
		File file = new File(DOCUMENT_ROOT + RESULT_DATA_DIRECTORY + RESULT_DATA_FILE);
		Map<String, String> result = null;
		try {
			if(file.length() > 0){
				ois = new ObjectInputStream(new FileInputStream(file));
				result = (Map<String, String>)ois.readObject();
			}
		} catch (FileNotFoundException e) {
		} finally{
			closeStream(ois);
		}
		System.out.println("result : " + result);
		if(result == null){
			result = new HashMap<String, String>();
		}
		result.put(voter, candidate);
		ObjectOutputStream oos = null;
		try{
			oos = new ObjectOutputStream(new FileOutputStream(DOCUMENT_ROOT + RESULT_DATA_DIRECTORY + RESULT_DATA_FILE));
			oos.writeObject(result);
			oos.flush();
			oos.reset();
		} finally {
			closeStream(oos);
		}
	}
	
	
	public static synchronized Map<String, String> getResult() throws IOException, ClassNotFoundException{
		ObjectInputStream ois = null;
		Map<String, String> result = null;
		try{
			ois = new ObjectInputStream(new FileInputStream(DOCUMENT_ROOT + RESULT_DATA_DIRECTORY + RESULT_DATA_FILE));
			result = (Map<String, String>)ois.readObject();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}finally{
			closeStream(ois);
		}
		return result;
	}
	
	private static void closeStream(Closeable closeable){
		if(closeable==null)	return;
		try{
			closeable.close();
		}catch(Exception e){
			//ignore
		}
	}
}
