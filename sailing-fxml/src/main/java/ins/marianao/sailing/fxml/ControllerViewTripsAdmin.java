package ins.marianao.sailing.fxml;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.bytecode.internal.bytebuddy.PrivateAccessorException;

import cat.institutmarianao.sailing.ws.model.Action;
import cat.institutmarianao.sailing.ws.model.Client;
import cat.institutmarianao.sailing.ws.model.Trip;
import cat.institutmarianao.sailing.ws.model.Trip.Status;
import cat.institutmarianao.sailing.ws.model.TripType;
import cat.institutmarianao.sailing.ws.model.User;
import cat.institutmarianao.sailing.ws.model.TripType.Category;
import cat.institutmarianao.sailing.ws.model.User.Role;
import ins.marianao.sailing.fxml.exception.OnFailedEventHandler;
import ins.marianao.sailing.fxml.manager.ResourceManager;
import ins.marianao.sailing.fxml.services.ServiceQueryTrips;
import ins.marianao.sailing.fxml.services.ServiceQueryUsers;
import ins.marianao.sailing.fxml.utils.ColumnButton;
import ins.marianao.sailing.fxml.utils.Formatters;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ControllerViewTripsAdmin implements Initializable {

	@FXML
	private ComboBox<Category> cmbCategory;
	@FXML
	private ComboBox<User> cmbUser;
	@FXML
	private ComboBox<Status> cmbStatus;
	@FXML
	private DatePicker dpFrom;
	@FXML
	private DatePicker dpTo;

	@FXML
	private TableView<Trip> tripsTable;
	@FXML
	private TableColumn<Trip, Number> colIndex;
	@FXML
	private TableColumn<Trip, String> colClient;
	@FXML
	private TableColumn<Trip, String> colCategory;
	@FXML
	private TableColumn<Trip, String> colTitle;
	@FXML
	private TableColumn<Trip, Number> colMax;
	@FXML
	private TableColumn<Trip, Number> colBooked;
	@FXML
	private TableColumn<Trip, String> colStatus;
	@FXML
	private TableColumn<Trip, Date> colDate;
	@FXML
	private TableColumn<Trip, Date> colDeparture;
	@FXML
	private TableColumn<Trip, Number> colPlaces;
	@FXML
	private TableColumn<Trip, String> colComents;
	@FXML
	private TableColumn<Trip, Boolean> colConfirm;
	@FXML
	private TableColumn<Trip, Boolean> colCancel;
	@FXML
	private TableColumn<Trip, Boolean> colReschedule;
	private ResourceBundle resource;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		// Guardar el ResourceBundle en una variable de clase
		this.resource = resources;

		final ServiceQueryUsers queryUsers = new ServiceQueryUsers(new Role[] { Role.CLIENT }, null);

		queryUsers.setOnSucceeded(Event -> {
			ObservableList<User> client = FXCollections.observableArrayList(queryUsers.getValue());

			client.add(0, null);
			cmbUser.setItems(FXCollections.observableArrayList(client));

			cmbUser.valueProperty().addListener((observable, oldValue, newValue) -> {
				reloadTrips();
			});

			cmbUser.setConverter(Formatters.getUserConverter());

		});

		queryUsers.start();

		cmbUser.setCellFactory(new Callback<ListView<User>, ListCell<User>>() {
			@Override
			public ListCell<User> call(ListView<User> param) {
				return new ListCell<User>() {
					@Override
					protected void updateItem(User item, boolean empty) {
						super.updateItem(item, empty);
						if (empty || item == null) {
							setText(resource.getString("text.User.ALL")); // Mostrar "ALL" si es null
						} else {
							setText(item.getInfo()); // Mostrar el nombre completo del usuario
						}
					}
				};
			}
		});

		ObservableList<TripType.Category> categoryList = FXCollections.observableArrayList(TripType.Category.GROUP,
				TripType.Category.PRIVATE);

		TripType.Category allCategory = null; // all será null para no tneer filtros

		// Crear la lista con ALL al inicio
		ObservableList<TripType.Category> categoryListWithAll = FXCollections.observableArrayList();
		categoryListWithAll.add(allCategory);
		categoryListWithAll.addAll(categoryList);

		// Asignar la lista al ComboBox
		cmbCategory.setItems(categoryListWithAll);

		// Configurar la visualización en el ComboBox
		cmbCategory.setCellFactory(new Callback<ListView<TripType.Category>, ListCell<TripType.Category>>() {
			@Override
			public ListCell<TripType.Category> call(ListView<TripType.Category> param) {
				return new ListCell<TripType.Category>() {
					@Override
					protected void updateItem(TripType.Category item, boolean empty) {
						super.updateItem(item, empty);
						if (empty || item == null) {
							setText(resource.getString("text.Category.ALL"));
						} else {
							setText(resource.getString("text.Category." + item.name()));
						}
					}
				};
			}
		});

		// Configurar el StringConverter para manejar "ALL"
		cmbCategory.setConverter(new StringConverter<TripType.Category>() {
			@Override
			public String toString(TripType.Category category) {
				if (category == null) {
					return resource.getString("text.Category.ALL"); // Mostrar "ALL" si es null
				}
				return resource.getString("text.Category." + category.name());
			}

			@Override
			public TripType.Category fromString(String string) {
				if (string.equals(resource.getString("text.Category.ALL"))) {
					return null; // Representamos "ALL" como null
				}
				return categoryList.stream()
						.filter(cat -> resource.getString("text.Category." + cat.name()).equals(string)).findFirst()
						.orElse(null);
			}
		});

		// Agregar listener para recargar los viajes cuando cambia la selección
		cmbCategory.valueProperty().addListener((observable, oldValue, newValue) -> {
			reloadTrips();
		});

		Status allStatus = null; // "ALL" será representado como null
		ObservableList<Status> statusListWithAll = FXCollections.observableArrayList();
		statusListWithAll.add(allStatus);
		statusListWithAll.addAll(FXCollections.observableArrayList(Trip.Status.values()));

		// Asignar la lista al ComboBox
		cmbStatus.setItems(statusListWithAll);

		// Configurar el CellFactory para mostrar los items en el dropdown
		cmbStatus.setCellFactory(new Callback<ListView<Status>, ListCell<Status>>() {
			@Override
			public ListCell<Status> call(ListView<Status> param) {
				return new ListCell<Status>() {
					@Override
					protected void updateItem(Status item, boolean empty) {
						super.updateItem(item, empty);
						if (empty || item == null) {
							setText(resource.getString("text.Status.ALL")); // Mostrar "ALL" si es null
						} else {
							setText(resource.getString("text.Status." + item.name()));
						}
					}
				};
			}
		});

		// Configurar el StringConverter para manejar "ALL"
		cmbStatus.setConverter(new StringConverter<Status>() {
			@Override
			public String toString(Status status) {
				if (status == null) {
					return resource.getString("text.Status.ALL"); // Mostrar "ALL" si es null
				}
				return resource.getString("text.Status." + status.name());
			}

			@Override
			public Status fromString(String string) {
				if (string.equals(resource.getString("text.Status.ALL"))) {
					return null; // Representamos "ALL" como null
				}
				return Arrays.stream(Trip.Status.values())
						.filter(st -> resource.getString("text.Status." + st.name()).equals(string)).findFirst()
						.orElse(null);
			}
		});

		cmbStatus.valueProperty().addListener((observable, oldValue, newValue) -> {
			reloadTrips();
		});

		reloadTrips();

		// Escuchar cambios en la fecha seleccionada en dpFrom
		dpFrom.valueProperty().addListener((observable, oldValue, newValue) -> reloadTrips());

		// Escuchar cambios en la fecha seleccionada en dpFrom
		dpTo.valueProperty().addListener((observable, oldValue, newValue) -> reloadTrips());

		// Columna Index
		this.tripsTable.setEditable(true);
		this.tripsTable.getSelectionModel().setCellSelectionEnabled(true);

		this.colIndex.setMinWidth(30);
		this.colIndex.setMaxWidth(60);
		this.colIndex.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Trip, Number>, ObservableValue<Number>>() {
					@Override
					public ObservableValue<Number> call(TableColumn.CellDataFeatures<Trip, Number> trip) {
						return new SimpleLongProperty(tripsTable.getItems().indexOf(trip.getValue()) + 1);
					}
				});

