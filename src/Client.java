import java.util.Scanner;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Client class
 *
 * @author Kerly Titus
 */

public class Client extends Thread {

    private static int numberOfTransactions; /* Number of transactions to process */
    private static int maxNbTransactions; /* Maximum number of transactions */
    private static Transactions[] transaction; /* Transactions to be processed */
    private static Network objNetwork; /* Client object to handle network operations */
    private String clientOperation; /* sending or receiving */

    /**
     * Constructor method of Client class
     * 
     * @return
     * @param
     */
    Client(String operation) {
        if (operation.equals("sending")) {
            Logger.println("\n Initializing client sending application ...");
            numberOfTransactions = 0;
            maxNbTransactions = 100;
            transaction = new Transactions[maxNbTransactions];
            objNetwork = new Network("client");
            clientOperation = operation;
            Logger.println("\n Initializing the transactions ... ");
            readTransactions();
            Logger.println("\n Connecting client to network ...");
            String cip = objNetwork.getClientIP();
            if (!(objNetwork.connect(cip))) {
                Logger.println("\n Terminating client application, network unavailable");
                System.exit(0);
            }
        } else if (operation.equals("receiving")) {
            Logger.println("\n Initializing client receiving application ...");
            clientOperation = operation;
        }
    }

    /**
     * Accessor method of Client class
     * 
     * @return numberOfTransactions
     * @param
     */
    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    /**
     * Mutator method of Client class
     * 
     * @return
     * @param nbOfTrans
     */
    public void setNumberOfTransactions(int nbOfTrans) {
        numberOfTransactions = nbOfTrans;
    }

    /**
     * Accessor method of Client class
     * 
     * @return clientOperation
     * @param
     */
    public String getClientOperation() {
        return clientOperation;
    }

    /**
     * Mutator method of Client class
     * 
     * @return
     * @param operation
     */
    public void setClientOperation(String operation) {
        clientOperation = operation;
    }

    /**
     * Reading of the transactions from an input file
     * 
     * @return
     * @param
     */
    public void readTransactions() {
        Scanner inputStream = null; /* Transactions input file stream */
        int i = 0; /* Index of transactions array */

        try {
            inputStream = new Scanner(new FileInputStream("transaction.txt"));
        } catch (FileNotFoundException e) {
            Logger.println("File transaction.txt was not found");
            Logger.println("or could not be opened.");
            System.exit(0);
        }
        while (inputStream.hasNextLine()) {
            try {
                transaction[i] = new Transactions();
                transaction[i].setAccountNumber(inputStream.next()); /* Read account number */
                transaction[i].setOperationType(inputStream.next()); /* Read transaction type */
                transaction[i].setTransactionAmount(inputStream.nextDouble()); /* Read transaction amount */
                transaction[i].setTransactionStatus("pending"); /* Set current transaction status */
                i++;
            } catch (InputMismatchException e) {
                Logger.println("Line " + i + "file transactions.txt invalid input");
                System.exit(0);
            }

        }
        setNumberOfTransactions(i); /* Record the number of transactions processed */

        Logger.println(
                "\n DEBUG : Client.readTransactions() - " + getNumberOfTransactions() + " transactions processed");

        inputStream.close();

    }

    /**
     * Sending the transactions to the server
     * 
     * @return
     * @param
     */
    public void sendTransactions() {
        int i = 0; /* index of transaction array */

        while (i < getNumberOfTransactions()) {
            // while( objNetwork.getInBufferStatus().equals("full") ); /* Alternatively,
            // busy-wait until the network input buffer is available */

            while (objNetwork.getInBufferStatus().equals("full")) {
                Thread.yield();
            }
            transaction[i].setTransactionStatus("sent"); /* Set current transaction status */

            Logger.println("\n DEBUG : Client.sendTransactions() - sending transaction on account "
                    + transaction[i].getAccountNumber());

            objNetwork.send(transaction[i]); /* Transmit current transaction */
            i++;
        }

    }

    /**
     * Receiving the completed transactions from the server
     * 
     * @return
     * @param transact
     */
    public void receiveTransactions(Transactions transact) {
        int i = 0; /* Index of transaction array */

        while (i < getNumberOfTransactions()) {
            // while( objNetwork.getOutBufferStatus().equals("empty")); /* Alternatively,
            // busy-wait until the network output buffer is available */
            while (objNetwork.getOutBufferStatus().equals("empty")) {
                Thread.yield();
            }

            objNetwork.receive(transact); /* Receive updated transaction from the network buffer */

            Logger.println("\n DEBUG : Client.receiveTransactions() - receiving updated transaction on account "
                    + transact.getAccountNumber());

            Logger.println(transact); /* Display updated transaction */
            i++;
        }
    }

    /**
     * Create a String representation based on the Client Object
     * 
     * @return String representation
     * @param
     */
    public String toString() {
        return ("\n client IP " + objNetwork.getClientIP() + " Connection status"
                + objNetwork.getClientConnectionStatus() + "Number of transactions " + getNumberOfTransactions());
    }

    /**
     * Code for the run method
     * 
     * @return
     * @param
     */
    public void run() {
        Transactions transact = new Transactions();
        long serverStartTime, serverEndTime;
        serverStartTime = System.currentTimeMillis();

        if (clientOperation.equals("sending")) {
            sendTransactions();
        } else if (clientOperation.equals("receiving")) {
            receiveTransactions(transact);
            String cip = objNetwork.getClientIP();
            objNetwork.disconnect(cip);
        }

        serverEndTime = System.currentTimeMillis();
        Logger.println("\n Terminating client " + clientOperation + " thread - Running time "
                + (serverEndTime - serverStartTime) + " milliseconds");
    }
}
