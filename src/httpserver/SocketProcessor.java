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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author user
 */
public class SocketProcessor implements Runnable {

    private final Socket socket;
    private final InputStream is;
    private final OutputStream os;
    private BufferedReader buffer;
    private Map headers = new HashMap();
    private StringBuilder content = new StringBuilder();
    private String method_path;

    public SocketProcessor(Socket s) throws IOException {

        this.socket = s;
        this.is = s.getInputStream();
        this.os = s.getOutputStream();

    }

    @Override
    public void run() {
        System.out.println("--=Client accepted=--");
        try {

            buffer = new BufferedReader(new InputStreamReader(is));
            readInputHeaders();
            readInputContent();

            writeResponse("<html><body><h1>Hello World</h1></body></html>");

        } catch (Throwable t) {
            System.err.println("ERROR: " + t.getMessage());
        } finally {
            try {
                socket.close();
            } catch (Throwable t) {
                System.err.println("ERROR: " + t.getMessage());
            }
        }
        System.out.println("--=Client processing finished=--");
    }

    private void readInputHeaders() throws Throwable {

        method_path = buffer.readLine();
        System.out.println(method_path);
        while (true) {
            String line = buffer.readLine();
            if (line == null || line.length() == 0) {
                System.out.println(headers.toString());
                break;
            }
            headers.put(line.substring(0, line.indexOf(":")).trim(), (line.substring(line.indexOf(":")+1)).trim());

        }
    }

    private void readInputContent() throws IOException {
        if (method_path != null && method_path.contains("POST")) {
            int cont_size;
            cont_size = Integer.parseInt((String) headers.get("Content-Length"));
            int i = 0;
            while (i < cont_size) {
                i++;
                content.append((char) buffer.read());
            }
            System.out.println(content.toString());
        }
    }

    private void writeResponse(String body) throws Throwable {
        String[] method_path_protacol = method_path.split(" ");
        
        if(method_path_protacol[1].equals("/echo")){
          body = method_path+"\r\n";
            for (Iterator it = headers.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                body += entry.getKey()+" : "+entry.getValue()+"\r\n";
            }
          body +=  content;
            
        }
        
        String response = "HTTP/1.1 200 OK\r\n"
                + "Server: YarServer/2009-09-09\r\n"
                + "Content-Type: text/html\r\n"
                + "Content-Length: " + body.length() + "\r\n"
                + "Connection: close\r\n\r\n";
        String result = response + body;
        os.write(result.getBytes());
        os.flush();
        headers = null;
    }

}
