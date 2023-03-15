package controllers;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import main.Main;
import models.Block;
import models.Blockchain;
import models.Wallet;

public class WalletController {

	private int walletCounter = 0;

	@FXML
	private Button addWallet, newTransaction;

	@FXML
	private TextField wallet, amount, walletName, specificWallet;
	
	@FXML
	private ComboBox<Wallet> walletDropdown, sendToWallet;
	
	private ObservableList<Wallet> wallets = FXCollections.observableArrayList();

	@FXML
	public void initialize() {	
	wallet.setDisable(true);
	specificWallet.setDisable(true);
	
	wallets.add(Blockchain.getWallets().get(0));
	walletDropdown.getItems().addAll(wallets);
	walletDropdown.setValue(wallets.get(0));
	sendToWallet.getItems().addAll(wallets);
	sendToWallet.setValue(wallets.get(0));
	
	wallet.setText(Blockchain.getWallets().get(0).getBalance() + " Coins in total");
	specificWallet.setText(Blockchain.getWallets().get(0).getBalance() + " Coins in wallet '" + Blockchain.getWallets().get(0).getName() + "'");
	}

	
	public void comboChangeWallet() {
		specificWallet.setText(walletDropdown.getValue().getBalance() + " Coins in wallet '" + walletDropdown.getValue().getName() + "'");
	}
	
	public void addWallet() {
		Blockchain.addWallet(walletName.getText());
		walletDropdown.getItems().addAll(Blockchain.getWallets().get(Blockchain.getWallets().size()-1));
		sendToWallet.getItems().addAll(Blockchain.getWallets().get(Blockchain.getWallets().size()-1));
		walletName.setText("");
	}
	
	
	public void newTransaction() {
		String ammountString = amount.getText();
		float ammount = Float.parseFloat(ammountString);
		Blockchain.addTransaction(walletDropdown.getValue().sendFunds(sendToWallet.getValue().publicKey, ammount, false));
	}
}
