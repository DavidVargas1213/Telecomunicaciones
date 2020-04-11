import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;


public class httpserver implements Runnable {
	
	static final File WEB_ROOT = new File(".");
	static final String DEFAULT_FILE = "index.html";
	static final String ERROR_NOT_FOUND = "Error_404";
	static final String METHOD_NOT_SUPPORTED = "not_supported.html";
	
	static final int PORT = 8680;
	
	static final boolean verbose = true; 
	
	private Socket connect;
	
	public httpserver(Socket a) {
		connect = a; 
	}
	
	public static void main(String[] args) {
		try {
			ServerSocket servidorConectado = new ServerSocket(PORT);
			System.out.println("Servidor iniciado");
			
			while(true) {
				httpserver myServer = new httpserver(servidorConectado.accept());
				
				if(verbose) {
					System.out.println("Conexion abierta");
				}
				
				Thread thread = new Thread(myServer);
				thread.start();
				
				
			}
		} catch (IOException e) {
			System.err.println("Error de conexion con el servidor : " + e.getMessage()); 
		}
		
	}
	
	public void run() {
		
		BufferedReader in = null; 
		PrintWriter out = null;
		BufferedOutputStream  dataOut = null; 
		String fileRequested = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			
			out = new PrintWriter(connect.getOutputStream());
			
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			
			String input = in.readLine();
			
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase();
			
			fileRequested = parse.nextToken().toLowerCase();
			
			
			if(!method.equals("GET") && !method.equals("HEAD")) {
				if (verbose) {
					System.out.println("501 :" + method + "method.");
				}
				
				File file = new File (WEB_ROOT, METHOD_NOT_SUPPORTED);
				int fileLength= (int) file.length();
				//String contentMimeType = "text/html";
				
				byte[] fileData = readFileData(file,fileLength);
				
				out.println("HTTP/1.1 501");
				
				dataOut.write(fileData, 0, fileLength);
				dataOut.flush();
				
			}else {
				if(fileRequested.endsWith("/")) {
					fileRequested+= DEFAULT_FILE;
				}
				
				File file = new File(WEB_ROOT, fileRequested);
				int fileLength= (int) file.length();
				String content = getContentType(fileRequested);
				
				if(method.equals("GET")) {
					byte [] fileData = readFileData(file,fileLength);
					
					out.println("HTTP/1.1 200 OK");
					
					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
				}
				
				if (verbose) {
					System.out.println("File " + fileRequested + "of type index" + content );
				}
			}
			
		}catch(FileNotFoundException fnfe) {
			try {
				fileNotFound(out, dataOut, fileRequested);
			}catch(IOException ioe) {
				System.err.println(ioe.getMessage());
			}
			
		} catch (IOException e) {
			System.err.println("Server error xdddd : " + e);
		}finally {
			try {
				in.close();
				out.close();
				dataOut.close();
				connect.close();
			} catch (Exception e) {
				System.err.println("Error closing : " + e.getMessage());
			}
			if(verbose) {
			System.out.println("Conexion cerrada");
		}
		}
		
		
		
	}

	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
		File file = new File(WEB_ROOT, ERROR_NOT_FOUND);
		int fileLength= (int) file.length();
		//String content = "text/htm";
		byte[] fileData = readFileData(file, fileLength);
		
		//out.println("HTTP/1.1 404 FILE NOT FOUND");
		
		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
		
		if (verbose) {
			System.out.println("File" + fileRequested + "not found");
		}
		
	}

	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn=null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		}finally {
			if(fileIn != null)
				fileIn.close();
		}
		
		return fileData;
	}
	
	private String getContentType(String fileRequested) {
		if(fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
			return "text/html";
		else
			return "text/plain";
		
	}


}
