package se.wasp.doctor.runner;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import se.wasp.doctor.processor.InputCSVProcessor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

@Command(name = "doctor (documentation-generator)", mixinStandardHelpOptions = true, version = "1.0",
        description = "Generates documentation (javadoc) for a given list of methods or classes")
public class Main implements Callable<Integer> {
    private static String inputAST;

    @CommandLine.Option(
            names = {"-s", "--source"},
            description = "Generate source files with output")
    private boolean sourceOutput;

    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

    @Override
    public Integer call() throws Exception {
        LOGGER.info("Generating documentation...");
        if (inputAST.length() > 0) {
            LOGGER.info(String.format("Working with the parsed AST piped in from the CLI"));
            InputCSVProcessor.process(inputAST, sourceOutput);
        }
        return 0;
    }

    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            content.append(line);
            content.append(System.lineSeparator());
        }
        inputAST = content.toString();
        LOGGER.info("Found piped input of length: " + inputAST.length());
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
