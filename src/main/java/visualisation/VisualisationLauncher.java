package visualisation;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.stage.StageStyle;

import java.io.IOException;

public class VisualisationLauncher extends Application {
    public static boolean onlyVisualisationStatic;
    public static boolean usingParallelExecution;
    public static int numCores;

    public static void initiateLaunch(String[] args, boolean onlyVisualisation, boolean usingParallelExecution, int numCores) {
        onlyVisualisationStatic = onlyVisualisation;
        usingParallelExecution = usingParallelExecution;
        numCores = numCores;
        launch(args);
    }

    @Override
    public void start(Stage mainScreen) throws IOException {
            Parent root;

            if (onlyVisualisationStatic) {
                root = FXMLLoader.load(getClass().getClassLoader().getResource("mainScreen.fxml"));
            } else {
                root = FXMLLoader.load(getClass().getClassLoader().getResource("parametersScreen.fxml"));
            }

            mainScreen.setResizable(false);
            mainScreen.setTitle("Scheduling McQueen 3");
            mainScreen.getIcons().add(new Image("images/icon.png"));

            mainScreen.setScene(new Scene(root));
            mainScreen.show();


    }
}
