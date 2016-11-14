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
import java.util.*;

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
            selectBtn.setDisable(false);
            String selectedLbl = "-";
            if (meeting.getState() == Meeting.STATE_PERSON_SELECTED
                    && meeting.getSelectedPersonId() != null) {
                Person p = db.getPerson(meeting.getSelectedPersonId());
                if (p != null) {
                    selectedLbl = p.getFullName();
                }
            }
            selectedMeetingLabel.setText(selectedLbl);
        } else {
            meetingsTableView.getSelectionModel().clearSelection();
            selectBtn.setDisable(true);
        }
        setStateLabel(meeting);
    }

    private void setStateLabel(Meeting meeting) {
        String lbl = "";
        if (meeting != null) {
            if (meeting.getState() == Meeting.STATE_PERSON_SELECTED
                    && meeting.getSelectedPersonId() != null) {
                Person p = db.getPerson(meeting.getSelectedPersonId());
                if (p != null) {
                    lbl = "The person selected for the meeting is: " + p.getFullName();
                }
            } else {
                lbl = "No person has been selected for this meeting yet.";
            }
        }
        evtMsgLabel.setText(lbl);
    }

    public void deleteSelectedMeeting(ActionEvent actionEvent) {
        if (selectedMeeting != null) {
            //Show a confirm dialog
            Optional<ButtonType> result = showAndWaitConfirm("Delete the selected data?",
                    "All the information about the meeting will be removed.");
            if (result.get() == ButtonType.OK) {
                db.deleteMeeting(selectedMeeting.getId());
                meetingsData.remove(selectedMeeting);
                selectedMeeting = null;
                meetingsTableView.getSelectionModel().clearSelection();
                //update the view with no selection
                setParticipantControl(false);
                participantsListView.setItems(FXCollections.observableArrayList());
                absentsListView.setItems(FXCollections.observableArrayList());
            }
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


    //Selection process
    public void onSelectBtnPressed(ActionEvent actionEvent) {
        if (participantsListView.getItems().size() > 0) {
            if (selectedMeeting.getState() == Meeting.STATE_PERSON_UNSELECTED) {
                selectRandomForMeeting();
            } else {
                Optional<ButtonType> res = showAndWaitConfirm("Someone has been selected.",
                        "Would you like to retake?");
                if (res.get() == ButtonType.OK) {
                    selectRandomForMeeting();
                }
            }
        }
    }

    private void selectRandomForMeeting() {
        List<Person> set = new ArrayList(participantsListView.getItems());
        List<Person> absents = absentsListView.getItems();
        set.removeAll(absents);
        if (set.size() > 0) {
            //select randomly
            Random rnd = new Random();
            int i = rnd.nextInt(set.size());
            Person p = set.get(i);
            //complete dialog
            ButtonType buttonTypeOk = new ButtonType("Ok");
            ButtonType buttonTypeRetake = new ButtonType("Retake");
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Selection complete");
            alert.setHeaderText(p.getFullName() + " selected!");
            alert.setContentText("Are you ok with the choice?");
            alert.getButtonTypes().setAll(buttonTypeOk, buttonTypeRetake, buttonTypeCancel);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonTypeOk) {
                setSelectedPersonForMeeting(p);
            }
            if (result.get() == buttonTypeRetake) {
                selectRandomForMeeting();
            }
        }
    }

    void setSelectedPersonForMeeting(Person person) {
        selectedMeeting.setSelectedPersonId(person.getId());
        selectedMeeting.setState(Meeting.STATE_PERSON_SELECTED);
        db.updateMeeting(selectedMeeting);
        setMeetingDetails(selectedMeeting);
    }

    public void selectNextDue() {
        log("Selecting next due...");
        Date now = new Date();
        long minTime = Long.MAX_VALUE;
        Meeting sel = null;
        for (Meeting m : meetingsData) {
            if (now.before(m.getDate())) {
                long time = m.getDate().getTime() - now.getTime();
                if (time < minTime) {
                    sel = m;
                    minTime = time;
                }
            }
        }
        if (sel != null) {
//            log("Selected for date " + sel.getDate());
            meetingsTableView.getSelectionModel().select(sel);
        }
    }

    //
    private void log(String text) {
        Config.instance().getLogger().info(text);
    }

    private Optional<ButtonType> showAndWaitConfirm(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        return alert.showAndWait();
    }
}
