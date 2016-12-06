package org.ejvm.frontend;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.ejvm.compilation.EjvmCompilationException;
import org.ejvm.compilation.EjvmCompiler;
import org.ejvm.inspection.EjvmProgram;
import org.ejvm.util.EjvmVersion;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * <p>
 * Command line frontend that provides access to the eJVM compiler.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 */
public class EjvmCompile {

	@Option(name = "-d", aliases = "--omit-debug-info", usage = "Omits debug information from the compiled IVJM executable.")
	private boolean omitDebugInformation = false;

	@Option(name = "-w", aliases = "--replace-whitespace", usage = "Replaces white space in the output file's name with underscores.")
	private boolean replaceWhitespace = false;

	@Option(name = "-v", aliases = "--version", usage = "Displays the version and exits.", help = true)
	private boolean showVersion = false;

	@Argument(metaVar = "FILE", required = true)
	private String filename;

	public static void main(String[] args) {
		EjvmCompile frontend = new EjvmCompile();
		CmdLineParser parser = new CmdLineParser(frontend);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("eJVMc sourcefile [options...]");
			parser.printUsage(System.err);
			return;
		}
		try {
			frontend.compile();
		} catch (EjvmCompilationException e) {
			System.err.println("Unable to compile: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Unable to open source or executable file: " + e.getMessage());
		}
	}

	private void compile() throws IOException, EjvmCompilationException {
		if (showVersion) {
			System.out.println("eJVM version " + EjvmVersion.currentVersion().toString());
			return;
		}
		Path sourceFile = Paths.get(filename);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		EjvmCompiler compiler = new EjvmCompiler(omitDebugInformation);
		compiler.compile(Files.newInputStream(sourceFile), out);
		byte[] executable = out.toByteArray();
		EjvmProgram program = new EjvmProgram(ByteBuffer.wrap(executable));
		String name = program.getProgramName() + ".exf";
		if (replaceWhitespace) {
			name = name.replaceAll("\\s+", "_");
		}
		Path outputFile = Paths.get(name);
		Files.copy(new ByteArrayInputStream(executable), outputFile, StandardCopyOption.REPLACE_EXISTING);
	}
}