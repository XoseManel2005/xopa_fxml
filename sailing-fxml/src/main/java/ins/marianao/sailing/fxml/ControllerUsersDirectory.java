package ins.marianao.sailing.fxml;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.exception.ExceptionUtils;

import cat.institutmarianao.sailing.ws.model.User;
import cat.institutmarianao.sailing.ws.model.Client;
import cat.institutmarianao.sailing.ws.model.User.Role;
import ins.marianao.sailing.fxml.exception.OnFailedEventHandler;
import ins.marianao.sailing.fxml.manager.ResourceManager;
import ins.marianao.sailing.fxml.services.ServiceDeleteUser;
import ins.marianao.sailing.fxml.services.ServiceQueryUsers;
import ins.marianao.sailing.fxml.utils.ColumnButton;
import ins.marianao.sailing.fxml.utils.Formatters;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javafx.util.Pair;


public class ControllerUsersDirectory extends AbstractControllerPDF {
	@FXML private BorderPane viewUsersDirectory;

	@FXML private ComboBox<Pair<String,String>> cmbRole;
	@FXML private TextField txtFullnameSearch;

	@FXML private TableView<User> usersTable;
	@FXML private TableColumn<User, Number> colIndex;
	@FXML private TableColumn<User, String> colRole;
	@FXML private TableColumn<User, String> colUsername;
	@FXML private TableColumn<User, String> colFullname;
	@FXML private TableColumn<User, String> colPhone;
	@FXML private TableColumn<User, Boolean> colUpdate;
	@FXML private TableColumn<User, Boolean> colDelete;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle resource) {

		super.initialize(url, resource);

		this.txtFullnameSearch.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				
				reloadUsers();
			}
		});

		
		List<Pair<String,String>> roles = Stream.of(User.Role.values()).map(new Function<Role,Pair<String,String>>() {
			@Override
			public Pair<String,String> apply(Role t) {
				String key = t.name();
				return new Pair<String, String>(key, resource.getString("text.User."+key));
			}
			
		}).collect(Collectors.toList());
		
		
		ObservableList<Pair<String,String>> listRoles = FXCollections.observableArrayList(roles);
		listRoles.add(0, null);
		//listTipus.addAll(Stream.of(User.Role.values()).map(Enum::toString).collect(Collectors.toList()));
		
		this.cmbRole.setItems(listRoles);
		this.cmbRole.setConverter(Formatters.getStringPairConverter("User"));
		
		this.cmbRole.valueProperty().addListener(new ChangeListener<Pair<String,String>>() {
			@Override
			public void changed(ObservableValue<? extends Pair<String,String>> observable, Pair<String,String> oldValue, Pair<String,String> newValue) {
				reloadUsers();
			}
		});

		this.reloadUsers();

		this.usersTable.setEditable(true);
		this.usersTable.getSelectionModel().setCellSelectionEnabled(true);

		this.colIndex.setMinWidth(40);
		this.colIndex.setMaxWidth(60);
		this.colIndex.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<User, Number>, ObservableValue<Number>>() {
			@Override
			public ObservableValue<Number> call(TableColumn.CellDataFeatures<User, Number> usuari) {
				return new SimpleLongProperty( usersTable.getItems().indexOf(usuari.getValue()) + 1 );
			}
		});

		this.colRole.setMinWidth(100);
		this.colRole.setMaxWidth(140);
		//this.colRol.setCellValueFactory(new PropertyValueFactory<User,String>("role"));
		//this.colRol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole().toString()));
		this.colRole.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> cellData) {
				String key = cellData.getValue().getRole().toString();
				return new SimpleStringProperty(resource.getString("text.User."+key));
			}
		});
		
		
		this.colRole.setCellFactory(TextFieldTableCell.forTableColumn());
		this.colRole.setEditable(false);
		
		this.colUsername.setMinWidth(120);
		this.colUsername.setMaxWidth(160);
		this.colUsername.setCellValueFactory(new PropertyValueFactory<User,String>("username"));
		this.colUsername.setCellFactory(TextFieldTableCell.forTableColumn());
		this.colUsername.setEditable(false);

		this.colFullname.setMinWidth(200);
		this.colFullname.setMaxWidth(Double.MAX_VALUE);
		this.colFullname.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> user) {
				return new SimpleStringProperty( user.getValue().isAdmin()?"":((Client) user.getValue()).getFullName() );
			}
		});

		this.colPhone.setMinWidth(140);
		this.colPhone.setMaxWidth(180);
		this.colPhone.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> user) {
				return new SimpleStringProperty( user.getValue().isAdmin()?"":Formatters.getPhoneFormatter().toString(((Client) user.getValue()).getPhone()));
			}
		});

		this.colUpdate.setMinWidth(50);
		this.colUpdate.setMaxWidth(70);
		// define a simple boolean cell value for the action column so that the column will only be shown for non-empty rows.
		this.colUpdate.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<User, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<User, Boolean> cell) {
				return new SimpleBooleanProperty(false);
			}
		});
	
		this.colUpdate.setCellFactory(new ColumnButton<User, Boolean>(ResourceManager.getInstance().getText("fxml.text.viewUsers.col.update"),
											new Image(getClass().getResourceAsStream("resources/view-details.png")) ) {
			@Override
			public void buttonAction(User user) {
				ResourceManager.getInstance().getMenuController().openUserForm(user, false);
			}
		});
		
		this.colDelete.setMinWidth(50);
		this.colDelete.setMaxWidth(70);
		// define a simple boolean cell value for the action column so that the column will only be shown for non-empty rows.
		this.colDelete.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<User, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<User, Boolean> cell) {
				return new SimpleBooleanProperty(false);
			}
		});
	
		this.colDelete.setCellFactory(new ColumnButton<User, Boolean>(ResourceManager.getInstance().getText("fxml.text.viewUsers.col.delete"),
											new Image(getClass().getResourceAsStream("resources/recycle-bin.png")) ) {
			@Override
			public void buttonAction(User usuari) {
				try {
					boolean result = ControllerMenu.showConfirm(ResourceManager.getInstance().getText("fxml.text.viewUsers.delete.title"), 
																	ResourceManager.getInstance().getText("fxml.text.viewUsers.delete.text"));
					if (result) {
						deleteUsuari(usuari);
					}
				} catch (Exception e) {
					ControllerMenu.showError(resource.getString("error.viewUsers.delete"), e.getMessage(), ExceptionUtils.getStackTrace(e));
				}
			}
		});
		
		ResourceManager.getInstance().getStage().heightProperty().addListener(event -> {
			viewUsersDirectory.setPrefHeight(ResourceManager.getInstance().getStage().getHeight());
		});
	}

	private void deleteUsuari(User user) throws Exception {
		final ServiceDeleteUser deleteUser = new ServiceDeleteUser(user);
		
		deleteUser.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
            	reloadUsers();
            }
        });
		
		deleteUser.setOnFailed(new OnFailedEventHandler(ResourceManager.getInstance().getText("error.viewUsers.delete.web.service")));
		
		deleteUser.start();
	}
	
	private void reloadUsers() {
		Role[] roles = null;
		Pair<String,String> role = this.cmbRole.getValue();
		String search = this.txtFullnameSearch.getText();

		this.usersTable.setEditable(false);
		
		if (role != null) roles = new Role[] { Role.valueOf(role.getKey()) };
		
		final ServiceQueryUsers queryUsers = new ServiceQueryUsers(roles, search);
		
		queryUsers.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
            	usersTable.setEditable(true);
            	
            	usersTable.getItems().clear();
            	
                ObservableList<User> users = FXCollections.observableArrayList(queryUsers.getValue());

        		usersTable.setItems( users );
            }
        });
		
		queryUsers.setOnFailed(new OnFailedEventHandler(ResourceManager.getInstance().getText("error.viewUsers.web.service")));
		
		queryUsers.start();
	}
	
	@Override
	protected String htmlContentToPDF() {
		List<User> users = this.usersTable.getItems();
		Pair<String,String> role = this.cmbRole.getValue();
		String search = this.txtFullnameSearch.getText();
		
		List<Pair<String,String>> filters = new LinkedList<>();
		if (role != null) filters.add(new Pair<>(ResourceManager.getInstance().getText("fxml.text.viewUsers.role"), role.getValue()));
		if (search != null && !search.isBlank()) filters.add(new Pair<>(ResourceManager.getInstance().getText("fxml.text.viewUsers.search.prompt"), "\""+search+"\""));
		
		return ResourceManager.getInstance().usersDirectoryHtml(users, filters);
	}
	
	@Override
	protected String documentTitle() {
		return ResourceManager.getInstance().getText("fxml.text.viewUsers.report.title");
	}
	
	@Override
	protected String documentFileName() {
		return ResourceManager.FILE_REPORT_USERS;
	}

}



