package pievis.spsel.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.stage.Stage;
import pievis.spsel.model.Person;
import pievis.utils.SystemUtils;

import java.util.Date;

/**
 * Created by Pievis on 06/11/2016.
 */
public class PersonEditDialogController {

    final static int ACTION_INSERT = 0;
    final static int ACTION_UPDATE = 1;

    public TextField firstNameTextField;
    public TextField lastNameTextField;
    public TextField birthdayTextField;

    //The one to be modified
    private Person person = null;
    private int action = 0;
    private boolean confirmPressed = false;
    private Stage dialogStage;

    /**
     * Set the person context for the UPDATE action
     *
     * @param person
     */
    public void setPerson(Person person) {
        this.person = person;
        setPersonDetails(person);
    }

    /**
     * Set the action to be performed on confirm
     *
     * @param action
     */
    public void setAction(int action) {
        this.action = action;
    }

    public boolean isConfirmClicked() {
        return confirmPressed;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private boolean isInputValid() {
        boolean isValid = true;
        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();
        Date birthday = SystemUtils.parseDate(birthdayTextField.getText());

        firstNameTextField.setStyle("-fx-border-color: white;");
        birthdayTextField.setStyle("-fx-border-color: white;");
        lastNameTextField.setStyle("-fx-border-color: white;");

        if(firstName == null || firstName.equals("")){
            isValid = false;
            firstNameTextField.setStyle("-fx-border-color: #df9797;");
        }
        if(lastName == null || lastName.equals("")){
            isValid = false;
            lastNameTextField.setStyle("-fx-border-color: #df9797;");
        }

        if(birthday == null){
            isValid = false;
            birthdayTextField.setStyle("-fx-border-color: #df9797;");
        }

        return isValid;
    }

    private void setPersonDetails(Person p) {
        firstNameTextField.setText(p.getFirstName());
        lastNameTextField.setText(p.getLastName());
        birthdayTextField.setText(SystemUtils.formatDate(p.getBirthday()));
    }

    /**
     * updates the reference passed to the dialog
     */
    private void updatePersonDetails() {
        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();
        Date birthday = SystemUtils.parseDate(birthdayTextField.getText());
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setBirthday(birthday);
    }

    public void onConfirmPressed(ActionEvent actionEvent) {
        if (isInputValid()) {
            confirmPressed = true;
            updatePersonDetails();
            dialogStage.close();
        }
    }

    public void onCancelPressed(ActionEvent actionEvent) {
        dialogStage.close();
    }
}
