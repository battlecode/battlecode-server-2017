package battlecode.engine.instrumenter.lang;

import java.util.regex.Pattern;

// This class allows us to instrument certain string operations.
// The instrumenter replaces calls to java.lang.String methods,
// which are not instrumented, with these methods, which are.

public class InstrumentableString {
	private InstrumentableString() { }

	static public boolean matches(String str, String regex) {
		return Pattern.matches(regex,str);
	}

	static public String replaceAll(String str, String regex, String replacement) {
		return Pattern.compile(regex).matcher(str).replaceAll(replacement);
	}

	static public String replaceFirst(String str, String regex, String replacement) {
		return Pattern.compile(regex).matcher(str).replaceFirst(replacement);
	}

	static public String [] split(String str, String regex) {
		return split(str,regex,0);
	}

	static public String [] split(String str, String regex, int limit) {
		return Pattern.compile(regex).split(str,limit);
	}
}