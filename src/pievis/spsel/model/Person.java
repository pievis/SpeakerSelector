package pievis.spsel.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Pievis on 06/11/2016.
 */
public class Person implements Serializable {

    String firstName, lastName;
    Date birthday;
    int id;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName(){
        return firstName + " " + lastName;
    }
}
