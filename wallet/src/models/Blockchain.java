package models;

import java.security.Security;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import controllers.WalletController;
import main.Main;

public class Blockchain {
	
    public static ArrayList<Block> blockchain;
    public static ArrayList<Block> benchchain;
    private static Block currentBlock;
    public static ArrayList<Transaction> futureTransactions = new ArrayList<Transaction>();
 
	public static float minimumTransaction = 0.1f;
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //list of all unspent transactions. 
	public static Wallet walletA;
	public static Wallet walletB;
	public static Wallet coinbase;
	public static ArrayList<Wallet> wallets = new ArrayList<Wallet>();
	public static Transaction genesisTransaction;
	
	private static Set<Integer> nonceList = new HashSet<>();
    private static ArrayList<Integer> doubleNonceList  = new ArrayList<>();
    
    // Thread handling
	private ThreadPoolExecutor pool;
	private static final int MAX_THREADS = 10;
	
    public Blockchain() {
    	blockchain = new ArrayList<Block>();
    	benchchain = new ArrayList<Block>();
        currentBlock = null;
        pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);
    }
    
    public void generateGenesis() {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); 
		coinbase  = new Wallet("coinbase");
		walletA = new Wallet("My first Wallet");
		wallets.add(walletA);
		
		//create genesis transaction, which sends 100 NoobCoin to walletA: 
		genesisTransaction = new Transaction(coinbase.publicKey, wallets.get(0).publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction	
		genesisTransaction.transactionId = "0"; //manually set the transaction id
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.
		
		System.out.println("Creating and Mining Genesis block... ");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);
	}
  
    
    public static void addBlock(Block newBlock) {
    	newBlock.mineGenesisBlock();
    	currentBlock = newBlock;
		blockchain.add(currentBlock);
		currentBlock = null;
	}

    /*
     * Adds a new block to the chain and mines it or adds another thread in order to mine the current one faster
     */
    
    public void addBlock(String data) {
    	if(currentBlock == null) {
    		// Setting currentBlock as fast as possible in order to avoid overlapping of blocks and thus corrupting the chain
    		currentBlock = new Block(data, "");
    		
    		// Setting the previous hash
    		String previousHash = "0";
            if (blockchain.size() > 0) {
                Block previousBlock = blockchain.get(blockchain.size() - 1);
                previousHash = previousBlock.getHash();
            }
            
            // Creating a new block and updating currentBlock
            Block newBlock = new Block(data, previousHash);
            currentBlock = newBlock;
            
            currentBlock.addTransactions(futureTransactions);
            futureTransactions = new ArrayList<Transaction>();
         
            // Mining the block and adding it to an array
            mineBlock();
            Main.getController().writeToTerminal("\nMining new block...");
    	} else {
    		// If there is still space pool, then print msg, otherwise runnable will be in queue and waiting
    		if(pool.getActiveCount() < MAX_THREADS) {
        		Main.getController().writeToTerminal("Helping to mine the current block...");
    		}
    		mineBlock();
    	}
        
    }
    
    /*
     * Helper method, that creates and starts a thread
     */
    private void mineBlock() {
    	pool.execute(currentBlock);
    	System.out.println("Number of waiting jobs: " + pool.getQueue().size());
    }
    
    /*
     * Helper method, that closes the pool
     */
    public void shutDown() throws InterruptedException {
    	pool.shutdownNow();
    }
    
    /*
     * If the block has been mined, a thread will call this method to indicate that no more mining is needed by the other threads
     */
    public static synchronized void blockMined(long counter) {
    	if(hasBlockBeenMined())
    		return;
    	Main.getController().writeToTerminal("Block mined with " + new DecimalFormat( "#,###,###,##0" ).format(counter) + " tries!");
    	if(!(currentBlock.getTimeStamp() == 100))
    	{
    		blockchain.add(currentBlock);
    	} else {
    		benchchain.add(currentBlock);
    	}
    	
    	if(isChainValid()) {
    		rewardMiner();
    	}
    	
    	currentBlock = null;
    	return;
    }
    
    /*
     * Tells the running threads, if the currentBlock has been mined
     */
    public static boolean hasBlockBeenMined() {
    	if(currentBlock == null)
    		return true;
		return false;
	}

    /*
     * Prints the chain
     */
    public void printChain() {
    	boolean benchmark = Block.getBenchmark();
    	ArrayList<Block> chain = new ArrayList<Block>();
    	
    	if(benchmark) {
    		chain = benchchain;
    	} else {
    		chain = blockchain;
    	}
    	
    	if(chain.size() > 0) {
	    	String tableStr = "\n";
	    	tableStr += String.format("+--------------+--------------+--------------+---------------+%n");
	    	tableStr += String.format("| Hash         | Prev. hash   | Data         | Timestamp     |%n");
	        tableStr += String.format("+--------------+--------------+--------------+---------------+%n");
	        for (Block block : chain) {
	        	tableStr += block.toString();
	        }
	        tableStr += String.format("+--------------+--------------+--------------+---------------+%n");
	        
	        Main.getController().writeToTerminal(tableStr);
	
	        if(!benchmark) {
	        if (isChainValid()) {
	            Main.getController().writeToTerminal("The chain is valid!");
	        } else {
	        	Main.getController().writeToTerminal("CHAIN CORRUPTED!");
	        }
	        }
	        
    	} else {
    		Main.getController().writeToTerminal("The chain is empty.");
    	}
    }
    
    
    

    /*
     * Compares blocks and their hashes to determine if the chain is still valid
     */
    public static boolean isChainValid() {
        boolean returnCheck = true;
        Block currentBlock;
        Block previousBlock;
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            if (!currentBlock.getHash().equals(currentBlock.calculateHash(true, 0))) {
            	System.out.println("hash of " + i + " cant be recalculated");
                returnCheck = false;
            }

            if (!previousBlock.getHash().equals(currentBlock.getPrevHash())) {
            	System.out.println("prev hash of " + i + " doesnt mathc");
                returnCheck = false;
            }
        
        TransactionOutput tempOutput;
		for(int t=0; t < currentBlock.transactions.size(); t++) {
			Transaction currentTransaction = currentBlock.transactions.get(t);
			
			if(!currentTransaction.verifiySignature()) {
				System.out.println("#Signature on Transaction(" + t + ") is Invalid");
				returnCheck = false;
			}
			if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
				System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
				returnCheck = false;
			}
			
			for(TransactionInput input: currentTransaction.inputs) {	
				tempOutput = tempUTXOs.get(input.transactionOutputId);
				
				if(tempOutput == null) {
					System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
					returnCheck = false;
				}
				
				if(input.UTXO.value != tempOutput.value) {
					System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
					returnCheck = false;
				}
				
				tempUTXOs.remove(input.transactionOutputId);
			}
			
			for(TransactionOutput output: currentTransaction.outputs) {
				tempUTXOs.put(output.id, output);
			}
			
			if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
				System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
				returnCheck = false;
			}
			if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
				System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
				returnCheck = false;
			}
		}
    }
        return returnCheck;
}
    
    public static void addTransaction(Transaction transaction) {
    	futureTransactions.add(transaction);
    }
    
    public static float getMinimumTransaction() {
  		return minimumTransaction;
  	}

  	public static void setMinimumTransaction(float minimumTransaction) {
  		Blockchain.minimumTransaction = minimumTransaction;
  	}

  	public static HashMap<String, TransactionOutput> getUTXOs() {
  		return UTXOs;
  	}

  	public static void setUTXOs(HashMap<String, TransactionOutput> uTXOs) {
  		UTXOs = uTXOs;
  	}

  	public static Transaction getGenesisTransaction() {
  		return genesisTransaction;
  	}

  	public static void setGenesisTransaction(Transaction genesisTransaction) {
  		Blockchain.genesisTransaction = genesisTransaction;
  	}

	public static ArrayList<Wallet> getWallets() {
		return wallets;
	}

	public static void setWallets(ArrayList<Wallet> wallets) {
		Blockchain.wallets = wallets;
	}

	public static void addWallet(String name) {
		Wallet w = new Wallet(name);
		wallets.add(w);
	}

	public static Wallet getWalletPerName(String name) {
		Wallet wallet = new Wallet("invalid wallet");
		
		for(Wallet w : wallets) {
			if(w.getName().equals(name)) {
				wallet = w;
			}
		}
		return wallet;
	}
  	

	public static synchronized void addNonceToNonceList(int nonce) {
		if(nonce == 0) {
			return;
		}
		if(!(nonceList.add(nonce))) {
			doubleNonceList.add(nonce);
		}
	}
	
	public static void printDoubleNonceList() {
		for(int n : doubleNonceList) {
			System.out.println(n);
		}
		System.out.println(doubleNonceList.size() + " nonces were used twice!");
		doubleNonceList  = new ArrayList<>();
		nonceList = new HashSet<>();
	}
}
