package pievis.spsel.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pievis.spsel.Config;
import pievis.spsel.Main;
import pievis.spsel.model.Database;
import pievis.spsel.model.Meeting;
import pievis.spsel.model.Person;
import pievis.utils.SystemUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Pievis on 11/11/2016.
 */
public class MeetingsController implements Initializable {

    //General
    public TextField searchField;
    //meeting management
    public Label meetingTitleLabel;
    public Label meetingDateLabel;
    public Label selectedMeetingLabel;
    public Label evtMsgLabel;
    public TableView meetingsTableView;
    public Button newBtn;
    public Button editBtn;
    public Button deleteBtn;
    public Button selectBtn;
    public TableColumn<Meeting, String> meetTitleColumn;
    public TableColumn<Meeting, String> meetDateColumn;
    public TextField meetingTitleEditText;
    public TextField meetingDateEditText;
    //partecipants in selected meeting
    public ListView participantsListView;
    public Button addPartBtn;
    public Button setAbsentBtn;
    public Button removePartBtn;
    //absents for the meeting
    public ListView absentsListView;
    public Button removeAbsentBtn;

    //other
    Database db;
    Meeting selectedMeeting = null;
    Person selectedParticipant = null;
    Person selectedAbsent = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        db = Config.instance().getDatabase();
        initPeopleLists();
        setParticipantControl(false);
        initMeetingsTableView();
        //TODO fetch the next due and work with it if this action is set
    }

    private void initPeopleLists() {
        //setup the list cells for people
        participantsListView.setCellFactory(param -> new PeopleController.PersonListCell());
        absentsListView.setCellFactory(param -> new PeopleController.PersonListCell());

        participantsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Person p = (Person) observable.getValue();
            selectedParticipant = p;
            if (p != null) {
                boolean isAbsent = absentsListView.getItems().contains(p);
                setAbsentBtn.setDisable(isAbsent);
            }
            setParticipantControl(p != null);
            if (p != null)
                log("Selected Part: " + selectedParticipant.getFullName() + " " + selectedParticipant.getId());
        });

        absentsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Person p = (Person) observable.getValue();
            selectedAbsent = p;
            if (p != null)
                log("Selected absent: " + selectedAbsent.getFullName() + " " + selectedAbsent.getId());
        });
    }

    ObservableList<Meeting> meetingsData;
    FilteredList<Meeting> filteredMeetingsData;

    private void initMeetingsTableView() {
        List<Meeting> meetings = db.getMeetings();

        meetTitleColumn.setCellValueFactory(new PropertyValueFactory<Meeting, String>("title"));
        meetTitleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        meetTitleColumn.setOnEditCommit(event -> {
            String newVal = event.getNewValue();
            if (newVal != null && newVal.length() > 0) {
                Meeting m = event.getRowValue();
                m.setTitle(event.getNewValue());
                db.updateMeeting(m);
            }
        });
        meetDateColumn.setCellValueFactory(param -> {
            Meeting m = param.getValue();
            if (m != null) {
                Date date = m.getDate();
                if (date != null) {
                    return new SimpleStringProperty(SystemUtils.formatDate(date));
                }
            }
            return null;
        });
        meetDateColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        meetDateColumn.setOnEditCommit(event -> {
            String newVal = event.getNewValue();
            if (newVal != null) {
                Meeting m = event.getRowValue();
                Date date = SystemUtils.parseDate(newVal);
                if (date != null) {
                    m.setDate(date);
                    db.updateMeeting(m);
                }
            }
        });
        /*meetDateColumn.setCellFactory(
                column -> {
            return new TableCell<Meeting, Date>() {
                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) {
                        setText(SystemUtils.formatDate(item));
                    } else {
                        setText(null);
                    }
                }
            };
        });*/

        meetingsData = FXCollections.observableArrayList(meetings);
        //sort it before showing (desc)
        meetingsData.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        //set the filtered function to show all data at the beginning
        filteredMeetingsData = new FilteredList<Meeting>(meetingsData, meeting -> true);
        meetingsTableView.setItems(filteredMeetingsData);
        meetingsTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Meeting>() {
            @Override
            public void changed(ObservableValue observable, Meeting oldValue, Meeting newValue) {
                Meeting m = (Meeting) observable.getValue();
                if (m != null) {
                    setMeetingsSelection(m);
                    addPartBtn.setDisable(false);
                }
            }
        });

        meetingsTableView.setEditable(true); //make the user update the event data from the table


        setupMeetingsSearchField();
    }

    private void setupMeetingsSearchField() {
        if (filteredMeetingsData != null) {
            searchField.textProperty().addListener(observable -> {
                //filter results according to the text
                String filter = searchField.getText();
//                log(" called with filter " + filter);
                if (filter == null || filter.length() == 0) {
                    filteredMeetingsData.setPredicate(meeting -> true);
                } else {
                    filteredMeetingsData.setPredicate(meeting -> {
                        String searchStr = meeting.getTitle() + SystemUtils.formatDate(meeting.getDate());
                        searchStr = searchStr.toLowerCase();
                        return searchStr.contains(filter);
                    });
                }
            });
        }
    }

    private void setMeetingsSelection(Meeting meeting) {
        selectedMeeting = meeting;
        log("Selected Meeting " + selectedMeeting.getId());
        setMeetingDetails(meeting);
        setupParticipants(meeting);
        setupAbsents(meeting);
    }

    private void setMeetingDetails(Meeting meeting) {
        if (meeting != null) {
            meetingTitleLabel.setText(meeting.getTitle());
            String dateStr = SystemUtils.formatDate(meeting.getDate());
            meetingDateLabel.setText(dateStr);
            editBtn.setDisable(false);
            selectBtn.setDisable(false);
        } else {
            editBtn.setDisable(true);
            selectBtn.setDisable(true);
        }
    }


    //participants management
    private void setupParticipants(Meeting meeting) {
        List<Person> part = db.getPersonByIds(meeting.getParticipants());
        if (part != null) {
            ObservableList<Person> peopleData = FXCollections.observableArrayList(part);
            participantsListView.setItems(peopleData);
        } else {
            participantsListView.setItems(FXCollections.observableArrayList());
        }
    }

    public void addSelectedToAbsents(ActionEvent actionEvent) {
        markAsNotPresent(selectedParticipant);
    }

    private void markAsNotPresent(Person person) {
        if (person == null)
            return; //safety check
        boolean isAbsent = absentsListView.getItems().contains(person);
        if (!isAbsent) {
            selectedMeeting.addAbsent(person.getId());
            absentsListView.getItems().add(person);
        }
        db.updateMeeting(selectedMeeting);
    }

    public void removeSelectedFromParticipants(ActionEvent actionEvent) {
        if (selectedParticipant == null)
            return;
        boolean isAbsent = absentsListView.getItems().contains(selectedParticipant);
        if (isAbsent) {
            removePersonFromAbsents(selectedParticipant);
        }
        Person person = selectedParticipant;
        participantsListView.getItems().remove(person);
        selectedMeeting.removeParticipant(person.getId());
        db.updateMeeting(selectedMeeting);
        selectedParticipant = null;
        setParticipantControl(false);
    }

    private void setParticipantControl(boolean enabled) {
        boolean disabled = !enabled;
//        setAbsentBtn.setDisable(disabled);
        removePartBtn.setDisable(disabled);
        if (selectedMeeting == null)
            addPartBtn.setDisable(true);
        else
            addPartBtn.setDisable(false);
    }

    private final static String PEOPLE_SEL_DIALOG_LAYOUT_PATH = "/pievis/spsel/layout/peopleSelectionDialog.fxml";

    public void openPeopleSelectionDialog(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(PEOPLE_SEL_DIALOG_LAYOUT_PATH));
        List<Person> selection = null;
        boolean completed = false;
        try {
            Pane dialog = loader.load();
            PeopleSelectionController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(Main.getPrimaryStage());
            Scene scene = new Scene(dialog);
            dialogStage.setScene(scene);
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            selection = controller.getPeopleSelection();
            completed = controller.isSelectionComplete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (completed
                && selection != null
                && selectedMeeting != null
                && selection.size() > 0) {
            if (participantsListView.getItems() == null) {
                //for safety
                ObservableList<Person> peopleData = FXCollections.observableArrayList(selection);
                participantsListView.setItems(peopleData);
            } else {
                for (Person p : selection) {
                    boolean present = participantsListView.getItems().contains(p);
                    //no doubles
                    if (!present) {
                        participantsListView.getItems().add(p);
                        selectedMeeting.addParticipant(p.getId());
                    }
                }
            }
            db.updateMeeting(selectedMeeting);
        }
    }

    public void addNewMeeting(ActionEvent actionEvent) {
        Meeting m = getMeetingFromInput();
        if (m != null) {
            db.insertMeeting(m);
            meetingsData.add(m);
            resetMeetingFields();
        }
    }

    //return null if input is not valid
    private Meeting getMeetingFromInput() {
        String title = meetingTitleEditText.getText();
        String dateStr = meetingDateEditText.getText();
        Date date = SystemUtils.parseDate(dateStr);

        meetingTitleEditText.setStyle("-fx-border-color: white;");
        meetingDateEditText.setStyle("-fx-border-color: white;");

        boolean isValid = true;
        if (title == null || title.length() < 1) {
            isValid = false;
            meetingTitleEditText.setStyle("-fx-border-color: #df9797; -fx-border-width: 2px;");
        }
        if (date == null) {
            isValid = false;
            meetingDateEditText.setStyle("-fx-border-color: #df9797; -fx-border-width: 2px;");
        }

        if (isValid) {
            Meeting m = new Meeting();
            m.setTitle(title);
            m.setDate(date);
            return m;
        } else {
            return null;
        }
    }

    private void resetMeetingFields() {
        meetingDateEditText.clear();
        meetingTitleEditText.clear();
    }

    //absents management
    private void setupAbsents(Meeting meeting) {
        List<Person> part = db.getPersonByIds(meeting.getAbsents());
        if (part != null) {
            ObservableList<Person> peopleData = FXCollections.observableArrayList(part);
            absentsListView.setItems(peopleData);
        } else {
            absentsListView.setItems(FXCollections.observableArrayList());
        }
    }


    public void removeSelectedFromAbsents(ActionEvent actionEvent) {
        removePersonFromAbsents(selectedAbsent);
    }

    private void removePersonFromAbsents(Person person) {
        if (person == null)
            return;
        absentsListView.getItems().remove(person);
        selectedMeeting.removeAbsent(person.getId());
        db.updateMeeting(selectedMeeting);
    }

    //
    private void log(String text) {
        Config.instance().getLogger().info(text);
    }


}
