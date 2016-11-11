package pievis.spsel.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import pievis.spsel.Config;

import java.io.IOException;

/**
 * Created by Pievis on 05/11/2016.
 */
public class MainController {

    //private MenuBar menuBar;
    @FXML
    private BorderPane borderPane;
    public ProgressBar loadingBar;

    //Window Logic / menu
    private String currentLayout = null;
    private Object controllerRef = null;

    //layout paths
    private final static String PEOPLE_LAYOUT_PATH = "/pievis/spsel/layout/people.fxml";
    private final static String MEETINGS_LAYOUT_PATH = "/pievis/spsel/layout/meetings.fxml";

    /*
     * MenuBar control / handlers
     */

    @FXML
    public void openMeetingsListView(ActionEvent actionEvent) throws IOException {
        setLoadingProgress(0.25);
        setMainView(MEETINGS_LAYOUT_PATH);
        setLoadingProgress(1);
    }

    public void openMeetingsNewView(ActionEvent actionEvent) {
    }

    public void openMeetingsNextDueView(ActionEvent actionEvent) {
    }

    public void openPeopleListView(ActionEvent actionEvent) throws IOException {
        setLoadingProgress(0.25);
        setMainView(PEOPLE_LAYOUT_PATH);
        setLoadingProgress(1);
    }

    public void openPeopleNewView(ActionEvent actionEvent) throws IOException {
        setLoadingProgress(0.2);
        setMainView(PEOPLE_LAYOUT_PATH);
        if (controllerRef != null
                && controllerRef.getClass().equals(PeopleController.class)) {
            setLoadingProgress(0.5);
            PeopleController pc = (PeopleController) controllerRef;
            pc.newButtonPressed(new ActionEvent());
        }
        setLoadingProgress(1);
    }

    public void openAboutView(ActionEvent actionEvent) {
    }

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

    /**
     * Show a progress indicator in loading the new interface and its data
     * @param progress
     */
    private void setLoadingProgress(double progress){
        loadingBar.setVisible(true);
        loadingBar.setProgress(progress);
        if(progress >= 1 || progress <= 0){
            loadingBar.setVisible(false);
        }
    }

    private void log(String text) {
        Config.instance().getLogger().info(text);
    }
}
