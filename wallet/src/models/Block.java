package models;

import java.util.ArrayList;
import java.util.Date;
import java.security.*;

import main.Main;

public class Block implements Runnable {
	
    /*
     * These are the required fields for the Block
     */
    private String hash;
    private String prevHash;
    private String transactionData;
    private long timeStamp;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); //our data will be a simple message.
    public String merkleRoot;
    private final static int testState = 0;
    
    // For Benchmarking
	private static boolean benchmark = false;
    private long timeStampStart;
    private long timeStampEnd;
    
    private volatile static int sharedNonce;
    private int nonce;

    private static final int DIFFICULTY = 5;

    public Block(String transactionData, String prevHash) {
        this.transactionData = transactionData;
        this.prevHash = prevHash;
        this.timeStampStart = new Date().getTime();
        this.timeStamp = new Date().getTime();
        if(benchmark) {
        this.timeStamp = 100;
        this.prevHash = "0";
        }
        this.hash = calculateHash(false, 0);
        sharedNonce = 0;
        nonce = sharedNonce;
    }
    
    public Block(String prevHash) {
        this.prevHash = prevHash;
        this.timeStamp = new Date().getTime();
     
        this.hash = calculateHash(false, 0);
        sharedNonce = 0;
        nonce = sharedNonce;
    }
    
    /*
     * Calculates the hash for the block
     */
    public String calculateHash(boolean chainValidation, int calcNonce) {
    	int hashNonce = calcNonce;
    	if(testState >= 1) {
    		hashNonce = sharedNonce;
		}
    	
    	Blockchain.addNonceToNonceList(hashNonce);
    	// Use calculated nonce for target hash, if this method is used to check if the chain is valid
    	if(chainValidation) {
    		hashNonce = nonce;
    	}
        String strToHash = this.prevHash + this.timeStamp + this.merkleRoot + hashNonce;
        try {
            byte[] bytesOfMessage = strToHash.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] thedigest = md.digest(bytesOfMessage);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < thedigest.length; i++) {
                String hex = Integer.toHexString(0xff & thedigest[i]);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Mines the block that is getting stored in the Blockchain
     */
    public void mineBlock() {
    	merkleRoot = StringUtil.getMerkleRoot(transactions);
    	String target = new String(new char[DIFFICULTY]).replace('\0', '0');
        String calcHash = "";
        transactionData = merkleRoot;
        int backUpNonce = 0;
       
        while (sharedNonce < Integer.MAX_VALUE && !Blockchain.hasBlockBeenMined() && !Thread.currentThread().isInterrupted()) {
        	if(testState == 2) {
        		backUpNonce = addNonce();
        	} else {
        		backUpNonce = addNonceSync();
        	}
            calcHash = calculateHash(false, backUpNonce);
            if (calcHash.substring(0, DIFFICULTY).equals(target)) {
            	nonce = backUpNonce;
            	hash = calcHash;
            	Blockchain.blockMined(nonce);
            	
            	if(benchmark) {
            		this.timeStampEnd = new Date().getTime();
            		double seconds = (double) (timeStampEnd-timeStampStart)/1000;
            		
            		Main.getController().writeToTerminal("It took " + seconds + " seconds to calculate the hash.");
            	}           	
            	break;
            }
        }
    }
    
    public void mineGenesisBlock() {
    	merkleRoot = StringUtil.getMerkleRoot(transactions);
    	String target = new String(new char[DIFFICULTY]).replace('\0', '0');
    	transactionData = merkleRoot;
		while(!hash.substring( 0, DIFFICULTY).equals(target)) {
			nonce ++;
			sharedNonce = nonce;
			hash = calculateHash(false, sharedNonce);
		}
		System.out.println("Block Mined!!! : " + hash);
		Blockchain.printDoubleNonceList();
    }
    
    public synchronized int addNonceSync() {
    	return ++sharedNonce;
    }
    
    public int addNonce() {
    	return ++sharedNonce;
    }
    
    /*
     * Generates a row for the table
     */
    public String toString(){
        String leftAlignFormat = "| %-12s | %-12s | %-12s | %-13s |%n";
        return String.format(leftAlignFormat, substring(hash, 0, 12), substring(prevHash, 0, 12), substring(transactionData, 0, 12), timeStamp);
    }
    
    /*
     * Starts the mining process
     */
    @Override
    public void run() {
        mineBlock();
    }
    
    /*
     * Substring utility method
     */
    private String substring(String s, int start, int end) {
    	if(s.length() == end) {
    		return s.substring(start, end);
    	} else if(s.length() > end) {
    		return s.substring(start, end - 2) + "..";
    	} else {
    		return s.substring(start, s.length());
    	}
    }

    /*
     * Getter and Setter
     */
    
    public void setTransactionData(String transactionData) {
        this.transactionData = transactionData;
    }

    public String getHash() {
        return this.hash;
    }

    public String getPrevHash() {
        return this.prevHash;
    }
    
    public static boolean getBenchmark() {
    	return benchmark;
    }
    
    /*
     *  Toggle the Benchmark
     */
     public static void toggleBenchmark() {
    	 benchmark = !benchmark;
    	 if(benchmark) {
    		 Main.getController().writeToTerminal("\nThe Benchmark is now active!");
    	 } else {
    		 Main.getController().writeToTerminal("\nBenchmark has been deactivated!");
    	 }
     }

	public long getTimeStamp() {
		return this.timeStamp;
	}
	
	//Add transactions to this block
		public boolean addTransaction(Transaction transaction) {
			//process transaction and check if valid, unless block is genesis block then ignore.
			if(transaction == null) return false;		
			if((prevHash != "0")) {
				if((transaction.processTransaction() != true)) {
					System.out.println("Transaction failed to process. Discarded.");
					return false;
				}
			}
			transactions.add(transaction);
			System.out.println("Transaction Successfully added to Block");
			return true;
		}
		
		public boolean addTransactions(ArrayList<Transaction> futureTransactions) {
			//process transaction and check if valid, unless block is genesis block then ignore.
			if(futureTransactions.size() == 0) return true;	
			for(Transaction transaction : futureTransactions) {
				if(transaction == null) continue;
			
				if((prevHash != "0")) {
					if((transaction.processTransaction() != true)) {
						System.out.println("Transaction failed to process. Discarded.");
						return false;
					}
				}
				transactions.add(transaction);
				System.out.println("Transaction Successfully added to Block");
			}
			return true;
		}
}