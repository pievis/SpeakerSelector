package pievis.spsel.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pievis.spsel.Config;
import pievis.spsel.model.Database;
import pievis.spsel.model.Meeting;
import pievis.spsel.model.Person;
import pievis.utils.SystemUtils;

import java.net.URL;
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
    public TableColumn<Meeting, Date> meetDateColumn;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        db = Config.instance().getDatabase();

        //setup the list cells for people

        participantsListView.setCellFactory(param -> new PeopleController.PersonListCell());
        absentsListView.setCellFactory(param -> new PeopleController.PersonListCell());

        initMeetingsTableView();
        //TODO init view
        //fetch the next due and work with it if this action is set
    }

    ObservableList<Meeting> meetingsData;
    FilteredList<Meeting> filteredMeetingsData;

    private void initMeetingsTableView() {
        List<Meeting> meetings = db.getMeetings();
        meetingsTableView.setEditable(false);
        meetTitleColumn.setCellValueFactory(new PropertyValueFactory<Meeting, String>("title"));
        meetDateColumn.setCellValueFactory(new PropertyValueFactory<Meeting, Date>("date"));
        meetDateColumn.setCellFactory(column -> {
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
        });

        meetingsData = FXCollections.observableArrayList(meetings);
        //set the filtered function to show all data at the beginning
        filteredMeetingsData = new FilteredList<Meeting>(meetingsData, meeting -> true);
        meetingsTableView.setItems(filteredMeetingsData);
        meetingsTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Meeting>() {
            @Override
            public void changed(ObservableValue observable, Meeting oldValue, Meeting newValue) {
                Meeting m = (Meeting) observable.getValue();
                if (m != null) {
                    setMeetingsSelection(m);
                }
            }
        });
        setupMeetingsSearchField();
    }

    private void setupMeetingsSearchField() {
        if (filteredMeetingsData != null) {
            searchField.textProperty().addListener(observable -> {
                //filter results according to the text
                String filter = searchField.getText();
                log(" called with filter " + filter);
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
    private void setupParticipants(Meeting meeting){
        List<Person> part = db.getPersonByIds(meeting.getParticipants());
        if(part != null){
            ObservableList<Person> peopleData = FXCollections.observableArrayList(part);
            participantsListView.setItems(peopleData);
        }else{
            participantsListView.setItems(null);
        }
    }

    private void setupAbsents(Meeting meeting){
        List<Person> part = db.getPersonByIds(meeting.getAbsents());
        if(part != null){
            ObservableList<Person> peopleData = FXCollections.observableArrayList(part);
            absentsListView.setItems(peopleData);
        }else{
            absentsListView.setItems(null);
        }
    }

    //
    private void log(String text) {
        Config.instance().getLogger().info(text);
    }
}
