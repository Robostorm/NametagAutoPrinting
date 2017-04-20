/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nametagautoprint;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import static nametagautoprint.NametagAutoPrint.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jdom2.Element;

/**
 *
 * @author tim
 */
public class Printer {

    private final String name;
    private String ip;
    private int port;
    private String apiKey;
    private File config;
    private boolean active;
    private boolean available;

    private boolean printing;
    private Nametag nametag;

    private GridPane grid;
    private Label nameLabel;
    private TextField ipField;
    private TextField portField;
    private TextField apiKeyField;
    private TextField configField;
    private Button configButton;
    private CheckBox activeBox;
    private Button deleteButton;

    public Printer(String name) {
        this.name = name;
        ip = "127.0.0.1";
        port = 5000;
        apiKey = "ApiKey";
        config = new File("config/slic3r/mendel.ini");
        active = false;

        init();
    }

    public Printer(String name, String ip, int port, String apiKey, boolean active) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.apiKey = apiKey;
        this.config = new File("config/slic3r/mendel.ini");
        this.active = active;

        init();
    }

    // So that we don't need to repeat this in every copnstructor
    private void init() {

        grid = new GridPane();

        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setBackground(new Background(new BackgroundFill(Paint.valueOf("yellow"), CornerRadii.EMPTY, Insets.EMPTY)));
        grid.setPrefWidth(1000);
        //grid.setGridLinesVisible(true);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(20);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(30);
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setPercentWidth(7);
        ColumnConstraints column4 = new ColumnConstraints();
        column4.setPercentWidth(30);
        ColumnConstraints column5 = new ColumnConstraints();
        column5.setPercentWidth(100 - (column1.getPercentWidth() + column2.getPercentWidth() + column3.getPercentWidth() + column4.getPercentWidth()));
        column5.setHalignment(HPos.RIGHT);
        grid.getColumnConstraints().addAll(column1, column2, column3, column4, column5);

        nameLabel = new Label(name);
        nameLabel.setId("printerName");
        grid.add(nameLabel, 0, 0, 1, 3);

        ipField = new TextField(ip);
        ipField.setOnKeyTyped(e -> {
            ip = e.getText();
        });
        grid.add(ipField, 1, 0, 1, 1);

        portField = new TextField(Integer.toString(port));
        portField.setOnKeyTyped(e -> {
            port = Integer.parseInt(e.getText());
        });
        grid.add(portField, 2, 0, 1, 1);

        configField = new TextField(config.getName());
        configField.setEditable(false);
        grid.add(configField, 1, 1, 1, 1);

        configButton = new Button("Choose");
        configButton.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Config");
            fileChooser.setInitialDirectory(config.getParentFile());
            fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Slic3r Config", "ini"));
            config = fileChooser.showOpenDialog(NametagAutoPrint.getInstance().getStage());
            configField.setText(config.getName());
        });
        grid.add(configButton, 2, 1, 1, 1);

        apiKeyField = new TextField(apiKey);
        apiKeyField.setOnKeyTyped(e -> {
            apiKey = e.getText();
        });
        grid.add(apiKeyField, 3, 0, 1, 1);

        activeBox = new CheckBox();
        activeBox.setSelected(active);
        activeBox.setOnAction(e -> {
            active = activeBox.isSelected();
        });
        Label activeLabel = new Label("Active");
        activeLabel.setGraphic(activeBox);
        activeLabel.setContentDisplay(ContentDisplay.RIGHT);
        grid.add(activeLabel, 4, 0, 1, 1);

        deleteButton = new Button("Remove");
        deleteButton.setOnAction(e -> PrintMaster.removePrinter(this));
        grid.add(deleteButton, 4, 1, 1, 1);
    }

    public Pane getPane() {
        return (Pane) grid;
    }

    @Override
    public String toString() {
        return name;
    }

    public void slice(Nametag tag) {
        System.out.println(this + " is slicing " + tag);

        tag.setGcode(new File(String.format("%s/%s.gcode", NametagAutoPrint.gcodeDirectory, tag.toString())));

        String slic3rargs = String.format(" %s --output %s", tag.getStl().getAbsolutePath(), tag.getGcode().getAbsolutePath());

        if (p == null || !p.isAlive()) {
            try {

                System.out.println("Args: " + slic3rargs);

                p = Runtime.getRuntime().exec("slic3r" + slic3rargs);

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                String s;

                // read the output from the command
                while ((s = stdInput.readLine()) != null) {
                    System.out.println("Slic3r: " + s);
                }

                // read any errors from the attempted command
                while ((s = stdError.readLine()) != null) {
                    System.out.println("Slic3r: " + s);
                }

                while (p.isAlive()) {
                }
                System.out.println("Done");

            } catch (IOException e) {
                System.out.println("exception happened - here's what I know: ");
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            System.out.println("Slic3r already running. Waiting...");
        }

    }

    public boolean upload(Nametag tag) {
        System.out.println(this + " is uploading " + tag);
        boolean good = false;
        try{
            File file = new File(String.format("%s/%s.gcode", gcodeDirectory, name));
            String remotePath = "http://" + octoPrintHostName + "/api/files/local";
            if (!file.exists()) {
                System.out.println("File upload failed: file not found");
            }
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            FileBody fileBody = new FileBody(file);
            builder.addPart("file", fileBody);

            HttpPost post = new HttpPost(remotePath);

            post.setEntity(builder.build());
            post.addHeader("X-Api-Key", "08723BF9C8EE487CB4B7E3F2D989EA8F");
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(post);
            System.out.printf("Server Returned Code: %d\n", response.getStatusLine().getStatusCode());
            String message;
            switch (response.getStatusLine().getStatusCode()) {
                case 201:
                    message = "Upload Successful";
                    good = true;
                    break;
                case 400:
                    message = "File was not uploaded properly";
                    good = false;
                    break;
                case 401:
                    message = "Incorrect API Key";
                    good = false;
                    break;
                case 404:
                    message = "Either invalid save location was provided or API key was incorrect";
                    good = false;
                    break;
                case 409:
                    message = "Either you are attemping to overwirte a file being printed or printer is not operational";
                    good = false;
                    break;
                case 415:
                    message = "You attempting to upload a file other than a gcode or stl file";
                    good = false;
                    break;
                case 500:
                    message = "Internal server error, upload failed";
                    good = false;
                    break;
                default:
                    message = "Unexpected responses";
                    good = false;
                    break;
            }
            System.out.println(message);

            System.out.println("Done Uploading");
        }catch(IOException e){
            System.err.println("Error Uploding");
            good = false;
            e.printStackTrace();
        }
        return good;
    }

    public Element toElement() {
        Element printerElement = new Element("printer");
        printerElement.setAttribute("name", name);
        printerElement.setAttribute("ip", ip);
        printerElement.setAttribute("port", Integer.toString(port));
        printerElement.setAttribute("apiKey", apiKey);
        printerElement.setAttribute("file", config.getPath());
        printerElement.setAttribute("active", Boolean.toString(active));
        return printerElement;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
        ipField.setText(ip);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        portField.setText(Integer.toString(port));
    }

    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
        this.config = config;
        configField.setText(config.getName());
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        activeBox.setSelected(active);
    }

    public boolean isAvailable() {
        //return available;
        return isActive() && !isPrinting();
    }
    
    @Deprecated
    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isPrinting() {
        return printing;
    }

    public void setPrinting(boolean printing) {
        this.printing = printing;
    }

    public Nametag getNametag() {
        return nametag;
    }

    public void setNametag(Nametag nametag) {
        this.nametag = nametag;
    }
}
