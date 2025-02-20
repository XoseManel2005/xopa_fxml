package ins.marianao.sailing.fxml;

import java.util.Locale;
import java.util.ResourceBundle;

import ins.marianao.sailing.fxml.manager.ResourceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class SailingApplication extends Application {

	@Override
	public void start(Stage primaryStage) {
		try {
			/*
			 * BorderPane root = new BorderPane(); Scene scene = new Scene(root,400,400);
			 * scene.getStylesheets().add(getClass().getResource("application.css").
			 * toExternalForm()); primaryStage.setScene(scene); primaryStage.show();
			 */

			ResourceManager.getInstance().setStage(primaryStage);
			ResourceManager.getInstance().setApp(this);

			Locale.setDefault(new Locale("CA","ES"));
			ResourceBundle applicationBundle = ResourceBundle.getBundle("application");
			ResourceManager.getInstance().setApplicationBundle(applicationBundle);
			ResourceBundle translationsBundle = ResourceBundle.getBundle("i18n/messages");
			ResourceManager.getInstance().setTranslationBundle(translationsBundle);
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewAppSailingGUI.fxml"), translationsBundle);
			VBox root = (VBox) loader.load();

			ResourceManager.getInstance().setMenuController(loader.getController());
			
			Scene scene = new Scene(root, 1100, 700);

			// scene.getStylesheets().add("http://fonts.googleapis.com/css?family=Catamaran:regular,bold,bolditalic");
			scene.getStylesheets().add("http://fonts.googleapis.com/css?family=Catamaran");
			scene.getStylesheets().add("http://fonts.googleapis.com/css?family=Dosis");
			scene.getStylesheets().add("http://fonts.googleapis.com/css?family=Special+Elite");
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setScene(scene);
	        
			// primaryStage.setResizable(false);
			primaryStage.setTitle(ResourceManager.getInstance().getText("fxml.text.app.title"));
			primaryStage.setMinWidth(1100);
			primaryStage.setMaxWidth(1600);
			primaryStage.setMinHeight(700);
			primaryStage.setMaxHeight(960);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch();
	}
}
