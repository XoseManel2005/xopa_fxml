package ins.marianao.sailing.fxml.services;

import cat.institutmarianao.sailing.ws.model.User;
import ins.marianao.sailing.fxml.manager.ResourceManager;

public class ServiceSaveUser extends ServiceSaveBase<User> {

    private static final String PATH_SAVE_USER = "save";
	private static final String PATH_UPDATE_USER = "update";

    public ServiceSaveUser(User user, boolean isUpdating) throws Exception {
    	
        super(user, User.class, new String[]{ServiceQueryUsers.PATH_REST_USERS, isUpdating ? PATH_UPDATE_USER : PATH_SAVE_USER}, 
              isUpdating ? Method.PUT : Method.POST, shouldRequireAuth(user)); //true funciona crear admin pero no funciona sign in sin iniciar sesion
    }
    
    private static boolean shouldRequireAuth(User user) {
        User currentUser = ResourceManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            return false;
        }

        if (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.CLIENT) {
            return true;
        }

        return false;
    }
}
