package pievis.spsel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pievis.spsel.model.Database;
import pievis.spsel.model.FilePersistentDb;

public class Main extends Application {

    private final static String MAIN_LAYOUT_PATH = "/pievis/spsel/layout/main.fxml";
    private final static String APP_TITLE = "Speaker Selector";
    private final static String PFDB_DIR_PATH = "db";

    static private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource(MAIN_LAYOUT_PATH));
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(new Scene(root, 800, 600));
        this.primaryStage = primaryStage;
        primaryStage.show();
    }

    static public Stage getPrimaryStage(){
        return primaryStage;
    }

    public static void main(String[] args) {
        configApp();
        launch(args);
    }

    static void configApp() {
        Config conf = Config.get();
        try {
            Database db = new FilePersistentDb(PFDB_DIR_PATH);
            conf.setDatabase(db);
        }catch (Exception e){
            conf.getLogger().error("Error during db initialization");
        }
    }
}
