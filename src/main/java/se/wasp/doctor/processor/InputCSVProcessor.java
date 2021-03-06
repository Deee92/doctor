package se.wasp.doctor.processor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import se.wasp.doctor.util.CSVHeadersEnum;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

import static se.wasp.doctor.util.CSVHeadersEnum.*;

public class InputCSVProcessor {
    private final static Logger LOGGER = Logger.getLogger(InputCSVProcessor.class.getName());
    private static boolean printSource = false;
    static Set<CtClass<?>> parents = new LinkedHashSet<>();

    private static final CSVFormat csvFormat = CSVFormat.Builder
            .create(CSVFormat.DEFAULT)
            .setHeader(CSVHeadersEnum.class)
            .build();

    static List<String> methodPaths = new ArrayList<>();
    static Map<String, String> methodLines = new LinkedHashMap<String, String>();

    static Launcher launcher = new Launcher();

    public static void setSourcePrinted(CtClass<?> parent) {
        printSource = true;
        parents.add(parent);
    }

    public static void process(String content, boolean sourceOutput) throws IOException {
        Reader in = new InputStreamReader(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        Iterable<CSVRecord> records = csvFormat.parse(in);
        launcher.getEnvironment().setCommentEnabled(true);
        for (CSVRecord record : records) {
            if (!record.get(ABS_PATH).equals("ABS_PATH")) {
                launcher.addInputResource(record.get(ABS_PATH));
                methodLines.put(record.get(LINE_START), record.get(LINE_END));
            }
        }

        CtModel model = launcher.buildModel();
        model.processWith(new MethodProcessor(methodLines, sourceOutput));
        if (sourceOutput) {
            String outputDirectory = "./output/generated/";
            launcher.setSourceOutputDirectory(outputDirectory);
            launcher.prettyprint();
            LOGGER.info("Generated source output at " + outputDirectory);
        } else if (printSource) {
            System.out.println("=== Source file with generated documentation ===");
            parents.forEach(ctClass -> System.out.println(ctClass.prettyprint()));
        }
    }
}
