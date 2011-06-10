/*
 * $Id: Message.java,v 1.4 2008/01/28 21:28:37 edankert Exp $
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


/**
 * Representation of a message.
 *
 * @version	$Revision: 1.4 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 **/
public class Message {
	private static Distributor distributor = null;
	private Object content = null;
	private boolean isConsumed = false;

	/**
	 * Constructor for the message.
	 */
	public Message() {
		this( null);
	}

	/**
	 * Constructor for the message.
	 *
	 * @param content the message's content to be sent.
	 */
	public Message( Object content) {
		if ( distributor == null) {
			distributor = new Distributor();
		}

		this.content = content;
	}

	/**
	 * Sets the content.
	 * 
	 * @param content the message content.
	 */
	public void setContent( Object content) {
		this.content = content;
	}

	/**
	 * Returns the message.
	 * 
	 * @return the message's content 
	 */
	public Object getContent() {
		return content;
	}

	/**
	 * Consume the message.
	 **/
	public void consume() {
		isConsumed = true;
	}

	/**
	 * Check if the message has been consumed.
	 * 
	 * @return true if the message has been consumed.
	 **/
	public boolean isConsumed() {
		return isConsumed;
	}

	/**
	 * Send the message.
	 **/
	public void send() {
		distributor.distribute( this);
	}
}
