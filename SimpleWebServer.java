import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SimpleWebServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // 정적 파일 서빙
        server.createContext("/", new StaticFileHandler());
        server.createContext("/write", new WriteHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("서버가 http://localhost:8080 에서 실행 중입니다...");
        System.out.println("종료하려면 Ctrl+C를 누르세요.");
    }
    
    static class StaticFileHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path)) {
                path = "/index.html";
            }
            
            File file = new File("src/main/webapp" + path);
            if (file.exists()) {
                byte[] content = Files.readAllBytes(file.toPath());
                exchange.sendResponseHeaders(200, content.length);
                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
    
    static class WriteHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String response = "WriteServlet 처리 필요 - 서블릿 컨테이너가 필요합니다.";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes("UTF-8"));
            os.close();
        }
    }
}