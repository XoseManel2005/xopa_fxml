package ins.marianao.sailing.fxml;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.bytecode.internal.bytebuddy.PrivateAccessorException;

import cat.institutmarianao.sailing.ws.model.Action;
import cat.institutmarianao.sailing.ws.model.Client;
import cat.institutmarianao.sailing.ws.model.Trip;
import cat.institutmarianao.sailing.ws.model.Trip.Status;
import cat.institutmarianao.sailing.ws.model.TripType;
import cat.institutmarianao.sailing.ws.model.User;
import cat.institutmarianao.sailing.ws.model.TripType.Category;
import ins.marianao.sailing.fxml.exception.OnFailedEventHandler;
import ins.marianao.sailing.fxml.manager.ResourceManager;
import ins.marianao.sailing.fxml.services.ServiceQueryTrips;
import ins.marianao.sailing.fxml.utils.ColumnButton;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ControllerViewTripsAdmin implements Initializable {

    @FXML private ComboBox<Category> cmbCategory;
    @FXML private TextField tfClient;
    @FXML private ComboBox<Status> cmbStatus;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;

    @FXML private TableView<Trip> tripsTable;
    @FXML private TableColumn<Trip, Number> colIndex;
    @FXML private TableColumn<Trip, String> colClient;
    @FXML private TableColumn<Trip, String> colCategory;
    @FXML private TableColumn<Trip, String> colTitle;
    @FXML private TableColumn<Trip, Number> colMax;
    @FXML private TableColumn<Trip, Number> colBooked;
    @FXML private TableColumn<Trip, String> colStatus;
    @FXML private TableColumn<Trip, Date> colDate;
    @FXML private TableColumn<Trip, Date> colDeparture;
    @FXML private TableColumn<Trip, Number> colPlaces;
    @FXML private TableColumn<Trip, String> colComents;
    @FXML private TableColumn<Trip, Boolean> colConfirm;
    @FXML private TableColumn<Trip, Boolean> colCancel;
    @FXML private TableColumn<Trip, Boolean> colReschedule;
	private ResourceBundle resource;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	// TODO Auto-generated method stub
    	// Guardar el ResourceBundle en una variable de clase
        this.resource = resources;
        

        tfClient.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				reloadTrips();
			}
		});
        
     // Configuración del cmbStatus
        ObservableList<Category> categoryList = FXCollections.observableArrayList(TripType.Category.values());
        
        // Configurar el CellFactory para mostrar los items en el dropdown
        cmbCategory.setCellFactory((Callback<ListView<Category>, ListCell<Category>>) new Callback<ListView<Category>, ListCell<Category>>() {
            @Override
            public ListCell<Category> call(ListView<Category> param) {
                return new ListCell<Category>() {
                	@Override
                	protected void updateItem(Category item, boolean empty) {
                	    super.updateItem(item, empty);
                	    if (empty) {
                	        setText(resource.getString("text.Category.ALL"));
                	    } else {
                	        setText(resource.getString("text.Category." + item.name()));
                	    }
                	}
                };
            }
        });
        
        // Configurar el StringConverter para mostrar el item seleccionado
        cmbCategory.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
            	if (category==null) {
					return resource.getString("text.Category.ALL");
				}
                return category == null ? null : 
                    resource.getString("text.Category." + category.name());
            }

            @Override
            public Category fromString(String string) {
                return null;
            }
        });

        
        // Establecer los items del ComboBox
        cmbCategory.setItems(categoryList);
        
        // Agregar listener para recargar los viajes cuando cambia el estado
        cmbCategory.valueProperty().addListener((observable, oldValue, newValue) -> {
            reloadTrips();
        });
        
        
    	// Configuración del cmbStatus
        ObservableList<Status> statusList = FXCollections.observableArrayList(Trip.Status.values());
        
        // Configurar el CellFactory para mostrar los items en el dropdown
        cmbStatus.setCellFactory((Callback<ListView<Status>, ListCell<Status>>) new Callback<ListView<Status>, ListCell<Status>>() {
            @Override
            public ListCell<Status> call(ListView<Status> param) {
                return new ListCell<Status>() {
                	@Override
                	protected void updateItem(Status item, boolean empty) {
                	    super.updateItem(item, empty);
                	    if (empty) {
                	        setText(resource.getString("text.Status.ALL"));
                	    } else {
                	        setText(resource.getString("text.Status." + item.name()));
                	    }
                	}
                };
            }
        });
        
        // Configurar el StringConverter para mostrar el item seleccionado
        cmbStatus.setConverter(new StringConverter<Status>() {
            @Override
            public String toString(Status status) {
            	if (status==null) {
					return resource.getString("text.Status.ALL");
				}
                return status == null ? null : 
                    resource.getString("text.Status." + status.name());
            }

            @Override
            public Status fromString(String string) {
                return null;
            }
        });

        
        // Establecer los items del ComboBox
        cmbStatus.setItems(statusList);
        
        // Agregar listener para recargar los viajes cuando cambia el estado
        cmbStatus.valueProperty().addListener((observable, oldValue, newValue) -> {
            reloadTrips();
        });
    			
        reloadTrips();

        // Escuchar cambios en la fecha seleccionada en dpFrom
        dpFrom.valueProperty().addListener(new ChangeListener<java.time.LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends java.time.LocalDate> observable, java.time.LocalDate oldValue, java.time.LocalDate newValue) {
                reloadTrips();
            }
        });
        
     // Escuchar cambios en la fecha seleccionada en dpFrom
        dpTo.valueProperty().addListener(new ChangeListener<java.time.LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends java.time.LocalDate> observable, java.time.LocalDate oldValue, java.time.LocalDate newValue) {
                reloadTrips();
            }
        });
        
        //Columna Index
        this.tripsTable.setEditable(true);
		this.tripsTable.getSelectionModel().setCellSelectionEnabled(true);

		this.colIndex.setMinWidth(30);
		this.colIndex.setMaxWidth(60);
		this.colIndex.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, Number>, ObservableValue<Number>>() {
			@Override
			public ObservableValue<Number> call(TableColumn.CellDataFeatures<Trip, Number> trip) {
				return new SimpleLongProperty( tripsTable.getItems().indexOf(trip.getValue()) + 1 );
			}
		});
		
