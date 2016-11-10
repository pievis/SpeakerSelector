package pievis.spsel.model;

import java.util.Date;
import java.util.List;

/**
 * Created by Pievis on 08/11/2016.
 */
public class Meeting {

    int id;
    List<Integer> participants;
    List<Integer> absents;
    Date date;
    String title;

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
}
