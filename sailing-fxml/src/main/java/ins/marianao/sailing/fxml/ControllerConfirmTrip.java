package ins.marianao.sailing.fxml;

import java.util.Date;
import cat.institutmarianao.sailing.ws.model.Action;
import cat.institutmarianao.sailing.ws.model.Rescheduling;
import cat.institutmarianao.sailing.ws.model.Trip;
import ins.marianao.sailing.fxml.manager.ResourceManager;
import ins.marianao.sailing.fxml.services.ServiceSaveAction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ControllerConfirmTrip {

	@FXML
	private Button btnCancel;

	@FXML
	private Button btnSubmit;

	@FXML
	private TextField tfReason;

	@FXML
	private BorderPane viewTripsDirectory;

	Trip trip = null;

	public void loadTrip(Trip trip) {
		this.trip = trip;
		System.out.println("Trip recibido correctamente" + trip);
	}

	@FXML
	void cancel(ActionEvent event) {

		boolean result = ControllerMenu.showConfirm(
				ResourceManager.getInstance().getText("Cancel done"),
				ResourceManager.getInstance().getText("Are you sure you want to cancel??"));
		if (result) {
			if (result) {
				((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
			}
		}
	}

	@FXML
	void submit(ActionEvent event) {
		String reason = this.tfReason.getText().trim();

		// Crear Action 
		Action newAction = Rescheduling.builder().type(Action.Type.valueOf(Action.DONE)).idTrip(trip.getId())
				.date(new Date()).performer(ResourceManager.getInstance().getCurrentUser()).trip(trip).build();

		ServiceSaveAction addAction;
		try {
			addAction = new ServiceSaveAction(newAction);

			addAction.setOnSucceeded(e -> {
				ControllerMenu.showInfo("Done", "El trip has been done.");
				((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
			});
			
			addAction.setOnFailed(e -> {
				Throwable exception = addAction.getException();
				System.out.println(newAction);
				System.out.println(addAction.toString());

				String errorMessage = (exception != null) ? exception.getMessage() : "Error desconocido";
				ControllerMenu.showError("Error en el registro", errorMessage, "");
			});

			// Iniciar el servicio
			addAction.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
