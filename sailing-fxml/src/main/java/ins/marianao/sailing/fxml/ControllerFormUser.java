package ins.marianao.sailing.fxml;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cat.institutmarianao.sailing.ws.model.Client;
import cat.institutmarianao.sailing.ws.model.User;
import cat.institutmarianao.sailing.ws.model.User.Role;
import ins.marianao.sailing.fxml.manager.ResourceManager;
import ins.marianao.sailing.fxml.utils.Formatters;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.util.Pair;

public class ControllerFormUser {
    
    @FXML 
    private ComboBox<Pair<String,String>> cmbRole;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField pfConfirm;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtPhone;

    @FXML
    private BorderPane viewSigninForm;
    
    boolean isNewUser;
    
    public void prepareForNewUser() {
        isNewUser = true;
        clearFields();
    }

    public void loadUserProfile(User user) {
        isNewUser = false;

        if (user != null) {
            txtUsername.setText(user.getUsername());
            pfPassword.setText(user.getPassword()); //no se muestra la contraseña
            pfConfirm.setText("");
            txtName.setText(user instanceof Client ? ((Client) user).getFullName() : "");
            txtPhone.setText(user instanceof Client ? String.valueOf(((Client) user).getPhone()) : "");

            cmbRole.setValue(new Pair<>(user.getRole().name(), ResourceManager.getInstance().getText("text.User." + user.getRole().name())));
            cmbRole.setDisable(true);
        }
    }
    
    @FXML
    public void SignClick(ActionEvent event) {
        Pair<String, String> selectedRole = this.cmbRole.getValue();
        User.Role role = null;
        
        if (selectedRole != null) {
            try {
                role = User.Role.valueOf(selectedRole.getKey());
            } catch (IllegalArgumentException e) {
                ControllerMenu.showError("Error", "Rol inválido.");
                return;
            }
        }
        
        System.out.println("Rol seleccionado: " + role);

        Long phoneNumber = null;
        String fullName = null;

        if (role == User.Role.CLIENT) {
            String phoneText = this.txtPhone.getText().trim();
            if (phoneText.isEmpty()) {
                ControllerMenu.showError("Error", "El número de teléfono no puede estar vacío.");
                return;
            }

            try {
                phoneNumber = Long.parseLong(phoneText);
            } catch (NumberFormatException e) {
                ControllerMenu.showError("Error", "Número de teléfono no válido.");
                return;
            }

            fullName = this.txtName.getText().trim();
            if (fullName.isEmpty()) {
                ControllerMenu.showError("Error", "El nombre completo no puede estar vacío.");
                return;
            }
        }
        
        boolean isUpdating = !isNewUser;

        ResourceManager.getInstance().getMenuController().signin(
            this.txtUsername.getText(),
            this.pfPassword.getText(),
            this.pfConfirm.getText(),
            fullName,
            phoneNumber != null ? phoneNumber.intValue() : null, 
            role,
            isUpdating
        );
    }

    @FXML
    public void initialize () {
    	//reglas para el txtPhone
        txtPhone.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtPhone.setText(oldValue);
            } else if (newValue.length() > 9) {
				txtPhone.setText(oldValue);
			}
        });
    
        //vemos si es admin
        boolean isAdmin = ResourceManager.getInstance().isAdmin();

        //cargamos el comboBox con 
        List<Pair<String, String>> roles = Stream.of(User.Role.values())
            .filter(role -> isAdmin || role == Role.CLIENT)
            .map(role -> new Pair<>(role.name(), ResourceManager.getInstance().getText("text.User." + role.name())))
            .collect(Collectors.toList());
        ObservableList<Pair<String, String>> listRoles = FXCollections.observableArrayList(roles);

        //agregamos las opciones al combobox y asignamos comportamientos según el rol
        this.cmbRole.setItems(listRoles);
        this.cmbRole.setConverter(Formatters.getStringPairConverter("User"));

        if (!listRoles.isEmpty()) {
            this.cmbRole.setValue(listRoles.get(0));
            txtName.setDisable(isAdmin);
            txtPhone.setDisable(isAdmin);
        }
        
        this.cmbRole.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                User.Role selectedRoleValue = User.Role.valueOf(newVal.getKey()); //convertir el nuevo valor en objeto de tipo role
                boolean isClient = selectedRoleValue == User.Role.CLIENT;
                txtName.setDisable(!isClient);
                txtPhone.setDisable(!isClient);
            }
        });
    }
    
    private void clearFields() {
        txtUsername.clear();
        pfPassword.clear();
        pfConfirm.clear();
        txtName.clear();
        txtPhone.clear();
    }
}
