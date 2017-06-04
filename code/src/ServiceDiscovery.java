
import javafx.beans.property.StringProperty;

import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class ServiceDiscovery extends Service {

    //Constructor
    public ServiceDiscovery(StringProperty message, String type){
        super(message, type);

        this.type = type;

        updateMessage(getLine());
        updateMessage("Setting up discovery for type:  " + type);
        updateMessage(getLine());
    }

    @Override
    protected Void call() throws Exception {

            start = System.currentTimeMillis();
            updateMessage(getTime(start) + "ServiceDiscovery started!");

            // Create a JmDNS instance
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
            updateMessage(getTime(start) + "JmDNS instance created!");

            Listener serviceListen = new Listener();
            // Add a service listener
            jmdns.addServiceListener(type, serviceListen);
            updateMessage(getTime(start) + "Added Service Listener to JmDNS instance!");

            // Wait a bit
            synchronized (Thread.currentThread()){
                Thread.currentThread().wait();
            }
            //Thread.sleep(10000);

            jmdns.removeServiceListener(type, serviceListen);
            updateMessage(getTime(start) + "Service Listener removed.");

        jmdns.close();

        updateMessage("\n");
        return null;
    }

    private class Listener implements ServiceListener {
        @Override
        public void serviceAdded(ServiceEvent event) {
            System.out.println("Service added: " + event.getInfo());
            ServiceDiscovery.this.updateMessage(getTime(start) + "Service added \n" + getServiceInfo(event)); //print in window
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            System.out.println("Service removed: " + event.getInfo());
            ServiceDiscovery.this.updateMessage(getTime(start) + "Service removed \n" + getServiceInfo(event));
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            System.out.println("Service resolved: " + event.getInfo());
            ServiceDiscovery.this.updateMessage(getTime(start) + "Service resolved \n" + getServiceInfo(event));
        }
    }

}
