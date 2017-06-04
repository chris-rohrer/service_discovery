import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import javax.jmdns.ServiceEvent;

public class Service extends Task<Void>{

    public long start;
    public String type;

    public Service(StringProperty message, String type){
        message.bind(this.messageProperty());
        this.type = type;

    }

    public String getTime(double start){

        long elapsedTime = (long)(System.currentTimeMillis() - start);
        String format = String.format("%%0%dd", 2);
        String centiseconds = String.format(format, (elapsedTime/10) % 100);
        String seconds = String.format(format, (elapsedTime/1000) % 60);
        String minutes = String.format(format, ((elapsedTime/1000) / 60) % 60);
        String time =  minutes + ":" + seconds + ":" + centiseconds;


        return "["+ time + "] : ";
    }

    @Override
    protected Void call() throws Exception {
        return null;
    }

    public String getServiceInfo(ServiceEvent event){
        return  "\t\t Name: " + event.getInfo().getName() + "\n" +
                "\t\t Domain: " + event.getInfo().getDomain() + "\n" +
                "\t\t Address: " + event.getInfo().getAddress() + "\n" +
                "\t\t Port: " + event.getInfo().getPort() + "\n" +
                "\t\t Type: " + event.getType() + "\n";
                //"\t\t Full Info: " + event.getInfo();
    }

    public String getLine(){
        return "----------------------------------------------------";
    }
}
