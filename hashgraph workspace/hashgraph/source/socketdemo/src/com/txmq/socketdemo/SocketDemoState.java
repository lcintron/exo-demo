package com.txmq.socketdemo;

/*
 * This file is public domain.
 *
 * SWIRLDS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF 
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SWIRLDS SHALL NOT BE LIABLE FOR 
 * ANY DAMAGES SUFFERED AS A RESULT OF USING, MODIFYING OR 
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.swirlds.platform.Address;
import com.swirlds.platform.AddressBook;
import com.swirlds.platform.FCDataInputStream;
import com.swirlds.platform.FCDataOutputStream;
import com.swirlds.platform.FastCopyable;
import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldState;
import com.swirlds.platform.Utilities;
import com.txmq.exo.core.ExoPlatformLocator;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.persistence.BlockLogger;

import io.swagger.model.Animal;

/**
 * This holds the current state of the swirld. For this simple "hello swirld" code, each transaction is just
 * a string, and the state is just a list of the strings in all the transactions handled so far, in the
 * order that they were handled.
 */
public class SocketDemoState implements SwirldState {
	/** names and addresses of all members */
	private AddressBook addressBook;
	
	/**
	 * The zoo consists of a number of lions, tigers, and bears pushed into the zoo by users
	 */
	private List<String> lions = Collections
			.synchronizedList(new ArrayList<String>());

	/** @return all the strings received so far from the network */
	public synchronized List<String> getLions() {
		return lions;
	}

	public synchronized void addLion(String name) {
		this.lions.add(name);
	}
	
	private List<String> tigers = Collections
			.synchronizedList(new ArrayList<String>());

	/** @return all the strings received so far from the network */
	public synchronized List<String> getTigers() {
		return tigers;
	}

	public synchronized void addTiger(String name) {
		this.tigers.add(name);
	}
	
	private List<String> bears = Collections
			.synchronizedList(new ArrayList<String>());

	/** @return all the strings received so far from the network */
	public synchronized List<String> getBears() {
		return bears;
	}
	
	public synchronized void addBear(String name) {
		this.bears.add(name);
	}
	
	private List<String> endpoints = Collections
			.synchronizedList(new ArrayList<String>());

	/** @return all the strings received so far from the network */
	public synchronized List<String> getEndpoints() {
		return endpoints;
	}
	
	public synchronized void addEndpoint(String endpoint) {
		this.endpoints.add(endpoint);
	}

	// ///////////////////////////////////////////////////////////////////

	@Override
	public synchronized AddressBook getAddressBookCopy() {
		return addressBook.copy();
	}

	@Override
	public synchronized FastCopyable copy() {
		SocketDemoState copy = new SocketDemoState();
		copy.copyFrom(this);
		return copy;
	}

	@Override
	public void copyTo(FCDataOutputStream outStream) {
		/*
		try {
			Utilities.writeStringArray(outStream,
					strings.toArray(new String[0]));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	@Override
	public void copyFrom(FCDataInputStream inStream) {
		/*
		try {
			strings = new ArrayList<String>(
					Arrays.asList(Utilities.readStringArray(inStream)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}

	@Override
	public synchronized void copyFrom(SwirldState old) {
		lions = Collections.synchronizedList(new ArrayList<String>(((SocketDemoState) old).lions));
		tigers = Collections.synchronizedList(new ArrayList<String>(((SocketDemoState) old).tigers));
		bears= Collections.synchronizedList(new ArrayList<String>(((SocketDemoState) old).bears));
		endpoints = Collections.synchronizedList(new ArrayList<String>(((SocketDemoState) old).endpoints));
		addressBook = ((SocketDemoState) old).addressBook.copy();
		myName = ((SocketDemoState) old).myName;
	}

	@Override
	public synchronized void handleTransaction(long id, boolean consensus,
			Instant timeCreated, byte[] transaction, Address address) {
		
		/**
		 * Write a configurable mapper to automatically route a transaction to
		 * a handler based on transaction type.  @see SocketDemoMain
		 */
		if (consensus) {
			try {
				ExoMessage message = ExoMessage.deserialize(transaction);
				BlockLogger.addTransaction(message, this.myName);
				ExoPlatformLocator.getTransactionRouter().routeTransaction(message, this);
				/*
				switch (message.transactionType.getValue()) {
					case SocketDemoTransactionTypes.ADD_ANIMAL:
						Animal animal = (Animal) message.payload;
						switch (animal.getSpecies()) {
							case "lion":
								this.lions.add(animal.getName());
								break;
							case "tiger":
								this.tigers.add(animal.getName());
								break;
							case "bear":
								this.bears.add(animal.getName());
								break;
						}						
						break;
					case SocketDemoTransactionTypes.ANNOUNCE_NODE:
						this.endpoints.add((String) message.payload);
						break;
				}		*/	
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void noMoreTransactions() {
	}

	private String myName;
	@Override
	public synchronized void init(Platform platform, AddressBook addressBook) {
		this.myName = platform.getAddress().getSelfName();
		this.addressBook = addressBook;
	}
}	