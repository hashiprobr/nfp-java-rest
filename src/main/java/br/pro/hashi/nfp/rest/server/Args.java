package br.pro.hashi.nfp.rest.server;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class Args extends HashMap<String, String> {
	private static final long serialVersionUID = 6649538312740878768L;

	private <T> List<T> map(String name, String regex, Function<String, T> mapper) {
		return Stream.of(split(name, regex)).map(mapper).toList();
	}

	private String[] split(String name, String regex) {
		String value = get(name);
		if (regex == null) {
			throw new IllegalArgumentException("Regular expression cannot be null");
		}
		regex = regex.strip();
		if (regex.isEmpty()) {
			throw new IllegalArgumentException("Regular expression cannot be blank");
		}
		return value.split(regex);
	}

	public String get(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Arg name cannot be null");
		}
		name = name.strip();
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Arg name cannot be blank");
		}
		String value = super.get(name);
		if (value == null) {
			throw new IllegalArgumentException("Arg %s does not exist".formatted(name));
		}
		return value;
	}

	public boolean getBoolean(String name) {
		return Boolean.parseBoolean(get(name));
	}

	public byte getByte(String name) {
		return Byte.parseByte(get(name));
	}

	public short getShort(String name) {
		return Short.parseShort(get(name));
	}

	public int getInt(String name) {
		return Integer.parseInt(get(name));
	}

	public long getLong(String name) {
		return Long.parseLong(get(name));
	}

	public float getFloat(String name) {
		return Float.parseFloat(get(name));
	}

	public double getDouble(String name) {
		return Double.parseDouble(get(name));
	}

	public BigInteger getBigInteger(String name) {
		return new BigInteger(get(name));
	}

	public BigDecimal getBigDecimal(String name) {
		return new BigDecimal(get(name));
	}

	public List<String> getList(String name, String regex) {
		return List.of(split(name, regex));
	}

	public List<Boolean> getListBoolean(String name, String regex) {
		return map(name, regex, Boolean::parseBoolean);
	}

	public List<Byte> getListByte(String name, String regex) {
		return map(name, regex, Byte::parseByte);
	}

	public List<Short> getListShort(String name, String regex) {
		return map(name, regex, Short::parseShort);
	}

	public List<Integer> getListInt(String name, String regex) {
		return map(name, regex, Integer::parseInt);
	}

	public List<Long> getListLong(String name, String regex) {
		return map(name, regex, Long::parseLong);
	}

	public List<Float> getListFloat(String name, String regex) {
		return map(name, regex, Float::parseFloat);
	}

	public List<Double> getListDouble(String name, String regex) {
		return map(name, regex, Double::parseDouble);
	}

	public List<BigInteger> getListBigInteger(String name, String regex) {
		return map(name, regex, BigInteger::new);
	}

	public List<BigDecimal> getListBigDecimal(String name, String regex) {
		return map(name, regex, BigDecimal::new);
	}
}
