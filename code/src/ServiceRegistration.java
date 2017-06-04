import javafx.beans.property.StringProperty;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import com.sun.net.httpserver.*;

public class ServiceRegistration extends Service {

    public ServiceRegistration(StringProperty message, String type){
        super(message, type);

        updateMessage(getLine());
        updateMessage("Setting up registration with type:  " + type);
        updateMessage(getLine());
    }

    public void callUpdateMessage(String message){
        updateMessage(getTime(start) + message);
    }

    @Override
    protected Void call() throws Exception {

        start = System.currentTimeMillis();
        updateMessage(getTime(start) + "Service Registration started.");

        HttpServer server = HttpServer.create(new InetSocketAddress(1234), 0);
        server.createContext("/", new Handler(this));
        server.setExecutor(null); // creates a default executor
        server.start();
        updateMessage(getTime(start) + "HttpServer created.");

            // Create a JmDNS instance
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
            updateMessage(getTime(start) + "JmDNS instance created!");

            // Register a service
            ServiceInfo serviceInfo = ServiceInfo.create(type, "http server", 1234, "");
            jmdns.registerService(serviceInfo);
            updateMessage(getTime(start) +  "JmDNS service registered! \n" +
                                            "\t\t Name: " + serviceInfo.getName() + "\n" +
                                            "\t\t Domain: " + serviceInfo.getDomain() + "\n" +
                                            "\t\t Address: " + serviceInfo.getAddress() + "\n" +
                                            "\t\t Port: " + serviceInfo.getPort() + "\n" +
                                            "\t\t Type: " + serviceInfo.getType() + "\n");

        // Wait until notified
        synchronized (Thread.currentThread()){
            Thread.currentThread().wait();
        }

        jmdns.unregisterAllServices();
        updateMessage(getTime(start) + "JmDNS service unregistered!");
        jmdns.close();
        server.stop(0);

        updateMessage(getTime(start) + "JmDNS closed and server stopped!");
        return null;
    }

    static class Handler implements HttpHandler {

        ServiceRegistration service;

        Handler(ServiceRegistration service){
            super();
            this.service = service;
        }
        public void handle(HttpExchange t) throws IOException {

            service.callUpdateMessage(  "Incoming HTTP Request \n\n " +
                                        "\t\t Protocol: " + t.getProtocol() + "\n" +
                                        "\t\t RemoteAddress: " + t.getRemoteAddress() + "\n" +
                                        "\t\t RequestURI: " + t.getRequestURI() + "\n" +
                                        "\t\t RequestMethod: " + t.getRequestMethod() +"\n\n" +
                                        "\t\t RequestHeader Values: \n" +
                                        "\t\t Accept-Encoding: " + t.getRequestHeaders().get("Accept-Encoding") + "\n" +
                                        "\t\t Accept: " + t.getRequestHeaders().get("Accept") + "\n" +
                                        "\t\t Connection: " + t.getRequestHeaders().get("Connection") + "\n" +
                                        "\t\t Host: " + t.getRequestHeaders().get("Host") + "\n" +
                                        "\t\t User-Agent: " + t.getRequestHeaders().get("User-Agent") + "\n" +
                                        "\t\t Accept-Language: " + t.getRequestHeaders().get("Accept-Language") + "\n");

            StringBuilder pageBuilder = new StringBuilder();

            try {

                InputStream in = getClass().getResourceAsStream("/index.php");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String str;
                while ((str = reader.readLine()) != null) {
                    pageBuilder.append(str);
                }
                reader.close();

            } catch (IOException e) {

            }

            String response = pageBuilder.toString();

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}