//		Columna Client
		this.colClient.setMinWidth(170);
		this.colClient.setMaxWidth(Double.MAX_VALUE);
		this.colClient.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<Trip, String> trip) {
				return new SimpleStringProperty( trip.getValue().getClient().getFullName());
			}
		});
		
//		Columna Category
		this.colCategory.setMinWidth(50);
		this.colCategory.setMaxWidth(Double.MAX_VALUE);
		this.colCategory.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<Trip, String> trip) {
				return new SimpleStringProperty( trip.getValue().getDeparture().getTripType().getCategory().name());
			}
		});
		
//		Columna Titulo
		this.colTitle.setMinWidth(50);
		this.colTitle.setMaxWidth(Double.MAX_VALUE);
		this.colTitle.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<Trip, String> trip) {
				return new SimpleStringProperty( trip.getValue().getDeparture().getTripType().getTitle());
			}
		});
		
//		Columna Max
		this.colMax.setMinWidth(30);
		this.colMax.setMaxWidth(Double.MAX_VALUE);
		this.colMax.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, Number>, ObservableValue<Number>>() {
			@Override
			public ObservableValue<Number> call(TableColumn.CellDataFeatures<Trip, Number> trip) {
				return new SimpleIntegerProperty( trip.getValue().getDeparture().getTripType().getMaxPlaces());
			}
		});
		
//		Columna Booked
		this.colBooked.setMinWidth(50);
		this.colBooked.setMaxWidth(Double.MAX_VALUE);
		this.colBooked.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, Number>, ObservableValue<Number>>() {
			@Override
			public ObservableValue<Number> call(TableColumn.CellDataFeatures<Trip, Number> trip) {
				return new SimpleIntegerProperty( trip.getValue().getDeparture().getBookedPlaces());
			}
		});
		
//		Columna Status
		this.colStatus.setMinWidth(50);
		this.colStatus.setMaxWidth(Double.MAX_VALUE);
		this.colStatus.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<Trip, String> trip) {
				return new SimpleStringProperty( trip.getValue().getStatus().toString());
			}
		});
		
//		Columna Date
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		this.colDate.setMinWidth(50);
		this.colDate.setMaxWidth(Double.MAX_VALUE);
		this.colDate.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, Date>, ObservableValue<Date>>() {
			@Override
			public ObservableValue<Date> call(TableColumn.CellDataFeatures<Trip, Date> trip) {
				return new SimpleObjectProperty(sdf.format(trip.getValue().getDeparture().getDate()));
			}
		});
		
//		Columna Departure
		SimpleDateFormat sdf2 = new SimpleDateFormat("mm:HH");
		this.colDeparture.setMinWidth(50);
		this.colDeparture.setMaxWidth(Double.MAX_VALUE);
		this.colDeparture.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, Date>, ObservableValue<Date>>() {
			@Override
			public ObservableValue<Date> call(TableColumn.CellDataFeatures<Trip, Date> trip) {
				return new SimpleObjectProperty(sdf2.format(trip.getValue().getDeparture().getDeparture()));
			}
		});
		
