package visualisation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import solution.InputHandler;

import java.io.File;
import java.io.IOException;

public class MainController {




    private Stage stage;
    private Parent root;


    public void openBrowse(MouseEvent event) throws IOException {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            switchToParameters(file.getName(), file.getAbsolutePath(), event);
        }
    }

    public void switchToParameters(String fileName, String filePath, MouseEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("parametersScreen.fxml"));
        root = loader.load();
        ParametersController parametersController = loader.getController();
        parametersController.setFileInfo(fileName, filePath);

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }





}
