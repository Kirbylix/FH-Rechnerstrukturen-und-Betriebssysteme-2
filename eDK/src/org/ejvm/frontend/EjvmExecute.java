package org.ejvm.frontend;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ejvm.execution.EjvmExecutionListener;
import org.ejvm.execution.EjvmInstruction;
import org.ejvm.execution.EjvmMachine;
import org.ejvm.execution.EjvmProcess;
import org.ejvm.execution.EjvmProcess.ProcessState;
import org.ejvm.execution.EjvmProcessor;
import org.ejvm.util.EjvmVersion;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.OptionHandlerRegistry;
import org.kohsuke.args4j.spi.Messages;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * <p>
 * Command line frontend for the easy execution of an eJVM program.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 */
public class EjvmExecute {

	@Option(name = "-m", aliases = "--memory-size", usage = "Sets the memory size (in byte) for the eJVM machine. Default: 4096")
	private int memorySize = 4096;

	@Option(name = "-l", aliases = "--stack-line-size", usage = "Sets the number of hex bytes in a line in stack views. Default: 16")
	private int stackLineSize = 16;

	@Option(name = "-p", aliases = "--print-stack-modes", handler = StackModeHandler.class, usage = "Decides when to print out the program stack. Modes: STEP, INVOKE, RETURN")
	private List<PrintStackMode> printStackModes = new ArrayList<>();

	@Option(name = "-i", aliases = "--execution-interval", usage = "Sets the sleep interval (in milliseconds) between each program step. Default: 5")
	private long executionInterval = EjvmProcessor.DEFAULT_EXECUTION_INTERVAL;

	@Option(name = "-s", aliases = "--silent", usage = "Suppress status messages, e.g. when receiving a HALT.")
	private boolean silent = false;

	@Argument(metaVar = "FILE", required = true)
	private String filename;

	@Option(name = "-v", aliases = "--version", usage = "Displays the version and exits.", help = true)
	private boolean showVersion = false;

	public static void main(String[] args) {
		EjvmExecute executor = new EjvmExecute();
		OptionHandlerRegistry.getRegistry().registerHandler(Set.class, StackModeHandler.class);
		CmdLineParser parser = new CmdLineParser(executor);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("eJVM executable [options...]");
			parser.printUsage(System.err);
			return;
		}
		executor.run();
	}

	private void run() {
		if (showVersion) {
			System.out.println("eJVM version " + EjvmVersion.currentVersion().toString());
			return;
		}
		Path executable = Paths.get(filename);
		EjvmMachine machine = new EjvmMachine(memorySize);
		EjvmProcessor cpu = machine.loadProgram(executable);
		EjvmProcess process = cpu.getAssignedProcess();
		// process.addExecutionListener(new EjvmExecutionListener() {
		//
		// @Override
		// public void beforeExecution(EjvmProcess process, EjvmInstruction
		// instruction, Number... args) {}
		//
		// @Override
		// public void afterExecution(EjvmProcess process, EjvmInstruction
		// instruction, Number... args) {
		// System.out.print(instruction.name());
		// for (Number n: args) {
		// System.out.print(" " + n);
		// }
		// if (instruction == EjvmInstruction.IFLT) {
		// short value = process.popShortFromStack();
		// System.out.print(" (" + value + ")");
		// process.pushShortOnStack(value);
		// }
		// System.out.println();
		// }
		// });
		if (!silent) {
			// Register callback for HALT instructions.
			process.addExecutionListener(new EjvmExecutionListener() {

				@Override
				public void beforeExecution(EjvmProcess process, EjvmInstruction instruction, Number... args) {}

				@Override
				public void afterExecution(EjvmProcess process, EjvmInstruction instruction, Number... args) {
					if (instruction == EjvmInstruction.HALT) {
						System.out.println(">>MACHINE HALTED.");
						System.out.println(">>This process will now exit.");
						System.exit(0);
					} else if (instruction == EjvmInstruction.ERR) {
						if (args.length == 1) {
							int errorMessageIndex = args[0].intValue();
							String message = process.getProgram().getErrorMessage(errorMessageIndex);
							System.err.println(">>ERROR: " + message);
							System.err.println(">>Execution aborted.");
							System.exit(1);
						}
					}
				}
			});
		}
		/*
		 * We use the FP to detect method invocations and returns, in case we
		 * need to print them.
		 * 
		 * Using the EjvmExecutionListener would be more easy, but this piece of
		 * code was left as it is because it is a nice example of how to do
		 * stuff based only on how the process' attributes change.
		 */
		int currentFramePointer = process.getFramePointer();
		while (cpu.canStep()) {
			boolean printStack = printStackModes.contains(PrintStackMode.STEP) ? true : false;
			cpu.step();
			// Check if we have to print the program stack.
			if (process.getFramePointer() != currentFramePointer) {
				if (process.getFramePointer() < currentFramePointer) {
					// The stack grew, we have an invocation.
					if (printStackModes.contains(PrintStackMode.INVOKE))
						printStack = true;
				} else {
					// The stack shrunk, we have a return.
					if (printStackModes.contains(PrintStackMode.RETURN))
						printStack = true;
				}
				currentFramePointer = process.getFramePointer();
			}
			if (printStack && process.getProcessState() != ProcessState.BLOCKED) {
				printStack(process, stackLineSize);
			}
			try {
				Thread.sleep(executionInterval);
			} catch (InterruptedException e) {
				throw new RuntimeException(">>Execution sleep phase interrupted!", e);
			}
		}
		if (!silent) {
			System.out.println(">>Program exited normally.");
		}
	}

	private void printStack(EjvmProcess process, int lineSize) {
		System.out.println();
		ByteBuffer stackView = process.getStackView();
		int currentLine = 1;
		System.out.printf("0x%08X: ", process.getStackPointer());
		for (int i = 0; i < stackView.limit(); i++) {
			System.out.printf("%02X", stackView.get(i));
			if ((i + 1) % lineSize == 0) {
				System.out.println();
				System.out.printf("0x%08X: ", process.getStackPointer() + (lineSize * currentLine++));
			} else {
				System.out.print(" ");
			}
		}
		System.out.println();
	}

	/**
	 * <p>
	 * Enum to determine when to print a view of the program stack.
	 * </p>
	 * 
	 * @author Sebastian Nicolai Kaupe
	 */
	private static enum PrintStackMode {
		/**
		 * <p>
		 * Print stack every step.
		 * </p>
		 */
		STEP,
		/**
		 * <p>
		 * Print stack when a new method was entered.
		 * </p>
		 */
		INVOKE,
		/**
		 * <p>
		 * Print stack when a method was left.
		 * </p>
		 */
		RETURN;
	}

	protected static class StackModeHandler extends OptionHandler<PrintStackMode> {

		public StackModeHandler(CmdLineParser parser, OptionDef option, Setter<? super PrintStackMode> setter) {
			super(parser, option, setter);
		}

		@Override
		public int parseArguments(Parameters params) throws CmdLineException {
			int i = 0;
			while (i < params.size()) {
				try {
					PrintStackMode mode = PrintStackMode.valueOf(params.getParameter(i));
					setter.addValue(mode);
					i++;
				} catch (IllegalArgumentException e) {
					// TODO Rethink to make reordering possible.
					throw new CmdLineException(this.owner, Messages.ILLEGAL_OPERAND, params.getParameter(i));
				}
			}
			return params.size();
		}

		@Override
		public String getDefaultMetaVariable() {
			return "MODE...";
		}
	}
}