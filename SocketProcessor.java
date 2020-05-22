/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author user
 */
public class SocketProcessor implements Runnable {
  private Socket s;
  private InputStream is;
  private OutputStream os;
  private StringBuilder req = new StringBuilder();
    
    public SocketProcessor(Socket s) throws IOException {
        
        this.s = s;
        this.is = s.getInputStream();
        this.os = s.getOutputStream();
        
    }

    @Override
    public void run() {
        try {
                readInputHeaders();
                
                writeResponse("<html><body><h1>Hello from Habrahabr</h1></body></html>");
            } catch (Throwable t) {
                /*do nothing*/
            } finally {
                try {
                    s.close();
                } catch (Throwable t) {
                    /*do nothing*/
                }
            }
            System.err.println("Client processing finished");
    }
   
    private void writeResponse(String s) throws Throwable { 
            System.out.println(req.toString());
            String metod_path=req.substring(0, 10);
            int i =  req.indexOf("HTTP");
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Server: YarServer/2009-09-09\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + s.length() + "\r\n" +
                    "Connection: close\r\n\r\n";
            String result = response + s;
            os.write(result.getBytes());
            os.flush();
            req=null;
        }

        private void readInputHeaders() throws Throwable {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while(true) {
                String s = br.readLine();
                req.append(s+"\r\n");
                if(s == null || s.trim().length() == 0) {
                    break;
                }
            }
        }
    
}
