package ins.marianao.sailing.fxml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;


import org.apache.commons.lang3.exception.ExceptionUtils;

import com.itextpdf.styledxmlparser.exceptions.StyledXMLParserException;

import cat.institutmarianao.sailing.ws.model.Admin;
import cat.institutmarianao.sailing.ws.model.Client;
import cat.institutmarianao.sailing.ws.model.User;
import cat.institutmarianao.sailing.ws.model.User.Role;
import ins.marianao.sailing.fxml.exception.OnFailedEventHandler;
import ins.marianao.sailing.fxml.manager.ResourceManager;
import ins.marianao.sailing.fxml.services.ServiceAuthenticate;
import ins.marianao.sailing.fxml.services.ServiceSaveBase;
import ins.marianao.sailing.fxml.services.ServiceSaveUser;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

public class ControllerMenu implements Initializable {
	@FXML private BorderPane appRootPane;
	@FXML private AnchorPane portviewPane;

	@FXML private MenuBar menuBar;
	@FXML private Menu mnTrips;
	@FXML private MenuItem mnItBooking;
	@FXML private MenuItem mnItTrips;
	@FXML private Menu mnUsers;
	@FXML private MenuItem mnItAddUser;
	@FXML private MenuItem mnItUserDirectory;
	@FXML private MenuItem mnItImport;
	@FXML private MenuItem mnItExport;
	@FXML private Menu mnProfile;
	@FXML private MenuItem mnItEditProfile;
	@FXML private MenuItem mnItLogoff;
	@FXML private Menu mnLogin;
	@FXML private MenuItem mnItLogin;
	@FXML private MenuItem mnItRegister;


	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle resource) {
		this.logOff();
	}



	/**
	 * Called when Logoff menuItem is fired.
	 *
	 * @param event the action event.
	 */
	@FXML
	public void exitClick(ActionEvent event) {

		System.exit(0);
	}
	
	/**
	 * Called when Logoff menuItem is fired.
	 *
	 * @param event the action event.
	 */
	@FXML
	public void logoffClick(ActionEvent event) {

		this.logOff();
	}

	/**
	 * Called when Booking menuItem is fired.
	 *
	 */
	@FXML
	public void loginMenuClick() {
		try {
			BorderPane vista = (BorderPane)FXMLLoader.load(getClass().getResource("ViewFormLogin.fxml"), ResourceManager.getInstance().getTranslationBundle());

			this.loadView(vista);
		} catch (Exception e) {
			ControllerMenu.showError(ResourceManager.getInstance().getText("error.menu.view.opening"), e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}
	
	
	/**
	 * Called when Booking menuItem is fired.
	 *
	 * @param event the action event.
	 */
	@FXML
	public void bookingMenuClick() {
		try {
			// TODO Open trip types view. Remove empty view 
			BorderPane vista = new BorderPane();

			this.loadView(vista);
		} catch (Exception e) {
			ControllerMenu.showError(ResourceManager.getInstance().getText("error.menu.view.opening"), e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Called when Trips menuItem is fired.
	 *
	 */
	@FXML
	public void tripsMenuClick() {
		try {
			// TODO Open trips view. Remove empty view 
			BorderPane vista = new BorderPane();

			this.loadView(vista);
		} catch (Exception e) {
			ControllerMenu.showError(ResourceManager.getInstance().getText("error.menu.view.opening"), e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}

	

	/**
	 * Called when New User or Register menuItem is fired.
	 *
	 * @param event the action event.
	 */
	@FXML
	public void newUserMenuClick(ActionEvent event) {
		this.openUserForm(null, true);
	}

	/**
	 * Called when Users directory menuItem is fired.
	 *
	 * @param event the action event.
	 */
	@FXML
	public void usersDirectoryMenuClick(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewUsersDirectory.fxml"), ResourceManager.getInstance().getTranslationBundle());
			BorderPane vista = (BorderPane) loader.load();

			this.loadView(vista);
		} catch (Exception e) {
			ControllerMenu.showError(ResourceManager.getInstance().getText("error.menu.view.opening"), e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}
	
	/**
	 * Called when Import users menuItem is fired.
	 *
	 * @param event the action event.
	 */
	@FXML
	public void importUsersMenuClick(ActionEvent event) {
		try {
			File file = this.openFileChooser(ResourceManager.getInstance().getText("error.menu.file.open"), true); 
			
			if (file != null) {
				//SortedSet<User> imported = ResourceManager.getInstance().importUsers(file);

				// TODO Persist imported users
			}
		} catch (Exception e) {
			ControllerMenu.showError(ResourceManager.getInstance().getText("error.menu.import"), e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}
	

	/**
	 * Called when Export menuItem is fired.
	 *
	 * @param event the action event.
	 */
	@FXML
	public void exportUsersMenuClick(ActionEvent event) {
		try {
			File file = this.openFileChooser(ResourceManager.getInstance().getText("error.menu.file.write"), false); 
			
			if (file != null) {
				// TODO Export all users
				
				// ResourceManager.getInstance().exportUsers(file, toExport);
			}
		} catch (Exception e) {
			ControllerMenu.showError(ResourceManager.getInstance().getText("error.menu.export"), e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}
	
	/**
	 * Called when Edit Profile menuItem is fired.
	 *
	 * @param event the action event.
	 */
	@FXML
	public void editProfileMenuClick(ActionEvent event) {
		this.openUserForm(ResourceManager.getInstance().getCurrentUser(), false);
	}

	/**
	 * Called when About menuItem is fired.
	 *
	 * @param event the action event.
	 * @throws IOException
	 */
	@FXML
	public void aboutMenuClick(ActionEvent event) {

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("About ...");
		alert.setHeaderText(null);
		alert.setContentText("Copyright@" + Calendar.getInstance(new Locale("CA","ES")).get(Calendar.YEAR) + "\nÀlex Macia");
		alert.showAndWait();
	}
	
	private void logOff() {
		try {
			ResourceManager.getInstance().setCurrentUser(null); // Logoff

			// TODO Open trip types view

			this.logoffMenu();
			
		} catch (Exception e) {
			ControllerMenu.showError(ResourceManager.getInstance().getText("error.menu.view.opening"), e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}
	
	public void login(String username, String password) {
		try {
			final ServiceAuthenticate login = new ServiceAuthenticate(username, password);
			
			login.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

	            @Override
	            public void handle(WorkerStateEvent t) {
	            	Pair<User, String> loginResponse = login.getValue();
	    			
	            	ResourceManager.getInstance().setCurrentUser(loginResponse.getKey()); // Login user
	            	ResourceManager.getInstance().setCurrentToken(loginResponse.getValue()); // Token

	            	enableMenu();
	            	
	            	if (ResourceManager.getInstance().isAdmin()) tripsMenuClick();
	            	else bookingMenuClick();
	            }
	        });
			
			login.setOnFailed(new OnFailedEventHandler(ResourceManager.getInstance().getText("error.menu.login")));
			
			login.start();			

		} catch (Exception e) {
			ControllerMenu.showError(ResourceManager.getInstance().getText("error.menu.login"), e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}
	
	public void signin(String username, String password, String confirm, String name, Integer phone, User.Role role, boolean isUpdating) {
	    try {
	    	
	    	if (!password.equals(confirm)) {
	    		ControllerMenu.showError("Registro fallido", "Las contraseñas deben coincidir.", "");
                return;
			}
	    	
	        User newUser;
	        
	        if (role == User.Role.ADMIN) {  //preguntar esto, poner texto en password y confirm, hacer que aparzcan cosas distintas si es admin o cliente
	        	newUser = Admin.builder()
	                           .username(username)
	                           .password(password)
	                           .role(role)
	                           .build();
	        } else if (role == User.Role.CLIENT) { 
	            newUser = Client.builder()
	                           .username(username)
	                           .password(password)
	                           .fullName(name)
	                           .phone(phone)
	                           .role(role)
	                           .build();
	        } else {
	            ControllerMenu.showError("Error en el registro", "Rol no válido: " + role, "");
	            return;
	        }

	        final ServiceSaveUser signin = new ServiceSaveUser(newUser, isUpdating);

	        signin.setOnSucceeded(event -> {
	            User signedUser = signin.getValue();

	            if (signedUser == null) {
	                ControllerMenu.showError("Registro fallido", "No se pudo registrar el usuario.", "");
	                return;
	            }

	            ResourceManager.getInstance().setCurrentUser(signedUser);

	            loginMenuClick();
	        });


	        signin.setOnFailed(event -> {
	            Throwable exception = signin.getException();
	            String errorMessage = (exception != null) ? exception.getMessage() : "Error desconocido";
	            ControllerMenu.showError("Error en el registro", errorMessage, "");
	        });

	        signin.start();

	    } catch (Exception e) {
	        ControllerMenu.showError("Error en el registro", e.getMessage(), ExceptionUtils.getStackTrace(e));
	    }
	}




	public void enableMenu() {
		this.mnTrips.setVisible(true);
		this.mnProfile.setVisible(true);
		this.mnLogin.setVisible(false);
		
		if (ResourceManager.getInstance().isAdmin()) { 
			this.mnUsers.setVisible(true);
			this.mnItBooking.setVisible(false);
		} else {
			this.mnUsers.setVisible(false);
			this.mnItBooking.setVisible(true);
		}
	}

	private void logoffMenu() {
		//this.mnTrips.setVisible(false);
		this.mnTrips.setVisible(false);
		this.mnUsers.setVisible(false);
		this.mnProfile.setVisible(false);
		this.mnLogin.setVisible(true);
	}
	
	public void loadView(Pane vista) {
		if (vista == null) return;

		if (checkViewAlreadyLoaded(vista.getId())) return;

		this.portviewPane.getChildren().clear();

		//appRootPane.setPrefHeight(appRootPane.getHeight() - 80.0);

		this.portviewPane.getChildren().add(vista);

		AnchorPane.setTopAnchor(vista,0.0);
		AnchorPane.setBottomAnchor(vista,0.0);
		AnchorPane.setLeftAnchor(vista, 0.0);
		AnchorPane.setRightAnchor(vista, 0.0);
		//this.portviewPane.setVisible(true);
	}

	private boolean checkViewAlreadyLoaded(String id) {
		if (id == null || this.portviewPane != null || this.portviewPane.getChildren() != null) return false;

		Iterator<Node> fills = this.portviewPane.getChildren().iterator();

		while (fills.hasNext()) {
			Node aux = fills.next();
			if (id.equals(aux.getId())) return true;
		}

		return false;
	}

	public void openUserForm(User user, boolean isNewUser) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewFormUser.fxml"), ResourceManager.getInstance().getTranslationBundle());
			BorderPane vista = (BorderPane)loader.load();

			ControllerFormUser controllerFormUser = loader.getController();
			
			if (isNewUser) {
	            controllerFormUser.prepareForNewUser(); //nuevo registro
	        } else {
	            controllerFormUser.loadUserProfile(user);  //cargar perfil
	        }
			
			this.loadView(vista);
		} catch (Exception e) {
			e.printStackTrace();
			ControllerMenu.showError(ResourceManager.getInstance().getText("error.menu.view.opening"), e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}

	public static Button addIconToButton(Button button, Image image, int size) {
		ImageView logo = new ImageView(image);
		logo.setFitWidth(size);
		logo.setFitHeight(size);
		button.setGraphic(logo);
		button.setText(null);
		return button;
	}

	public static void showError(String title, String sms, String trace) {
		showAlert(ResourceManager.getInstance().getText("alert.title.error"), title, sms, trace, AlertType.ERROR, "error-panel");
	}

	public static void showError(String title, String sms) {
		showAlert(ResourceManager.getInstance().getText("alert.title.error"), title, sms, null, AlertType.ERROR, "error-panel");
	}
	
	public static void showInfo(String title, String sms) {
		showAlert(ResourceManager.getInstance().getText("alert.title.information"), title, sms, null, AlertType.INFORMATION, "info-panel");
	}

	public static boolean showConfirm(String title, String sms) {
		Optional<ButtonType> result = showAlert(ResourceManager.getInstance().getText("alert.title.confirm"), title, sms, null, AlertType.CONFIRMATION, "info-panel");

		return result.get() == ButtonType.OK;
	}

	private static Optional<ButtonType> showAlert(String title, String header, String sms, String trace, AlertType tipus, String paneId) {

		Alert alert = new Alert(tipus);
		alert.setTitle(title);
		alert.getDialogPane().setId(paneId);
		alert.setHeaderText(header);
		alert.setContentText(sms);
		alert.setResizable(true);


		if (trace == null) {
			alert.getDialogPane().setPrefSize(400, 120);
		} else {
			alert.getDialogPane().setPrefSize(520, 200);

			TextArea textArea = new TextArea(trace);
			textArea.setEditable(false);
			textArea.setWrapText(true);

			//textArea.setMaxWidth(Double.MAX_VALUE);
			textArea.setMaxWidth(600);
			textArea.setMaxHeight(Double.MAX_VALUE);
			textArea.setMaxHeight(300);
			textArea.setMinHeight(300);
			GridPane.setVgrow(textArea, Priority.ALWAYS);
			GridPane.setHgrow(textArea, Priority.ALWAYS);

			GridPane expContent = new GridPane();
			expContent.setMaxWidth(Double.MAX_VALUE);
			expContent.setMaxWidth(600);
			expContent.add(textArea, 0, 1);

			// Set expandable Exception into the dialog pane.
			alert.getDialogPane().setExpandableContent(expContent);
			alert.getDialogPane().setExpanded(false);


			// Change Listener => property has been recalculated

			alert.getDialogPane().expandedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							alert.getDialogPane().requestLayout();
							Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
							stage.sizeToScene();
						}
					});
				}
			});

			// Lambda version

			/*alert.getDialogPane().expandedProperty().addListener((observable, oldvalue, newvalue) -> {

				Platform.runLater(() -> {
					alert.getDialogPane().requestLayout();
					Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				});
			});*/

			// Invalidation Listener => property  and has to be recalculated

			/*alert.getDialogPane().expandedProperty().addListener((observable) -> {

				Platform.runLater(() -> {
					alert.getDialogPane().requestLayout();
					Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				});
			});*/
		}

		return alert.showAndWait();
	}
	
	private File openFileChooser(String title, boolean open) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("BIN", "*.bin"),
				new FileChooser.ExtensionFilter("Tots", "*.*")
				);
		File file;
		if (open) file = fileChooser.showOpenDialog(ResourceManager.getInstance().getStage());
		else file = fileChooser.showSaveDialog(ResourceManager.getInstance().getStage());
		return file;
	}
}
