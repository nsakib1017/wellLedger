package com.subsel.healthledger.exception;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.util.Map;


// TODO: 19/9/20 Delete
public class CustomOAuth2ExceptionSerializer extends StdSerializer<CustomOAuth2Exception> {

	protected CustomOAuth2ExceptionSerializer() {
		super(CustomOAuth2Exception.class);
	}

	@Override
	public void serialize(CustomOAuth2Exception e, JsonGenerator generator, SerializerProvider provider) throws IOException {
		generator.writeStartObject();
		generator.writeObjectField("status", e.getHttpErrorCode());
		String message = e.getMessage();
		if (message != null) {
			message = HtmlUtils.htmlEscape(message);
		}
		generator.writeStringField("message", message);
		if (e.getAdditionalInformation()!=null) {
			for (Map.Entry<String, String> entry : e.getAdditionalInformation().entrySet()) {
				String key = entry.getKey();
				String add = entry.getValue();
				generator.writeStringField(key, add);
			}
		}
		generator.writeEndObject();
	}
}
