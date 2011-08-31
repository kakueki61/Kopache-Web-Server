package org.kakueki61.socket_connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiWebServer {

	public static int PORT = 10000;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		setDebug();
		
		String documentRoot = "c://webserver";
		if(args.length == 2){
			PORT = Integer.parseInt(args[0]);
			documentRoot = args[1];
		}
		
		
		ServerSocket serverSocket = null;
		Socket socket = null;
		try{	
			serverSocket = new ServerSocket(PORT);
			serverSocket.setReuseAddress(true);
			System.out.println("サーバーが起動しました：" + serverSocket.getLocalPort());
			
			int i = 0;
			while(true){
				i++;
				socket = serverSocket.accept();
				System.out.println("接続されました　：　" + socket.getRemoteSocketAddress());
				
				PrintFileContentThread pfcThread = new PrintFileContentThread(serverSocket, socket, documentRoot, i);
				Thread thread = new Thread(pfcThread);
				thread.start();
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				if(serverSocket != null){
					serverSocket.close();
					System.out.println("接続が切れました(ServerSocket)");
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	
	public static void setDebug() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                String errorMessage = ex.toString() + "\n\n";

                StackTraceElement stackTrace[] = ex.getStackTrace();

                final StringBuilder bugReport = new StringBuilder();

                bugReport.append(errorMessage + "\n");
                for (int i = 0; i < stackTrace.length; i++) {
                    String className = stackTrace[i].getClassName();
                    int lineNumber = stackTrace[i].getLineNumber();
                    String elementString = className + " : " + String.valueOf(lineNumber);
                    bugReport.append(elementString + "\n");
                }

                System.err.println(bugReport.toString());
            }
        });
    }
}

