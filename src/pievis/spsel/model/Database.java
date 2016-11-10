package pievis.spsel.model;

import java.util.List;

/**
 * Created by Pievis on 06/11/2016.
 * Interface to access kid of data from a data source
 */
public interface Database {

    /**
     * @return not sorted list of people in the db
     */
    List<Person> getPeople();

    /**
     * @param id identifier of the person
     * @return person data
     */
    Person getPerson(int id);

    /**
     * Insert a new person in the db
     * @param person
     * @return id of the person
     */
    int insertPerson(Person person);

    /**
     * updates person based on its id
     * @param person
     */
    void updatePerson(Person person);

    /**
     * Removes a person from the db based on its id
     * @param person
     */
    void deletePerson(Person person);

    List<Meeting> getMeetings();
    Meeting getMeeting(int id);
    void deleteMeeting(int id);
    int insertMeeting(Meeting meeting);
    void updateMeeting(Meeting meeting);

}