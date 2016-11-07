package pievis.spsel.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import pievis.spsel.Config;
import sample.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.ResourceBundle;

/**
 * Created by Pievis on 05/11/2016.
 */
public class MainController {

    @FXML
    private MenuBar menuBar;
    @FXML
    private BorderPane borderPane;

    //Window Logic / menu
    private String currentLayout = null;
    private Object controllerRef = null;

    /*
     * MenuBar control / handlers
     */

    @FXML
    public void openEventsListView(ActionEvent actionEvent) throws IOException {

    }

    public void openEventsNewView(ActionEvent actionEvent) {
    }

    public void openEventsNextDueView(ActionEvent actionEvent) {
    }

    public void openPeopleListView(ActionEvent actionEvent) throws IOException {
        setMainView(PEOPLE_LAYOUT_PATH);
    }

    public void openPeopleNewView(ActionEvent actionEvent) throws IOException {
        setMainView(PEOPLE_LAYOUT_PATH);
        if (controllerRef != null
                && controllerRef.getClass().equals(PeopleController.class)) {
            PeopleController pc = (PeopleController) controllerRef;
            pc.newButtonPressed(new ActionEvent());
        }
    }

    public void openAboutView(ActionEvent actionEvent) {
    }

    //Other

    private final static String PEOPLE_LAYOUT_PATH = "/pievis/spsel/layout/people.fxml";


    /**
     * Set the main layout of the application
     *
     * @param layoutRef str reference to the selected .fxml file
     * @throws IOException
     */
    private void setMainView(String layoutRef) throws IOException {
        if (!layoutRef.equals(currentLayout)) {
            log("Switching to " + layoutRef);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(layoutRef));
            Pane layout = fxmlLoader.load();
            currentLayout = layoutRef;
            borderPane.setCenter(layout);
            controllerRef = fxmlLoader.getController();
        }
    }

    private void log(String text) {
        Config.get().getLogger().info(text);
    }
}