//		Columna Client
		this.colClient.setMinWidth(170);
		this.colClient.setMaxWidth(Double.MAX_VALUE);
		this.colClient.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Trip, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<Trip, String> trip) {
						return new SimpleStringProperty(trip.getValue().getClient().getFullName());
					}
				});

//		Columna Category
		this.colCategory.setMinWidth(50);
		this.colCategory.setMaxWidth(Double.MAX_VALUE);
		this.colCategory.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Trip, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<Trip, String> trip) {
						return new SimpleStringProperty(
								trip.getValue().getDeparture().getTripType().getCategory().name());
					}
				});

//		Columna Titulo
		this.colTitle.setMinWidth(50);
		this.colTitle.setMaxWidth(Double.MAX_VALUE);
		this.colTitle.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Trip, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<Trip, String> trip) {
						return new SimpleStringProperty(trip.getValue().getDeparture().getTripType().getTitle());
					}
				});

//		Columna Max
		this.colMax.setMinWidth(30);
		this.colMax.setMaxWidth(Double.MAX_VALUE);
		this.colMax.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Trip, Number>, ObservableValue<Number>>() {
					@Override
					public ObservableValue<Number> call(TableColumn.CellDataFeatures<Trip, Number> trip) {
						return new SimpleIntegerProperty(trip.getValue().getDeparture().getTripType().getMaxPlaces());
					}
				});

