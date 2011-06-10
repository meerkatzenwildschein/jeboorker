/*
 * $Id: DefaultPublisher.java,v 1.5 2008/01/28 21:28:37 edankert Exp $
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

import java.util.*;

/**
 * A default implmenentation of a publisher, used by the distributor to have one 
 * publisher for the Message class.
 *
 * @version	$Revision: 1.5 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 **/
public class DefaultPublisher implements Publisher {
	private List<Subscriber> subscribers = null;

	/**
	 * Constructs the DefaultPublisher with an empty list of subscribers.
	 */
	public DefaultPublisher() {
		subscribers = new ArrayList<Subscriber>();
	}
	
	/**
	 * Add a new Subscriber.
	 * 
	 * @param subscriber adds a message subscriber.
	 */
	public void addSubscriber(Subscriber subscriber) {
		subscribers.add(subscriber);
	}
	
	/**
	 * Remove a Subscriber.
	 * 
	 * @param subscriber removes a message subscriber.
	 */
	public void removeSubscriber(Subscriber subscriber) {
		subscribers.remove(subscriber);
	}

	/**
	 * Called when the message needs to be published.
	 * From here the message is distributed to all the subscribers.
	 *
	 * @param message the message to be published.
	 **/
	public void publish(Message message) {
		for (int i = 0; i < subscribers.size(); i++) {
			subscribers.get(i).handle(message);

			if (message.isConsumed()) {
				return;
			}
		}
	}
}
