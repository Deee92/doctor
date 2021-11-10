package se.wasp.doctor.processor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import se.wasp.doctor.util.CSVHeadersEnum;
import spoon.Launcher;
import spoon.reflect.CtModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

import static se.wasp.doctor.util.CSVHeadersEnum.EXTRA_INFO;
import static se.wasp.doctor.util.CSVHeadersEnum.FILEPATH;

public class InputCSVProcessor {
    private final static Logger LOGGER = Logger.getLogger(InputCSVProcessor.class.getName());

    private static final CSVFormat csvFormat = CSVFormat.Builder
            .create(CSVFormat.DEFAULT)
            .setHeader(CSVHeadersEnum.class)
            .build();

    static List<String> methodPaths = new ArrayList<>();

    static Launcher launcher = new Launcher();

    public static void process(String content, boolean sourceOutput) throws IOException {
        Reader in = new InputStreamReader(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        Iterable<CSVRecord> records = csvFormat.parse(in);
        launcher.getEnvironment().setCommentEnabled(true);
        for (CSVRecord record : records) {
            if (!record.get(FILEPATH).equals("FILEPATH")) {
                launcher.addInputResource(record.get(FILEPATH));
                methodPaths.add(record.get(EXTRA_INFO).replaceAll("visibility=.+;", ""));
            }
        }

        CtModel model = launcher.buildModel();
        model.processWith(new MethodProcessor(methodPaths, sourceOutput));
        if (sourceOutput) {
            String outputDirectory = "./output/generated/";
            launcher.setSourceOutputDirectory(outputDirectory);
            launcher.prettyprint();
            LOGGER.info("Generated source output at " + outputDirectory);
        }
    }
}
