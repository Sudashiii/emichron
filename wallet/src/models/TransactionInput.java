package models;

public class TransactionInput {
	
	public String transactionOutputId; // Reference to TransactionOutputs -> transactionId
	public TransactionOutput UTXO; // Contains the Unspent transaction output
	
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}

	@Override
	public String toString() {
		return "TransactionInput [transactionOutputId=" + transactionOutputId + ", UTXO=" + UTXO + "]";
	}
}
