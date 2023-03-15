package controllers;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import main.Main;
import models.Block;
import models.Blockchain;

public class Controller extends VBox {

	private static Blockchain blockchain;

	@FXML
	private Button newBlock, benchmark, printChain, close;
	
	@FXML
	public TextField numberThreads;
	
	@FXML
	private TextArea terminal;
	
	@FXML
	private ImageView overlay;

	@FXML
	public void initialize() {
		Main.setController(this);
		blockchain = new Blockchain();
		
		// Make TextInput numeric
		numberThreads.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	numberThreads.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
		
		// Ignore events for images (keeps scroll of TextArea enabled)
		overlay.setMouseTransparent(true);
		
		// Show boot-up screen
		bootUpScreen();
		
		FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.15), terminal);
		fadeTransition.setFromValue(1.0);
		fadeTransition.setToValue(0.9);
		fadeTransition.setCycleCount(Animation.INDEFINITE);
		fadeTransition.play();
		blockchain.generateGenesis();
	}

	// Main methods
	public void mineNewBlock() {
		for (int i = 0; i < Integer.parseInt(numberThreads.getText()); i++) {
			blockchain.addBlock("xyz");
		}		
	}

	public void toogleBenchmark() {
		Block.toggleBenchmark();
	}
	
	public void printChain() {
		blockchain.printChain();
	}
	
	public void printDoubleNonce() {
		Blockchain.printDoubleNonceList();
	}
	
	// Helper methods
	
	public void close() throws InterruptedException {
		System.out.println("Starting shutdown sequence...");
		
		blockchain.shutDown();
    	System.out.println("Pool has been shut down!");
    	
		System.out.println("Application has been terminated!");
		Runtime.getRuntime().halt(0);
	}
	
	public void writeToTerminal(String text) {
		// Uses run later to make sure, that the javaFx thread processes this request (even under heavy load/many requests) and that it isn't outsourced to another non-fx thread
		Platform.runLater( () -> this.terminal.appendText(text + "\n") );
	}
	 
	//Convert this file to UTF8 if the screen looks wrong
	public void bootUpScreen() {
		String str = ""
				
//			+ "███████╗███╗   ███╗██╗ ██████╗██╗  ██╗██████╗  ██████╗ ███╗   ██╗\r\n" 
//			+ "██╔════╝████╗ ████║██║██╔════╝██║  ██║██╔══██╗██╔═══██╗████╗  ██║\r\n"
//			+ "█████╗  ██╔████╔██║██║██║     ███████║██████╔╝██║   ██║██╔██╗ ██║\r\n"
//			+ "██╔══╝  ██║╚██╔╝██║██║██║     ██╔══██║██╔══██╗██║   ██║██║╚██╗██║\r\n"
//			+ "███████╗██║ ╚═╝ ██║██║╚██████╗██║  ██║██║  ██║╚██████╔╝██║ ╚████║\r\n"
//			+ "╚══════╝╚═╝     ╚═╝╚═╝ ╚═════╝╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═══╝\r\n";
				
			+ " ▓█████ ███▄ ▄███▓██▓▄████▄  ██░ ██ ██▀███  ▒█████  ███▄    █ \r\n"
			+ " ▓█   ▀▓██▒▀█▀ ██▓██▒██▀ ▀█ ▓██░ ██▓██ ▒ ██▒██▒  ██▒██ ▀█   █ \r\n"
			+ " ▒███  ▓██    ▓██▒██▒▓█    ▄▒██▀▀██▓██ ░▄█ ▒██░  ██▓██  ▀█ ██▒\r\n"
			+ " ▒▓█  ▄▒██    ▒██░██▒▓▓▄ ▄██░▓█ ░██▒██▀▀█▄ ▒██   ██▓██▒  ▐▌██▒\r\n"
			+ " ░▒████▒██▒   ░██░██▒ ▓███▀ ░▓█▒░██░██▓ ▒██░ ████▓▒▒██░   ▓██░\r\n"
			+ " ░░ ▒░ ░ ▒░   ░  ░▓ ░ ░▒ ▒  ░▒ ░░▒░░ ▒▓ ░▒▓░ ▒░▒░▒░░ ▒░   ▒ ▒ \r\n"
			+ "  ░ ░  ░  ░      ░▒ ░ ░  ▒   ▒ ░▒░ ░ ░▒ ░ ▒░ ░ ▒ ▒░░ ░░   ░ ▒░\r\n"
			+ "    ░  ░      ░   ▒ ░        ░  ░░ ░ ░░   ░░ ░ ░ ▒    ░   ░ ░ \r\n"
			+ "    ░  ░      ░   ░ ░ ░      ░  ░  ░  ░        ░ ░          ░ \r\n"
			+ "                    ░                                         \n";		
		
		this.terminal.appendText(str);
	}

	public static Blockchain getBlockchain() {
		return blockchain;
	}
}
