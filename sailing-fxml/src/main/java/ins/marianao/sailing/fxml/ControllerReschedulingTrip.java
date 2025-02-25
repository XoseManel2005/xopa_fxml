package ins.marianao.sailing.fxml;

import cat.institutmarianao.sailing.ws.model.Departure;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class ControllerReschedulingTrip {

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSubmit;

    @FXML
    private ComboBox<Departure> cbDeparture;

    @FXML
    private DatePicker dpDate;

    @FXML
    private TextField tfReason;

    @FXML
    private BorderPane viewTripsDirectory;

    @FXML
    void cancel(ActionEvent event) {

    }

    @FXML
    void submit(ActionEvent event) {

    }

}
