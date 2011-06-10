/*
 * $Id: Distributor.java,v 1.5 2008/01/28 21:28:37 edankert Exp $
 *
 * Copyright (c) 2002 - 2008, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *	 this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 * 	 notice, this list of conditions and the following disclaimer in the 
 *	 documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *	 may  be used to endorse or promote products derived from this software 
 *	 without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.bounce.message;

import java.util.HashMap;


/**
 * <p>This class is the Distributor for all messages, it distributes the messages to the
 * topic publishers. The publishers distribute the messages to the subscribers and the
 * subscribers handle the messages.</p>
 *
 * <p>It keeps a hashtable of publisher per topic, every message will also be sent to all the
 * super topic publishers.</p>
 *
 * @version	$Revision: 1.5 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 **/
public class Distributor {
	HashMap<Class, Publisher> publishers = null;

	/**
	 * The constructor for the Distributor.
	 * It creates a Hashtable for all the publishers and sets the default
	 * publisher for the top Message 'topic'.
	 */
	public Distributor() {
		publishers = new HashMap<Class, Publisher>();
		setPublisher(Message.class, new DefaultPublisher());
	}
	
	/**
	 * Sets a publisher for a specific topic to the controller.
	 * 
	 * @param topic the type of the message to subscribe to.
 	 * @param publisher the publisher that handles the message.
	 */
	public void setPublisher(Class topic, Publisher publisher) {
		publishers.put(topic, publisher);
	}
	
	/**
	 * Gets a publisher for a specific topic from the controller.
	 * If the publisher is null it will return the first publisher
	 * found going up the tree.
	 * 
	 * @param topic the topic to get a publisher for.
	 *
	 * @return the publisher for this topic if this publisher is not found
	 *         it will return null.
	 */
	public Publisher getPublisher(Class topic) {
		return publishers.get(topic); 
	}
	
	/**
	 * Asks the publishers to publish the message.
	 * 
	 * @param message the message to publish.
	 */
	public void distribute(Message message) {
		handle(message.getClass(), message);
	}

	/*
	 * A recursive method to force the super message to be published
	 * before the extended message. Will keep informing publishers
	 * until the message is consumed.
	 * 
	 * @param topic the topic.
	 * @param message the message to be published.
	 */
	private void handle(Class topic, Message message) {
		
		// If the topic isn't equal to the Message base class, loop again.
		if (topic != Message.class) {
			handle(topic.getSuperclass(), message);
		}

		if (!message.isConsumed()) {
			// Send the message to the publisher.
			Publisher publisher = publishers.get(topic);

			if (publisher != null) {
				publisher.publish( message);
			}
		}
	}
}
