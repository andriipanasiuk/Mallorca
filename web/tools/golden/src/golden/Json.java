package golden;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Json {

	private Json() {
	}

	static Map<String, Object> object(Object... keyValues) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (int i = 0; i < keyValues.length; i += 2) {
			result.put((String) keyValues[i], keyValues[i + 1]);
		}
		return result;
	}

	static List<Object> array(Object... values) {
		List<Object> result = new ArrayList<>();
		for (Object value : values) {
			result.add(value);
		}
		return result;
	}

	static void write(Path path, Object value) throws IOException {
		Files.createDirectories(path.getParent());
		StringBuilder builder = new StringBuilder();
		append(builder, value);
		builder.append('\n');
		try (Writer writer = new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8)) {
			writer.write(builder.toString());
		}
	}

	private static void append(StringBuilder builder, Object value) {
		if (value == null) {
			builder.append("null");
		} else if (value instanceof String) {
			appendString(builder, (String) value);
		} else if (value instanceof Boolean) {
			builder.append(value.toString());
		} else if (value instanceof Integer || value instanceof Long) {
			builder.append(value.toString());
		} else if (value instanceof Double) {
			appendDouble(builder, (Double) value);
		} else if (value instanceof Map) {
			builder.append('{');
			boolean first = true;
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
				if (!first) {
					builder.append(',');
				}
				first = false;
				appendString(builder, (String) entry.getKey());
				builder.append(':');
				append(builder, entry.getValue());
			}
			builder.append('}');
		} else if (value instanceof List) {
			builder.append('[');
			boolean first = true;
			for (Object item : (List<?>) value) {
				if (!first) {
					builder.append(',');
				}
				first = false;
				append(builder, item);
			}
			builder.append(']');
		} else {
			throw new IllegalArgumentException("Cannot write " + value.getClass());
		}
	}

	private static void appendDouble(StringBuilder builder, double value) {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			throw new IllegalArgumentException("Not a finite number: " + value);
		}
		if (value == Math.rint(value) && Math.abs(value) < 1e15) {
			builder.append((long) value);
		} else {
			builder.append(Double.toString(value));
		}
	}

	private static void appendString(StringBuilder builder, String value) {
		builder.append('"');
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '"' || c == '\\') {
				builder.append('\\');
			}
			builder.append(c);
		}
		builder.append('"');
	}
}
