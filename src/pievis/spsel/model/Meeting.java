package pievis.spsel.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Pievis on 08/11/2016.
 */
public class Meeting {

    public final static int STATE_PERSON_SELECTED = 1;
    public final static int STATE_PERSON_UNSELECTED = 0;

    int id;
    List<Integer> participants;
    List<Integer> absents;
    Date date;
    String title;
    Integer state = STATE_PERSON_UNSELECTED;
    Integer selectedPersonId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Integer> participants) {
        this.participants = participants;
    }

    public List<Integer> getAbsents() {
        return absents;
    }

    public void setAbsents(List<Integer> absents) {
        this.absents = absents;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void addAbsent(Integer personId){
        if(absents == null)
            setAbsents(new ArrayList<Integer>());
        absents.add(personId);
    }

    public void addParticipant(Integer personId){
        if(participants == null)
            setParticipants(new ArrayList<Integer>());
        participants.add(personId);
    }

    public boolean removeAbsent(Integer personId){
        return absents.remove(personId);
    }

    public boolean removeParticipant(Integer personId){
        return participants.remove(personId);
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getSelectedPersonId() {
        return selectedPersonId;
    }

    public void setSelectedPersonId(Integer selectedPersonId) {
        this.selectedPersonId = selectedPersonId;
    }
}
