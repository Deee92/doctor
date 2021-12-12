package se.wasp.doctor.processor;

import spoon.processing.AbstractProcessor;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtTypeReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MethodProcessor extends AbstractProcessor<CtMethod<?>> {
    private final static Logger LOGGER = Logger.getLogger(MethodProcessor.class.getName());

    List<String> methodsToProcess;
    Map<String, String> methodLines;
    boolean sourceOutput;

    MethodProcessor(Map<String, String> methodLines, boolean sourceOutput) {
        this.methodLines = methodLines;
        this.sourceOutput = sourceOutput;
    }

    private String getJavaDoc(String description,
                              String author,
                              String parameters,
                              String returned,
                              String thrownExceptions) {
        String doc = String.format(
                "/** \n" +
                        " * \n" +
                        " * %s" +
                        "%s" +
                        "%s" +
                        "%s" +
                        "%s" +
                        " */", description, author, parameters, returned, thrownExceptions);
        return doc;
    }


    private String stringifyMethodName(String methodName) {
        return methodName.replaceAll("([A-Z])", " $1").toLowerCase();
    }

    private String processAuthor(String filePath, String fileName,
                                 String lineRange) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("git", "blame", fileName, "-L", lineRange);
        builder.directory(new File(filePath));
        Process process = builder.start();
        String author = "";
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(System.getProperty("line.separator"));
        }
        String result = stringBuilder.toString();
        author = result.substring(10, result.indexOf("  "))
                .replace(")", "").replace("(", "");
        return author;
    }

    @Override
    public void process(CtMethod<?> method) {
        SourcePosition position = method.getOriginalSourceFragment().getSourcePosition();
        if (methodLines.entrySet().contains(Map.entry(position.getLine() + "", position.getEndLine() + ""))) {
            String description = stringifyMethodName(method.getSimpleName()) + "\n";
            description = description.replace("get", "Gets");
            description = description.replace("set", "Sets");
            String parameters = "";
            for (CtParameter<?> parameter : method.getParameters()) {
                parameters += " * @param  " + parameter.getSimpleName() + "\n";
            }
            String returned = "";
            if (!method.getType().toString().equals("void")) {
                returned = " * @return " + method.getType().getSimpleName() + "\n";
            }
            String thrownExceptions = "";
            for (CtTypeReference<?> thrownException : method.getThrownTypes()) {
                thrownExceptions += " * @throws  " + thrownException.getSimpleName() + "\n";
            }
            String author = "";

            String absolutePath = method.getOriginalSourceFragment().getSourcePosition().getFile().getAbsolutePath();
            String filePath = absolutePath.replaceAll("(.+\\/)([a-zA-Z0-9]+\\.java)", "$1");
            String fileName = absolutePath.replaceAll("(.+\\/)([a-zA-Z0-9]+\\.java)", "$2");
            String startLine = String.valueOf(method.getOriginalSourceFragment().getSourcePosition().getLine());
            String endLine = String.valueOf(method.getOriginalSourceFragment().getSourcePosition().getEndLine());
            String lineRange = startLine + "," + endLine;

            try {
                author = " * @author " + processAuthor(filePath, fileName, lineRange) + "\n";
            } catch (Exception e) {
                e.printStackTrace();
            }

            method.setDocComment(description + author + parameters + returned + thrownExceptions);
            LOGGER.info("Javadoc generated for " + method.getPath());
            if (!sourceOutput) {
//                System.out.println(getJavaDoc(description, author, parameters, returned, thrownExceptions));
                InputCSVProcessor.setSourcePrinted((CtClass<?>) method.getParent());
            }
        }
    }
}
