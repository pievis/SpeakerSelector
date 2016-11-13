package pievis.spsel.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.stage.Stage;
import javafx.util.Callback;
import pievis.spsel.Config;
import pievis.spsel.model.Database;
import pievis.spsel.model.Person;
import pievis.utils.SystemUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Pievis on 13/11/2016.
 */
public class PeopleSelectionController implements Initializable {

    public TextField searchTextField;
    public ListView peopleListView;

    private Database db;
    private ObservableList<Person> peopleData;
    private FilteredList<Person> filteredPeopleData;

    private List<Person> selection;
    private Stage dialogStage;
    private boolean selectionComplete = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        db = Config.instance().getDatabase();
        initializeListView();
    }

    void initializeListView() {
        List<Person> people = db.getPeople();
        peopleData = FXCollections.observableArrayList(people);
        //use sorted list
        peopleData.sort((o1, o2) -> o1.getFullName().compareToIgnoreCase(o2.getFullName()));

        filteredPeopleData = new FilteredList<Person>(peopleData, person -> true);
        peopleListView.setItems(filteredPeopleData);

        peopleListView.setCellFactory(param -> {
            CheckBoxListCell<Person> cell = new CheckBoxListCell<Person>() {
                @Override
                public void updateItem(Person item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setText(item.getFullName());
                    }
                }
            };
            Callback<Person, ObservableValue<Boolean>> callback = new Callback<Person, ObservableValue<Boolean>>() {
                @Override
                public ObservableValue<Boolean> call(Person person) {
                    //bind this observable to the cell checkbox
                    BooleanProperty bp = new SimpleBooleanProperty();
                    bp.addListener((observable, oldValue, newValue) -> {
                        log(person.getFullName() + " state " + newValue);
                        updateSelection(person, newValue);
                    });
                    if (selection != null) {
                        //remember selection when updating the view
                        bp.setValue(selection.contains(person));
                    }
                    return bp;
                }
            };

            cell.setSelectedStateCallback(callback);
            return cell;
        });

        //add filtered list management
        initSearchField();
    }

    private void initSearchField() {
        if (filteredPeopleData != null) {
            searchTextField.textProperty().addListener(observable -> {
                //filter results according to the text
                String filter = searchTextField.getText();
                if (filter == null || filter.length() == 0) {
                    filteredPeopleData.setPredicate(person -> true);
                } else {
                    filteredPeopleData.setPredicate(person -> {
                        String searchStr = person.getFullName().toLowerCase();
                        return searchStr.contains(filter);
                    });
                }
            });
        }
    }

    /**
     * Finalize the selected people and closes the dialog
     *
     * @param actionEvent
     */
    public void completeSelection(ActionEvent actionEvent) {
        selectionComplete = true;
        dialogStage.close();
    }

    private void updateSelection(Person p, boolean state) {
        if (selection == null)
            selection = new ArrayList<>();
        if (state) {
            selection.add(p);
        } else {
            selection.remove(p);
        }
    }

    public boolean isSelectionComplete(){
        return selectionComplete;
    }

    public List<Person> getPeopleSelection() {
        return selection;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    //
    private void log(String text) {
        Config.instance().getLogger().info(text);
    }


}
