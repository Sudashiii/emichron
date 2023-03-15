package main;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import controllers.Controller;
import controllers.WalletController;

public class Main extends Application {

	private static Controller controller;
	
	private static double xOffset;
	private static double yOffset;

	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("../views/scene.fxml"));

			Scene scene = new Scene(root);
			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			scene.setFill(Color.TRANSPARENT);

			// Make window draggable
			scene.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					xOffset = primaryStage.getX() - event.getScreenX();
					yOffset = primaryStage.getY() - event.getScreenY();
				}
			});

			scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					primaryStage.setX(event.getScreenX() + xOffset);
					primaryStage.setY(event.getScreenY() + yOffset);
				}
			});

			Font.loadFont(getClass().getResource("Glass_TTY_VT220.ttf").toExternalForm(), 18);

			primaryStage.setTitle("Emichron");
			primaryStage.initStyle(StageStyle.TRANSPARENT);
			primaryStage.setScene(scene);
			primaryStage.show();

			System.out.println("Application running...");
			
			Parent wallet = FXMLLoader.load(getClass().getResource("../views/wallet.fxml"));
			Stage stage = new Stage();
			Scene wlt = new Scene(wallet);

			wlt.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					xOffset = stage.getX() - event.getScreenX();
					yOffset = stage.getY() - event.getScreenY();
				}
			});

			wlt.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					stage.setX(event.getScreenX() + xOffset);
					stage.setY(event.getScreenY() + yOffset);
				}
			});
			
			wlt.getStylesheets().add(getClass().getResource("wallet.css").toExternalForm());
			stage.setTitle("Wallet");
			stage.setScene(wlt);
			stage.initStyle(StageStyle.UNDECORATED);
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	// Getter and setter for the controller
	public static void setController(Controller control) {
		controller = control;
	}

	public static Controller getController() {
		return controller;
	}
}