//		Columna Plcaes
		this.colPlaces.setMinWidth(20);
		this.colPlaces.setMaxWidth(Double.MAX_VALUE);
		this.colPlaces.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, Number>, ObservableValue<Number>>() {
			@Override
			public ObservableValue<Number> call(TableColumn.CellDataFeatures<Trip, Number> trip) {
				return new SimpleIntegerProperty( trip.getValue().getPlaces());
			}
		});
		
//		Columna Coments
		this.colComents.setMinWidth(100);
		this.colComents.setMaxWidth(Double.MAX_VALUE);
		this.colComents.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<Trip, String> trip) {
				List<Action> tripComent = trip.getValue().getTracking();
				//return new SimpleStringProperty(trip.getValue().getTracking());
				return null;
			}
		});
		

		
//		Columna Cancel
		this.colCancel.setMinWidth(50);
		this.colCancel.setMaxWidth(70);
		// define a simple boolean cell value for the action column so that the column will only be shown for non-empty rows.
		this.colCancel.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Trip, Boolean> cell) {
				return new SimpleBooleanProperty(false);
			}
		});
	
		this.colCancel.setCellFactory(new ColumnButton<Trip, Boolean>(ResourceManager.getInstance().getText("fxml.text.viewTrips.cancel.title"),
											new Image(getClass().getResourceAsStream("resources/cancel.png")) ) {
			@Override
			public void buttonAction(Trip trip) {
				try {
					boolean result = ControllerMenu.showConfirm(ResourceManager.getInstance().getText("fxml.text.viewTrips.cancel.title"), 
																	ResourceManager.getInstance().getText("fxml.text.viewTrips.cancel.text"));
					if (result) {
						//deleteUsuari(usuari);
					}
				} catch (Exception e) {
					ControllerMenu.showError(resource.getString("error.viewUsers.delete"), e.getMessage(), ExceptionUtils.getStackTrace(e));
				}
			}
		});
		
//		Columna Reschedule
		this.colReschedule.setMinWidth(50);
		this.colReschedule.setMaxWidth(70);
		// define a simple boolean cell value for the action column so that the column will only be shown for non-empty rows.
		this.colReschedule.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Trip, Boolean> cell) {
				return new SimpleBooleanProperty(false);
			}
		});
	
		this.colReschedule.setCellFactory(new ColumnButton<Trip, Boolean>(ResourceManager.getInstance().getText("fxml.text.viewTrips.cancel.title"),
											new Image(getClass().getResourceAsStream("resources/reschedule.png")) ) {
			@Override
			public void buttonAction(Trip trip) {
				ResourceManager.getInstance().getMenuController().openTripReschedule(trip);
			}
		});
		
//		Columna Confirm
		this.colConfirm.setMinWidth(50);
		this.colConfirm.setMaxWidth(70);
		// define a simple boolean cell value for the action column so that the column will only be shown for non-empty rows.
		this.colConfirm.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Trip, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Trip, Boolean> cell) {
				return new SimpleBooleanProperty(false);
			}
		});
	
		this.colConfirm.setCellFactory(new ColumnButton<Trip, Boolean>(ResourceManager.getInstance().getText("fxml.text.viewTrips.reschedule.title"),
											new Image(getClass().getResourceAsStream("resources/done.png")) ) {
			@Override
			public void buttonAction(Trip trip) {
				//ResourceManager.getInstance().getMenuController().openUserForm(trip);
			}
		});
		
		
    }

    private void reloadTrips() {
        // Obtener los valores seleccionados de los ComboBox y DatePicker
        Category category = cmbCategory.getValue();
        Status status = cmbStatus.getValue();
        LocalDate fromDate = dpFrom.getValue();
        LocalDate toDate = dpTo.getValue();
		String clientName = tfClient.getText();
        // Convertir LocalDate a java.util.Date
        Date FromDate = fromDate != null ? Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
        Date ToDate = toDate != null ? Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;

        // Convertir el estado y la categoría a arrays si es necesario
        Status[] statusArray = status != null ? new Status[]{status} : null;
        Category[] categoryArray = category != null ? new Category[]{category} : null;


        // Crear el servicio de consulta de viajes con los parámetros de búsqueda
        final ServiceQueryTrips queryTrips = new ServiceQueryTrips(statusArray, categoryArray, clientName, FromDate, ToDate);

        // Configurar el manejador de éxito de la consulta
        queryTrips.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                tripsTable.setEditable(true);
                tripsTable.getItems().clear();

                // Obtener la lista de viajes y establecerla en la tabla
                ObservableList<Trip> trips = FXCollections.observableArrayList(queryTrips.getValue());
                tripsTable.setItems(trips);
            }
        });

        // Configurar el manejador de fallo de la consulta
        queryTrips.setOnFailed(new OnFailedEventHandler(ResourceManager.getInstance().getText("error.viewTrips.web.service")));

        // Iniciar la consulta
        queryTrips.start();
    }
}