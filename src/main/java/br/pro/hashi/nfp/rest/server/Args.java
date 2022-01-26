package br.pro.hashi.nfp.rest.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import br.pro.hashi.nfp.rest.server.exception.ExistenceArgumentException;
import br.pro.hashi.nfp.rest.server.exception.FormatArgumentException;

public class Args extends HashMap<String, String> {
	private static final long serialVersionUID = 7075288835789638466L;

	public String get(String name) {
		if (name == null) {
			throw new FormatArgumentException("Arg name cannot be null");
		}
		if (name.isBlank()) {
			throw new FormatArgumentException("Arg name cannot be blank");
		}
		if (containsKey(name)) {
			return super.get(name);
		} else {
			throw new ExistenceArgumentException("Arg %s does not exist".formatted(name));
		}
	}

	public boolean getBoolean(String name) {
		return Boolean.parseBoolean(get(name));
	}

	public int getInt(String name) {
		try {
			return Integer.parseInt(get(name));
		} catch (NumberFormatException exception) {
			throw new FormatArgumentException("Arg %s cannot be converted to int".formatted(name));
		}
	}

	public double getDouble(String name) {
		try {
			return Double.parseDouble(get(name));
		} catch (NumberFormatException exception) {
			throw new FormatArgumentException("Arg %s cannot be converted to double".formatted(name));
		}
	}

	public List<String> getList(String name, String regex) {
		String value = get(name);
		if (regex == null) {
			throw new FormatArgumentException("Regular expression cannot be null");
		}
		if (regex.isBlank()) {
			throw new FormatArgumentException("Regular expression cannot be blank");
		}
		return Arrays.asList(value.split(regex));
	}

	public List<Boolean> getListBoolean(String name, String regex) {
		List<Boolean> values = new ArrayList<>();
		for (String value : getList(name, regex)) {
			values.add(Boolean.parseBoolean(value));
		}
		return values;
	}

	public List<Integer> getListInt(String name, String regex) {
		List<Integer> values = new ArrayList<>();
		for (String value : getList(name, regex)) {
			try {
				values.add(Integer.parseInt(value));
			} catch (NumberFormatException exception) {
				throw new FormatArgumentException("Arg %s cannot be converted to list of ints".formatted(name));
			}
		}
		return values;
	}

	public List<Double> getListDouble(String name, String regex) {
		List<Double> values = new ArrayList<>();
		for (String value : getList(name, regex)) {
			try {
				values.add(Double.parseDouble(value));
			} catch (NumberFormatException exception) {
				throw new FormatArgumentException("Arg %s cannot be converted to list of doubles".formatted(name));
			}
		}
		return values;
	}
}
