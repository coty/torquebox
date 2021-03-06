/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.torquebox.rack.core;

import javax.servlet.http.HttpServletResponse;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.rack.spi.RackResponse;

public class RackResponseImpl implements RackResponse {
	public static final String RESPONSE_HANDLER_RB = "org/torquebox/rack/core/response_handler";
	public static final String RESPONSE_HANDLER_CLASS_NAME = "TorqueBox::Rack::ResponseHandler";
	public static final String RESPONSE_HANDLER_METHOD_NAME = "handle";
	
	private IRubyObject rackResponse;

	public RackResponseImpl(IRubyObject rackResponse) {
		this.rackResponse = rackResponse;
	}

	public void respond(HttpServletResponse response) {
		Ruby ruby = rackResponse.getRuntime();
		ruby.getLoadService().require( RESPONSE_HANDLER_RB );
		RubyClass responseHandler = (RubyClass) ruby.getClassFromPath( RESPONSE_HANDLER_CLASS_NAME );
		JavaEmbedUtils.invokeMethod( ruby, responseHandler, RESPONSE_HANDLER_METHOD_NAME, new Object[]{ rackResponse, response }, Object.class );
	}
	
}
