package pievis.spsel.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import pievis.spsel.Config;
import pievis.spsel.Main;
import pievis.spsel.model.Database;
import pievis.spsel.model.Person;
import pievis.utils.SystemUtils;
import sample.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.ResourceBundle;

/**
 * Created by Pievis on 06/11/2016.
 */
public class PeopleController implements Initializable {


    @FXML
    public Label lastNameLabel;
    @FXML
    public Label birthdayLabel;
    @FXML
    public Label firstNameLabel;
    public TextField searchField;
    public ListView<Person> peopleListView;
    public Button newButton;
    public Button editButton;
    public Button deleteButton;

    private ObservableList<Person> data;
    private FilteredList<Person> filteredData;

    private Person selectedPerson = null;
    private Database db;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        db = Config.get().getDatabase();
        initPeopleList();
        setupSearchField();
        clearSelection();
    }

    private void initPeopleList() {
        List<Person> people = db.getPeople();
        data = FXCollections.observableArrayList(people);
        filteredData = new FilteredList<Person>(data, p -> true);
        peopleListView.setItems(filteredData);
        peopleListView.setCellFactory(param -> new PersonListCell());

        peopleListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    Person p = observable.getValue();
                    if (p != null) {
                        log("selected  " + p.getFullName());
                        setDetailsView(p);
                    } else {
                        Config.get().getLogger().error("selectedItemHandler - Missing value in Observable");
                    }
                });
    }

    private void setDetailsView(Person p) {
        lastNameLabel.setText(p.getLastName());
        firstNameLabel.setText(p.getFirstName());
        String date = SystemUtils.formatDate(p.getBirthday());
        birthdayLabel.setText(date);
        //remember the selection
        selectedPerson = p;
        deleteButton.setDisable(false);
        newButton.setDisable(false);
        editButton.setDisable(false);
    }

    private void clearSelection(){
        lastNameLabel.setText("");
        firstNameLabel.setText("");
        birthdayLabel.setText("");
        deleteButton.setDisable(true);
        newButton.setDisable(true);
        editButton.setDisable(true);
    }

    private void setupSearchField() {
        searchField.textProperty().addListener(
                (obs) -> {
                    //filter results according to the text
                    String filter = searchField.getText();
                    log(" called with filter " + filter);
                    if (filter == null || filter.length() == 0) {
                        filteredData.setPredicate(p -> true);
                    } else {
                        filteredData.setPredicate(p -> p.getFullName().toLowerCase().contains(filter));
                    }

                });
    }

    private final static String PERSON_DIALOG_LAYOUT_PATH = "/pievis/spsel/layout/personDialog.fxml";

    private boolean openPersonDialog(Person p, int action) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(PERSON_DIALOG_LAYOUT_PATH));
        try {
            Pane dialog = loader.load();
            PersonEditDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(Main.getPrimaryStage());
            if (action == PersonEditDialogController.ACTION_INSERT) {
                dialogStage.setTitle("Add new person");
            } else {
                dialogStage.setTitle("Update person");
            }
            controller.setDialogStage(dialogStage);
            controller.setAction(action);
            controller.setPerson(p); //dialog will update the reference.

            Scene scene = new Scene(dialog);
            dialogStage.setScene(scene);

            dialogStage.showAndWait(); // wait until it's closed
            return controller.isConfirmClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void newButtonPressed(ActionEvent actionEvent) {
        Person person = new Person();
        boolean okPressed = openPersonDialog(person, PersonEditDialogController.ACTION_INSERT);
        if (okPressed) {
            int id = db.insertPerson(person);
            person.setId(id);
            data.add(person);
        }
    }

    public void editButtonPressed(ActionEvent actionEvent) {
        if(selectedPerson == null)
            return;
        boolean okPressed = openPersonDialog(selectedPerson, PersonEditDialogController.ACTION_UPDATE);
        if (okPressed) {
            db.updatePerson(selectedPerson);
            peopleListView.setItems(null);
            peopleListView.setItems(filteredData);
            setDetailsView(selectedPerson);
        }
    }

    public void deleteButtonPressed(ActionEvent actionEvent) {
        if(selectedPerson == null)
            return;
        db.deletePerson(selectedPerson);
        data.remove(selectedPerson);
        selectedPerson = null;
        clearSelection();
    }

    //
    static class PersonListCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                this.setText(item.getFullName());
            }
        }
    }

    private void log(String text) {
        Config.get().getLogger().info(text);
    }
}
