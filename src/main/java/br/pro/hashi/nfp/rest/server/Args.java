package br.pro.hashi.nfp.rest.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import br.pro.hashi.nfp.rest.server.exception.ExistenceArgumentException;
import br.pro.hashi.nfp.rest.server.exception.FormatArgumentException;

public final class Args extends HashMap<String, String> {
	private static final long serialVersionUID = -7423139194700458380L;

	public final String get(String name) {
		if (this.containsKey(name)) {
			return super.get(name);
		} else {
			throw new ExistenceArgumentException("Arg %s does not exist".formatted(name));
		}
	}

	public final boolean getBoolean(String name) {
		return Boolean.parseBoolean(get(name));
	}

	public final int getInt(String name) {
		try {
			return Integer.parseInt(get(name));
		} catch (NumberFormatException exception) {
			throw new FormatArgumentException("Arg %s cannot be converted to int".formatted(name));
		}
	}

	public final double getDouble(String name) {
		try {
			return Double.parseDouble(get(name));
		} catch (NumberFormatException exception) {
			throw new FormatArgumentException("Arg %s cannot be converted to double".formatted(name));
		}
	}

	public final List<String> getList(String name, String regex) {
		return Arrays.asList(get(name).split(regex));
	}

	public final List<Boolean> getListBoolean(String name, String regex) {
		List<Boolean> values = new ArrayList<>();
		for (String value : getList(name, regex)) {
			values.add(Boolean.parseBoolean(value));
		}
		return values;
	}

	public final List<Integer> getListInt(String name, String regex) {
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

	public final List<Double> getListDouble(String name, String regex) {
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
