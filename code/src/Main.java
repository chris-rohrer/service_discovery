import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.geometry.Side;

import java.lang.Thread;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage window) {

        /* Initialize and Style UI components */

        /* Stage */
        window.setTitle("Service Discovery");

        /* Main Pane */
        BorderPane border = new BorderPane();
        border.setPadding(new Insets(10, 10, 10, 10));

        /* Top Menu Items */
        Label title = new Label("service_discovery");
        title.setFont(new Font("Andale Mono", 20));

        Button discovery = new Button("discovery");
        Button registration = new Button("registration");

        ToggleButton togglePrinter = new ToggleButton("printer");
        togglePrinter.setUserData("_ipp._tcp.local.");
        ToggleButton toggleHttp = new ToggleButton("http");
        toggleHttp.setUserData("_http._tcp.local.");
        toggleHttp.setSelected(true);
        ToggleGroup discButtonGroup = new ToggleGroup();
        togglePrinter.setToggleGroup(discButtonGroup);
        toggleHttp.setToggleGroup(discButtonGroup);

        /* Top Menu */
        HBox topMenu = new HBox();
        topMenu.setPadding(new Insets(0, 10, 10,0));
        topMenu.setSpacing(10);
        topMenu.getChildren().addAll(title, registration, discovery, togglePrinter,toggleHttp);
        border.setTop(topMenu);


        /* TextField */
        TextFlow textFieldDiscovery = new TextFlow();
        textFieldDiscovery.setLineSpacing(1.75);
        textFieldDiscovery.setPadding(new Insets(10, 10, 10, 10));

        TextFlow textFieldRegistration = new TextFlow();
        textFieldRegistration.setLineSpacing(1.75);
        textFieldRegistration.setPadding(new Insets(10, 10, 10, 10));

        /* ScrollPane */
        ScrollPane scrollPaneDiscovery = new ScrollPane(textFieldDiscovery);
        scrollPaneDiscovery.setFitToWidth(true);
        scrollPaneDiscovery.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPaneDiscovery.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        ScrollPane scrollPaneRegistration = new ScrollPane(textFieldRegistration);
        scrollPaneRegistration.setFitToWidth(true);
        scrollPaneRegistration.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPaneRegistration.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        /* Tabs and TabPane*/
        Tab discoveryTab = new Tab("discovery", scrollPaneDiscovery);
        Tab registrationTab = new Tab ("registration", scrollPaneRegistration);
        TabPane textTabs = new TabPane(discoveryTab, registrationTab);
        textTabs.setSide(Side.RIGHT);
        textTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        textTabs.setStyle("");

        /* Text to bind with Task's UpdateMessage property */
        Text messageDiscovery = new Text();
        Text messageRegistration = new Text();

        /*
         *  FUNCTIONALITY
         */

        /* On Close Requests */

        discoveryTab.setOnCloseRequest(event -> {
            event.consume();
            border.setCenter(scrollPaneRegistration);
        });
        registrationTab.setOnCloseRequest(event -> {
            event.consume();
            border.setCenter(scrollPaneDiscovery);
        });

        window.setOnCloseRequest(event -> {
            event.consume();
            if(closeProgram(window, textFieldDiscovery, textFieldRegistration)){
                window.close();
                System.exit(0);
            }
        });

        /* On Action Event */

        discovery.setOnAction(event -> {

            discovery.setDisable(true);

            switchFocus(border, textTabs, discoveryTab, scrollPaneDiscovery);

            System.out.println("Starting Discovery Thread!");
            ServiceDiscovery discoveryTask = new ServiceDiscovery(messageDiscovery.textProperty(), (String) discButtonGroup.getSelectedToggle().getUserData());
            Thread discovery_thread = new Thread(discoveryTask);
            discovery_thread.setDaemon(true);
            discovery_thread.setPriority(Thread.MAX_PRIORITY);
            discovery_thread.start();

            Button stopDiscovery = new Button("stop discovery");
            stopDiscovery.setOnAction(e -> {
                synchronized (discovery_thread){
                    discovery_thread.notify();
                }
            });

            topMenu.getChildren().remove(discovery);
            topMenu.getChildren().add(2, stopDiscovery);

            discoveryTask.setOnSucceeded(e -> {
                textTabs.getSelectionModel().select(discoveryTab);
                topMenu.getChildren().remove(stopDiscovery);
                topMenu.getChildren().add(2, discovery);
                discovery.setDisable(false);
            });
        });


        registration.setOnAction(event -> {

            registration.setDisable(true);

            switchFocus(border, textTabs, registrationTab, scrollPaneRegistration);

            System.out.println("Starting Registration Thread!");
            ServiceRegistration registrationTask = new ServiceRegistration(messageRegistration.textProperty(), (String) discButtonGroup.getSelectedToggle().getUserData());
            Thread registration_thread = new Thread(registrationTask);
            registration_thread.setDaemon(true);
            registration_thread.setPriority(Thread.MAX_PRIORITY);
            registration_thread.start();


            Button stopRegistration = new Button("stop registration");
            stopRegistration.setOnAction(e -> {
                synchronized (registration_thread){
                    registration_thread.notify();
                }
            });

            topMenu.getChildren().remove(registration);
            topMenu.getChildren().add(1, stopRegistration);

            registrationTask.setOnSucceeded(e -> {
                    textTabs.getSelectionModel().select(registrationTab);
                    topMenu.getChildren().remove(stopRegistration);
                    topMenu.getChildren().add(1, registration);
                    registration.setDisable(false);
            });
        });

        /* Listeners */
        addListener(messageDiscovery, textFieldDiscovery);
        addListener(messageRegistration, textFieldRegistration);

        /* Finalize and show Window */
        Scene scene = new Scene(border, 700, 400);
        scene.getStylesheets().add("res/style.css");
        window.setScene(scene);
        window.show();

    }

    private boolean closeProgram(Stage window, TextFlow textDiscovery, TextFlow textRegistration){

        Boolean answer;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Save");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to save your data before closing?");

        ButtonType buttonYes = new ButtonType("Yes");
        ButtonType buttonNo = new ButtonType("No");
        ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonYes, buttonNo, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonYes){
            answer = true;
        } else if (result.get() == buttonNo) {
            answer = false;
        } else {
            return false;
        }

        if(answer){

            FileChooser fileChooser = new FileChooser();

            //Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);

            //Show save file dialog
            File file = fileChooser.showSaveDialog(window);

            if(file != null){
                saveFile(textDiscovery, textRegistration,  file);
            }
        }

        return true;
    }

    private void saveFile(TextFlow textFieldOne, TextFlow textFieldTwo, File file){
        System.out.println("Saving File!");

        StringBuilder content = new StringBuilder();
        String titleDiscovery = "/* DISCOVERY */ \n" ;
        content.append(titleDiscovery);

        textFieldOne.getChildren().forEach(node -> {
            if(node instanceof Text){
                content.append((((Text) node).getText()));
            }
        });

        String titleRegistration = "/* REGISTRATION */ \n";
        content.append(titleRegistration);

        textFieldTwo.getChildren().forEach(node -> {
            if(node instanceof Text){
                content.append((((Text) node).getText()));
            }
        });

        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(content.toString());
            fileWriter.close();

        } catch (IOException ex) {
            System.out.println (ex.toString());
        }
    }

    private void addListener(Text msg, TextFlow field){
        msg.textProperty().addListener((observable, oldValue, newValue) -> {
            Text newText = new Text(newValue + "\n");
            newText.setFont(new Font("Andale Mono", 12));
            if(!Objects.equals(newValue, "")) field.getChildren().add(newText);
        });
    }

    private void switchFocus(BorderPane border, TabPane tabs, Tab tab, ScrollPane scroll){

        // ScrollPane is displayed
        if(border.getCenter() instanceof ScrollPane){

            border.setCenter(tabs);
            tabs.getSelectionModel().select(tab);

        }// border.getCenter() = TabPane
        else if(border.getCenter() instanceof TabPane){

            //focus
            tabs.getSelectionModel().select(tab);

        }// border.getCenter() = null
        else {
            border.setCenter(scroll);
        }
    }
    /* END */
}