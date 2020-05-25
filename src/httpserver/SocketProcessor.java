/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package httpserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
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
    private Map content_types = new HashMap();
    private Map start_line = new HashMap();
    private StringBuilder content = new StringBuilder();

    {
        content_types.put(".html", "text/html;charset=UTF-8");
        content_types.put(".htm", "text/html");
        content_types.put(".jpg", "image/jpeg");
        content_types.put(".text", "text");
        content_types.put(".css", "text/css");
        content_types.put(".js", "text/javascript;charset=UTF-8");
        content_types.put(".png", "image/png");
        content_types.put(".svg", "image/svg+xml");
        content_types.put(".woff", "font/woff");
        content_types.put(".ico", "image/x-icon");    
        content_types.put(null, "html");
    }

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
           // if (buffer.ready()) {

                readStartLine();
                readInputHeaders();
                readInputContent();

                writeResponse();
          //  }

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

        while (true) {
            String line = buffer.readLine();
            if (line == null || line.length() == 0) {
                System.out.println(headers.toString());
                break;
            }
            headers.put(line.substring(0, line.indexOf(":")).trim(), (line.substring(line.indexOf(":") + 1)).trim());

        }
    }

    private void readInputContent() throws IOException {
        if (start_line.get("method").equals("POST")) {
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

    private void writeResponse() throws Throwable {

        String current_content_type = ".html";
        Map params = new HashMap();
        byte[] fileContent = null;
        if (start_line.get("url").equals("/echo")) {
            String body = start_line + "\r\n";
            for (Iterator it = headers.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                body += entry.getKey() + " : " + entry.getValue() + "\r\n";
            }
            body += content;
            current_content_type = ".text";
            fileContent = body.getBytes();
        } else {

            String[] str = ((String) start_line.get("url")).split("\\?");
            String path = str.length != 0 ? str[0] : (String) start_line.get("url");
            String prm = str.length == 2 ? str[1] : null;

            if (prm != null) {
                String[] item = prm.split("&");
               
                    int i_item = 0;
                    do {
                        try{
                        params.put(item[i_item].split("=")[0], item[i_item].split("=")[1]);
                        }catch(Exception ee){
                           System.out.println("инвалидные параметры"); 
                        }
                        i_item++;
                    } while (item.length > i_item);
                
            }
            int ind_point = path.lastIndexOf(".");
            if (ind_point != -1) {
                current_content_type = path.substring(ind_point);
            } else {
                if (path.equals("/")) {
                    path = "/index";
                }
                path += current_content_type;
            }
            File file = new File("web" + path);
            if (file.exists()) {
                fileContent = Files.readAllBytes(file.toPath());
            } else {
                writeResponseError("404");
                return;
            }
        }

        String response = "HTTP/1.1 200 OK\r\n"
                + "Server: IzvesteenServer/2020-05-09\r\n"
                + "Content-Type: " + content_types.get(current_content_type) + "\r\n"
                + "Content-Length: " + fileContent.length + "\r\n"
                + "Connection: close\r\n\r\n";

        os.write(response.getBytes());
        os.write(fileContent);
        os.flush();
        headers = null;
    }

    private void readStartLine() throws IOException {

        String[] method_path_protacol = buffer.readLine().split(" ");
        start_line.put("method", method_path_protacol[0]);
        start_line.put("url", method_path_protacol[1]);
        start_line.put("protocol", method_path_protacol[2]);

        System.out.println(start_line);
    }

    private void writeResponseError(String string) throws IOException {

        System.out.println("файл не найден");
        String response = "HTTP/1.1 404 Not Found\r\n"
                + "Server: IzvesteenServer/2020-05-09\r\n"
                + "Connection: close\r\n\r\n";
        os.write(response.getBytes());
        os.flush();
    }

}
