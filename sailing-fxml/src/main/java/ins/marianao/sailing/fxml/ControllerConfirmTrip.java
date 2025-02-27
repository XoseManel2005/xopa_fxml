package ins.marianao.sailing.fxml;

import java.util.Date;
import cat.institutmarianao.sailing.ws.model.Action;
import cat.institutmarianao.sailing.ws.model.Rescheduling;
import cat.institutmarianao.sailing.ws.model.Trip;
import ins.marianao.sailing.fxml.manager.ResourceManager;
import ins.marianao.sailing.fxml.services.ServiceSaveTrip;
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
				ResourceManager.getInstance().getText("fxml.text.viewUsers.delete.title"),
				ResourceManager.getInstance().getText("fxml.text.viewUsers.delete.text"));
		if (result) {
			if (result) {
				((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
			}
		}
	}

	@FXML
	void submit(ActionEvent event) {
		String reason = this.tfReason.getText().trim();

		// Crear Action con todos los campos obligatorios
		Action newAction = Rescheduling.builder().type(Action.Type.valueOf(Action.DONE)).idTrip(trip.getId())
				.date(new Date()).performer(ResourceManager.getInstance().getCurrentUser()).trip(trip).build();

		ServiceSaveTrip addAction;
		try {
			addAction = new ServiceSaveTrip(newAction);

			// Manejar el éxito
			addAction.setOnSucceeded(e -> {
				ControllerMenu.showInfo("Done", "El trip se ha confirmado.");
				((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
			});

			// Manejar el fallo
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