//		Columna Booked
		this.colBooked.setMinWidth(50);
		this.colBooked.setMaxWidth(Double.MAX_VALUE);
		this.colBooked.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Trip, Number>, ObservableValue<Number>>() {
					@Override
					public ObservableValue<Number> call(TableColumn.CellDataFeatures<Trip, Number> trip) {
						return new SimpleIntegerProperty(trip.getValue().getDeparture().getBookedPlaces());
					}
				});

//		Columna Status
		this.colStatus.setMinWidth(50);
		this.colStatus.setMaxWidth(Double.MAX_VALUE);
		this.colStatus.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Trip, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<Trip, String> trip) {
						return new SimpleStringProperty(trip.getValue().getStatus().toString());
					}
				});

//		Columna Date
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		this.colDate.setMinWidth(50);
		this.colDate.setMaxWidth(Double.MAX_VALUE);
		this.colDate
				.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, Date>, ObservableValue<Date>>() {
					@Override
					public ObservableValue<Date> call(TableColumn.CellDataFeatures<Trip, Date> trip) {
						return new SimpleObjectProperty(sdf.format(trip.getValue().getDeparture().getDate()));
					}
				});

//		Columna Departure
		SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
		this.colDeparture.setMinWidth(50);
		this.colDeparture.setMaxWidth(Double.MAX_VALUE);
		this.colDeparture
				.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, Date>, ObservableValue<Date>>() {
					@Override
					public ObservableValue<Date> call(TableColumn.CellDataFeatures<Trip, Date> trip) {
						return new SimpleObjectProperty(sdf2.format(trip.getValue().getDeparture().getDeparture()));
					}
				});

//		Columna Plcaes
		this.colPlaces.setMinWidth(20);
		this.colPlaces.setMaxWidth(Double.MAX_VALUE);
		this.colPlaces.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Trip, Number>, ObservableValue<Number>>() {
					@Override
					public ObservableValue<Number> call(TableColumn.CellDataFeatures<Trip, Number> trip) {
						return new SimpleIntegerProperty(trip.getValue().getPlaces());
					}
				});

//		Columna Coments
		this.colComents.setMinWidth(100);
		this.colComents.setMaxWidth(Double.MAX_VALUE);
		this.colComents.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Trip, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<Trip, String> trip) {
						List<Action> tripComent = trip.getValue().getTracking();
						for (Action tripComent1 : tripComent) {
							if (tripComent1.getInfo()!=null && !tripComent1.getInfo().trim().isEmpty()) {
								return new SimpleStringProperty(tripComent1.getInfo());
							}
						}
						return null;
					}
				});

