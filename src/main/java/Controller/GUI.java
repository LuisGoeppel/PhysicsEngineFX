package Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class GUI extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Image icon = new Image("C:\\Users\\goeppl\\IdeaProjects\\PhysicsEngineFX\\" +
                "src\\main\\resources\\Images\\EngineLogo.PNG");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("EngineView.fxml"));
        Parent root = loader.load();

        EngineController gameController = loader.getController();
        Scene gameScene = new Scene(root);
        gameScene.addEventFilter(KeyEvent.KEY_PRESSED, gameController::keyPressed);
        gameScene.addEventFilter(ScrollEvent.SCROLL, gameController::onScroll);
        gameController.init();

        stage.setTitle("PhysicsEngine2D");
        stage.getIcons().add(icon);
        stage.setScene(gameScene);
        stage.setResizable(false);
        stage.requestFocus();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}