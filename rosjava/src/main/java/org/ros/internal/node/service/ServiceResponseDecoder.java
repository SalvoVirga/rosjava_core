/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.internal.node.service;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

/**
 * Decodes service responses.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
class ServiceResponseDecoder<ResponseType> extends ReplayingDecoder<ServiceResponseDecoderState> {

	private ServiceServerResponse response;

	public ServiceResponseDecoder() {
		reset();
	}

	private void reset() {
		checkpoint(ServiceResponseDecoderState.ERROR_CODE);
		response = new ServiceServerResponse();
	}
	
	public ServiceServerResponse getServiceResponse() {
		return response;
	}

	// TODO: check this
	@Override
	protected void decode(ChannelHandlerContext arg0, ByteBuf buf, List<Object> out) throws Exception {
		switch (state()) {
		case ERROR_CODE:
			response.setErrorCode(buf.readByte());
			out.add(buf.readByte());
			checkpoint(ServiceResponseDecoderState.MESSAGE_LENGTH);
		case MESSAGE_LENGTH:
			response.setMessageLength(buf.readInt());
			out.add(buf.readInt());
			checkpoint(ServiceResponseDecoderState.MESSAGE);
		case MESSAGE:
			response.setMessage(buf.readBytes(response.getMessageLength()));
			out.add(buf.readBytes(response.getMessageLength()));
			reset();
		default:
			throw new IllegalStateException();
		}
	}
}