//		Columna Cancel
		this.colCancel.setMinWidth(50);
		this.colCancel.setMaxWidth(70);
		this.colCancel.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Trip, Boolean>, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Trip, Boolean> cell) {
						return new SimpleBooleanProperty(false);
					}
				});

		this.colCancel.setCellFactory(column -> new TableCell<Trip, Boolean>() {
			private final Button button = new Button();

			{
				// cargar imagen y ajustar tamaño
				ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("resources/cancel.png")));
				imageView.setFitWidth(16);
				imageView.setFitHeight(16);

				// juntamos el botón con la imagen
				button.setGraphic(imageView);
				button.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
				button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

				button.setOnAction(event -> {
					Trip trip = getTableView().getItems().get(getIndex());
					try {
						ResourceManager.getInstance().getMenuController().openTripCancel(trip);
					} catch (Exception e) {
						ControllerMenu.showError(resource.getString("error.viewUsers.delete"), e.getMessage(),
								ExceptionUtils.getStackTrace(e));
					}
				});
			}

			@Override
			protected void updateItem(Boolean item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || getTableRow() == null || getTableRow().getItem() == null) {
					setGraphic(null);
				} else {
					Trip trip = getTableRow().getItem();
					if (trip.getStatus() == Status.DONE || trip.getStatus() == Status.CANCELLED) {
						setGraphic(null); // no mostrar el botón si el estado es DONE o CANCELLED
					} else {
						setGraphic(button); // mostrar el botón
					}
				}
			}
		});

		// Columna Reschedule
		this.colReschedule.setMinWidth(50);
		this.colReschedule.setMaxWidth(70);
		this.colReschedule.setCellValueFactory(cell ->
		    new SimpleBooleanProperty(false)
		);
		this.colReschedule.setCellFactory(column -> new TableCell<Trip, Boolean>() {
		    private final Button button = new Button();
		    {
		        // Cargar imagen y ajustar tamaño para reschedule
		        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("resources/reschedule.png")));
		        imageView.setFitWidth(16);
		        imageView.setFitHeight(16);
		        button.setGraphic(imageView);
		        button.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
		        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		        // Lógica para replanificar el viaje
		        button.setOnAction(event -> {
		            Trip trip = getTableView().getItems().get(getIndex());
		            ResourceManager.getInstance().getMenuController().openTripReschedule(trip);
		        });
		    }

		    @Override
		    protected void updateItem(Boolean item, boolean empty) {
		        super.updateItem(item, empty);
		        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
		            setGraphic(null);
		        } else {
		            Trip trip = getTableRow().getItem();
		            if (trip.getStatus() == Status.DONE || trip.getStatus() == Status.CANCELLED) {
		                setGraphic(null); // No mostrar si el viaje ya está DONE o CANCELLED
		            } else {
		                setGraphic(button);
		            }
		        }
		    }
		});

		// Columna Confirm
		this.colConfirm.setMinWidth(50);
		this.colConfirm.setMaxWidth(70);
		this.colConfirm.setCellValueFactory(cell ->
		    new SimpleBooleanProperty(false)
		);
		this.colConfirm.setCellFactory(column -> new TableCell<Trip, Boolean>() {
		    private final Button button = new Button();
		    {
		        // Cargar imagen y ajustar tamaño para confirmar
		        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("resources/done.png")));
		        imageView.setFitWidth(16);
		        imageView.setFitHeight(16);
		        button.setGraphic(imageView);
		        button.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
		        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		        // Lógica para confirmar el viaje
		        button.setOnAction(event -> {
		            Trip trip = getTableView().getItems().get(getIndex());
		            ResourceManager.getInstance().getMenuController().openTripConfirm(trip);
		        });
		    }

		    @Override
		    protected void updateItem(Boolean item, boolean empty) {
		        super.updateItem(item, empty);
		        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
		            setGraphic(null);
		        } else {
		            Trip trip = getTableRow().getItem();
		            if (trip.getStatus() == Status.DONE || trip.getStatus() == Status.CANCELLED) {
		                setGraphic(null); // No mostrar si el viaje ya está DONE o CANCELLED
		            } else {
		                setGraphic(button);
		            }
		        }
		    }
		});


	}

	public void reloadTrips() {
		// Obtener los valores seleccionados de los ComboBox y DatePicker
		Category category = cmbCategory.getValue();
		Status status = cmbStatus.getValue();
		LocalDate fromDate = dpFrom.getValue();
		LocalDate toDate = dpTo.getValue();
		User selectedUser = cmbUser.getValue();
		String clientName = (selectedUser != null) ? selectedUser.getUsername() : null;

		System.out.println("dpFrom value: " + fromDate);
		System.out.println("dpTo value: " + toDate);

		// Convertir LocalDate a java.util.Date correctamente
		Date fromDateConverted = (fromDate != null)
				? Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
				: null;

		Date toDateConverted = (toDate != null)
				? Date.from(toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())
				: null;

		System.out.println("Converted fromDate: " + fromDateConverted);
		System.out.println("Converted toDate: " + toDateConverted);

		Status[] statusArray = (status != null) ? new Status[] { status } : new Status[0];
		Category[] categoryArray = (category != null) ? new Category[] { category } : new Category[0];

		// Crear el servicio
		final ServiceQueryTrips queryTrips = new ServiceQueryTrips(statusArray, categoryArray, clientName,
				fromDateConverted, toDateConverted);

		queryTrips.setOnSucceeded(event -> {
			tripsTable.setEditable(true);
			tripsTable.getItems().clear();

			// Obtener la lista de viajes
			List<Trip> resultTrips = queryTrips.getValue();
			if (resultTrips != null) {
				tripsTable.setItems(FXCollections.observableArrayList(resultTrips));
			}
		});

		queryTrips.setOnFailed(
				new OnFailedEventHandler(ResourceManager.getInstance().getText("error.viewTrips.web.service")));

		// Iniciar la consulta
		queryTrips.start();
	}
}