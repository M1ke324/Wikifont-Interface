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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VariantController {
    private Gson gson = new Gson();
    
    //Logger
    private static final Logger logger=LogManager.getLogger(VariantController.class);
    
    //Font scelto, caricato dalla pagina precedente
    public static Font fontScelto;
    
    //Tabella
    @FXML TableView<Variants> table2=new TableView<>();
    private ObservableList<Variants> ol;
    
    //Titolo della pagina
    @FXML Text titolo;

    //Elementi addizionali che si creano quando "Add variant" viene premuto
    @FXML TextField variant;
    @FXML TextField link;
    @FXML Text error;
    @FXML Button send;
    @FXML Button show;
    @FXML private VBox vbox;
    
    //Flag per il controllo sull'aggiunta di nuove varianti
    private boolean toSend=false;
    
    //Eventi del pulsante "Add Variant"
    EventHandler<ActionEvent> eventHide;
    EventHandler<ActionEvent> eventShow;

    @FXML
    public void initialize(){
        logger.debug("Seconda schermata caricata");
        
        //Carico il titolo della pagina
        Task tit = new Task<Void>() {
            @Override
            public Void call() {
                logger.debug("Mostro il titolo");
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        titolo.setText(fontScelto.getFamily());
                        javafx.scene.text.Font font =javafx.scene.text.Font.loadFont(fontScelto.getMenu(), 48);
                        titolo.setFont(font);
                    }
                });
                return null;
            }
        };
        new Thread(tit).start();
        
        //Creazione dinamica delle colonne della tabella
        TableColumn variantCol = new TableColumn("Variant");
        variantCol.setCellValueFactory(new PropertyValueFactory<>("variant"));
        variantCol.setMaxWidth(600);
        TableColumn linkCol = new TableColumn("Link");
        linkCol.setCellValueFactory(new PropertyValueFactory<>("link"));
        
        table2.getColumns().addAll(variantCol,linkCol);
        
        ol = FXCollections.observableArrayList();
        table2.setItems(ol);
        
        //Riempimento della tabella con le varianti del font scelto
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                try {
                    URL url = new URL("http://localhost:8080/font/variants");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDoOutput(true);
                    con.setRequestMethod("POST");
                    OutputStream os = con.getOutputStream();
                    os.write(("family="+fontScelto.getFamily()).getBytes());
                    os.flush();
                    os.close();
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer content = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    
                    //Deserializzazione e riempimento della tabella
                    JsonArray json = gson.fromJson(content.toString(), JsonElement.class).getAsJsonArray();
                    for(int i=0;i<json.size();i++){
                        JsonObject d=json.get(i).getAsJsonObject();
                        Variants variant= new Variants(
                                d.get("family").getAsString(),
                                d.get("variant").getAsString(),
                                d.get("link").getAsString()
                        );
                        ol.add(variant);
                    }
                    
                    logger.debug("Dati caricati");
                } catch (IOException ex) {
                    //IOExcpetion comprende anche le possibili ProtocolException e MalformedURLException
                    logger.error(ex.getMessage());
                }
                return null;
            }
        };
        new Thread(task).start();
        
        //Viene implementato l'evento che serve a rimuovere gli elementi aggiunti con l'eventoShow
        eventHide=new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e) {
                vbox.getChildren().remove(link);
                vbox.getChildren().remove(variant);
                vbox.getChildren().remove(error);
                vbox.getChildren().remove(send);
                
                show.setText("Add Variant");
                show.setOnAction(eventShow);
            }
        };
        
        //Implementazione dell'evento di aggiunta delle verianti
        EventHandler<ActionEvent> eventAdd=new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e) {
                Task task = new Task<Void>() {
                        @Override
                        public Void call() {
                            //Controlla che tutti i campi siano stati riempiti
                            Platform.runLater(new Runnable() {
                                @Override public void run() {
                                    String stringa="";
                                    if(link.getText().isBlank())
                                        stringa+=" link";
                                    if(variant.getText().isBlank())
                                        stringa+=" variant";
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
                                    URL url = new URL("http://localhost:8080/font/addvariant");
                                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                                    con.setDoOutput(true);
                                    con.setRequestMethod("POST");
                                    con.setRequestProperty("Content-Type", "application/json");
                                    OutputStream os = con.getOutputStream();
                                    Variants variants=new Variants(
                                            fontScelto.family,
                                            variant.getText(),
                                            link.getText());
                                    os.write(gson.toJson(variants).getBytes());
                                    logger.debug(gson.toJson(variants));
                                    os.flush();
                                    os.close();
                                    con.getResponseMessage();
                                    ol.add(variants);
                                    
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
                //Aggiunge gli elementi grafici necessari per l'aggiunta delle varianti
                variant=new TextField();
                variant.setPromptText("Variant");
                variant.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
                vbox.getChildren().add(variant);
                
                link=new TextField();
                link.setPromptText("Link");
                link.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
                vbox.getChildren().add(link);
                
                error=new Text();
                error.setText("");
                vbox.getChildren().add(error);
                
                send=new Button();
                send.setText("Add Variant");
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
    
    //Funzione che mostra all'utente la variante del font
    @FXML
    public void tryIt(){
        logger.debug("Carico la variante");
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                javafx.scene.text.Font font =javafx.scene.text.Font.loadFont(table2.getSelectionModel().getSelectedItem().getLink(), 48);
                titolo.setFont(font);
                return null;
            }
        };
        new Thread(task).start();
        
    }
    
    //Torna alla schermata iniziale
    @FXML
    private void back() throws IOException {
        App.setRoot("font");
    }
    
    //Funzione che permette all'utente di copiare il link
    @FXML
    private void copyURL(){
        logger.debug("Copio l'URL");
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                Platform.runLater(new Runnable() {
                    @Override 
                    public void run() {
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        ClipboardContent content = new ClipboardContent();
                        content.putString(table2.getSelectionModel().getSelectedItem().getLink());
                        clipboard.setContent(content);
                        if(clipboard.hasString())
                            logger.debug("Copia riuscita: "+clipboard.getString());
                        else
                            logger.debug("Copia non riuscita");   
                    }
                });
                return null;
            }
        };
        new Thread(task).start();
    }
    
    //Rimozione di una variante dalla tabella e dal DB
    @FXML
    public void remove(){
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                try {
                    logger.info("Elimino "+table2.getSelectionModel().getSelectedItem().getFamily());
                    URL url = new URL("http://localhost:8080/font/deletevariant");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDoOutput(true);
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    OutputStream os = con.getOutputStream();
                    os.write(gson.toJson(table2.getSelectionModel().getSelectedItem()).getBytes());
                    os.flush();
                    os.close();
                    con.getResponseMessage();
                    ol.remove(table2.getSelectionModel().getSelectedItem());
                } catch (IOException ex) {
                    //IOException comprende anche le possibili ProtocolException e MalformedURLException
                    logger.error(ex.getMessage());
                }
                return null;
            }
        };
        new Thread(task).start();
    }
}