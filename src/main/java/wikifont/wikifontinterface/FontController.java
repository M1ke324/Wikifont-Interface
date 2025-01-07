package wikifont.wikifontinterface;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class FontController {
    private Gson gson = new Gson();
    
    //Logger
    private static final Logger logger=LogManager.getLogger(FontController.class);
    
    //Tabella
    @FXML TableView<Font> table = new TableView<>();
    private ObservableList<Font> ol;
    
    //Flag che segnala se caricadati è già stato premuto
    private static boolean caricamento=false;
    
    //Elementi della pagina
    @FXML Button caricaDati;
    @FXML Text testoProva;
    @FXML Text wikifont;
    @FXML Button show;
    
    //Elementi addizionali che si creano quando "Add Font" viene premuto
    @FXML TextField family;
    @FXML TextField version;
    @FXML TextField category;
    @FXML TextField kind;
    @FXML TextField lastModified;
    @FXML TextField menu;
    @FXML Text error;
    @FXML Button send;
    @FXML private VBox vbox;
    
    //flag per il controllo sull'aggiunta di nuovi font
    boolean toSend=false;
    
    //Eventi del pulsante "Add font"
    EventHandler<ActionEvent> eventHide;
    EventHandler<ActionEvent> eventShow;
    
    
    
    @FXML
    public void initialize(){
        logger.debug("Prima schermata caricata");
        
        //Carico il font per la scritta in pagina "Wikifont"
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                logger.debug("Mostro wikifont");
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        javafx.scene.text.Font font =javafx.scene.text.Font.loadFont("https://fonts.gstatic.com/s/aboreto/v2/5DCXAKLhwDDQ4N8blKTeA2yuxSY.ttf", 48);
                        wikifont.setFont(font);
                    }
                });
                return null;
            }
        };
        new Thread(task).start();
        
        //Se i dati sono già stati caricati e la pagina viene ricaricata il pulsante "Carica dati" viene rimosso
        if(caricamento)
            vbox.getChildren().remove(caricaDati);
        
        //Creazione dinamica delle colonne della tabella
        TableColumn familyCol = new TableColumn("Family");
        familyCol.setCellValueFactory(new PropertyValueFactory<>("family"));
        
        TableColumn versionCol = new TableColumn("Version");
        versionCol.setCellValueFactory(new PropertyValueFactory<>("version"));

        TableColumn categoryCol = new TableColumn("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn kindCol = new TableColumn("Kind");
        kindCol.setCellValueFactory(new PropertyValueFactory<>("kind"));

        TableColumn lastModifiedCol = new TableColumn("Last Modified");
        lastModifiedCol.setCellValueFactory(new PropertyValueFactory<>("lastModified"));

        table.getColumns().addAll(familyCol, versionCol, categoryCol, kindCol, lastModifiedCol);

        ol = FXCollections.observableArrayList();
        table.setItems(ol);
        
        //Implementazione del pulsante carica dati
        EventHandler<ActionEvent> eventCaricaDati=new EventHandler<ActionEvent>(){
                public void handle(ActionEvent e) {
                    //Avverto l'utente del cariccamento dati
                    //e blocco eventuali altre richieste di caricamento
                    caricaDati.setDisable(true);
                    caricaDati.setText("Caricando...");
                    Task task = new Task<Void>() {
                        @Override
                        public Void call() {
                            logger.debug("Carico i dati");
                            
                            try {
                                //Chiama /caricadati sul server 
                                URL url = new URL("http://127.0.0.1:8080/font/caricadati");
                                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                                con.setRequestMethod("GET");
                                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                                String inputLine;
                                StringBuffer content = new StringBuffer();
                                while ((inputLine = in.readLine()) != null) {
                                    content.append(inputLine);
                                }
                                in.close();
                                
                                //Deserializza la risposta e la carica sulla tabella
                                JsonArray json = gson.fromJson(content.toString(), JsonElement.class).getAsJsonArray();
                                for(int i=0;i<json.size();i++){
                                    JsonObject d=json.get(i).getAsJsonObject();
                                    Font font= new Font(
                                            d.get("family").getAsString(),
                                            d.get("version").getAsString(),
                                            d.get("lastModified").getAsString().substring(0, 10),
                                            d.get("category").getAsString(),
                                            d.get("kind").getAsString(),
                                            d.get("menu").getAsString()
                                    );
                                    ol.add(font);
                                }
                                //Il pulsante dà il feedback all'utente sulla fine del caricamento
                                Platform.runLater(new Runnable() {
                                    @Override public void run() {
                                        caricaDati.setText("Dati caricati");
                                    
                                    }
                                });
                                Thread.sleep(2000);
                                Platform.runLater(new Runnable() {
                                    @Override public void run() {
                                        vbox.getChildren().remove(caricaDati);
                                    
                                    }
                                });
                                //Si setta il flag di caricamento dati
                                caricamento=true;
                                logger.debug("Dati caricati");
                            } catch (Exception  ex) {
                                logger.error(ex.getMessage());
                                //Se il caricamento non è andato a buon fine si dà la possibilità di riprovare
                                Platform.runLater(new Runnable() {
                                    @Override public void run() {
                                        caricaDati.setDisable(false);
                                        caricaDati.setText("Carica dati");
                                    }
                                });
                            }
                            return null;
                        }
                    };
                    new Thread(task).start();
                }
            };
        caricaDati.setOnAction(eventCaricaDati);
        
        //Se al caricamento della pagina il flag è settato i dati vengono caricati in automatico
        if(caricamento){
            logger.debug("Carico automaticamente i dati");
            caricaDati.fire();
        }
        
        //Viene implementato l'evento che serve a rimuovere gli elementi aggiunti con l'eventoShow
        eventHide=new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e) {
                vbox.getChildren().remove(family);
                vbox.getChildren().remove(version);
                vbox.getChildren().remove(category);
                vbox.getChildren().remove(lastModified);
                vbox.getChildren().remove(kind);
                vbox.getChildren().remove(menu);
                vbox.getChildren().remove(error);
                vbox.getChildren().remove(send);
                
                //Fa tornare il pulsante Hide alla funzione iniziale
                show.setText("Add Font");
                show.setOnAction(eventShow);
            }
        };
        
        //Implementazione dell'evento di aggiunta dei font
        EventHandler<ActionEvent> eventAdd=new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e) {
                Task task = new Task<Void>() {
                        @Override
                        public Void call() {
                            //Controlla che tutti i campi siano stati riempiti
                            Platform.runLater(new Runnable() {
                                @Override public void run() {
                                    String stringa="";
                                    if(family.getText().isBlank())
                                        stringa+=" Family";
                                    if(version.getText().isBlank())
                                        stringa+=" Version";
                                    if(category.getText().isBlank())
                                        stringa+=" Category";
                                    if(kind.getText().isBlank())
                                        stringa+=" Kind";
                                    if(lastModified.getText().isBlank())
                                        stringa+=" Last Modified";
                                    if(menu.getText().isBlank())
                                        stringa+=" Menu";
                                    if(stringa!=""){
                                        // Avverte l'utente nel caso uno dei campi sia vuoto
                                        error.setText("Those field are blank:"+stringa);
                                        toSend=false;
                                    }else
                                        toSend=true;
                                }
                            });
                            //Se il controllo è andato a buon fine si procede con l'invio
                            if(toSend){
                                try{
                                    URL url = new URL("http://localhost:8080/font/addfont");
                                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                                    con.setDoOutput(true);
                                    con.setRequestMethod("POST");
                                    con.setRequestProperty("Content-Type", "application/json");
                                    OutputStream os = con.getOutputStream();
                                    Font font=new Font(family.getText(),version.getText(),Date.valueOf(lastModified.getText()).toString(),category.getText(),kind.getText(),menu.getText());
                                    os.write(gson.toJson(font).getBytes());
                                    logger.debug(gson.toJson(font));
                                    os.flush();
                                    os.close();
                                    con.getResponseMessage();
                                    ol.add(font);
                                    
                                    //Cancella eventuali errori precedenti
                                    Platform.runLater(new Runnable() {
                                        @Override public void run() {
                                            error.setText("");
                                        }
                                    });
                                }catch(IOException ex){
                                    logger.error(ex.getMessage());
                                }
                            }
                            return null;
                        }
                };
                new Thread(task).start();
            }
        };
        
        //Implemento show
        eventShow=new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e) {
                //Aggiunge gli elementi grafici necessari per l'aggiunta di un font
                family=new TextField();
                family.setPromptText("Family");
                family.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
                vbox.getChildren().add(family);
                
                version=new TextField();
                version.setPromptText("Version");
                version.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
                vbox.getChildren().add(version);
                
                category=new TextField();
                category.setPromptText("Category");
                category.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
                vbox.getChildren().add(category);
                
                kind=new TextField();
                kind.setPromptText("Kind");
                kind.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
                vbox.getChildren().add(kind);
                
                lastModified=new TextField();
                lastModified.setPromptText("Last Modified");
                lastModified.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
                vbox.getChildren().add(lastModified);
                
                menu=new TextField();
                menu.setPromptText("Menu");
                menu.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
                vbox.getChildren().add(menu);
                
                error=new Text();
                error.setText("");
                vbox.getChildren().add(error);
                
                send=new Button();
                send.setText("Add Font");
                send.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-background-radius: 30; -fx-padding: 10 20; -fx-font-size: 14px;");
                send.setOnAction(eventAdd);
                vbox.getChildren().add(send);
                
                //Cambia la funzione del pulsante iniziale in eventHide
                show.setText("Hide");
                show.setOnAction(eventHide);     
            }
        };
        show.setOnAction(eventShow);
    }
    
    //Rimozione di un font dalla tabella e dal DB
    @FXML
    public void remove(){
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                try {
                    logger.info("Elimino "+table.getSelectionModel().getSelectedItem().getFamily());
                    URL url = new URL("http://localhost:8080/font/deletefont");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDoOutput(true);
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    OutputStream os = con.getOutputStream();
                    os.write(gson.toJson(table.getSelectionModel().getSelectedItem()).getBytes());
                    os.flush();
                    os.close();
                    con.getResponseMessage();
                    ol.remove(table.getSelectionModel().getSelectedItem());
                } catch (IOException ex) {
                    //IOExcpetion comprende anche le possibili ProtocolException e MalformedURLException
                    logger.error(ex.getMessage());
                }
                return null;
            }
        };
        new Thread(task).start();
    }
    //Funzione che mostra all'utente il font
    @FXML
    public void tryIt(){
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                logger.debug("Mostro il font");
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        testoProva.setText(table.getSelectionModel().getSelectedItem().getFamily());
                        javafx.scene.text.Font font =javafx.scene.text.Font.loadFont(table.getSelectionModel().getSelectedItem().getMenu(), 48);
                        testoProva.setFont(font);
                    }
                });
                return null;
            }
        };
        new Thread(task).start();
    }
    
    //Funzione che cambia la scena e passa il font scelto dall'utente
    @FXML
    private void fontScelto() throws IOException {
        VariantController.fontScelto=table.getSelectionModel().getSelectedItem();
        App.setRoot("variant");
    }
}
