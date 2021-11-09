package se.wasp.doctor.processor;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.logging.Logger;

public class MethodProcessor extends AbstractProcessor<CtMethod<?>> {
    private final static Logger LOGGER = Logger.getLogger(MethodProcessor.class.getName());

    List<String> methodsToProcess;
    boolean sourceOutput;

    MethodProcessor(List<String> methodsToProcess, boolean sourceOutput) {
        this.methodsToProcess = methodsToProcess;
        this.sourceOutput = sourceOutput;
    }

    private String getJavaDoc(String description,
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
                        " */", description, parameters, returned, thrownExceptions);
        return doc;
    }


    private String stringifyMethodName(String methodName) {
        return methodName.replaceAll("([A-Z])", " $1").toLowerCase();
    }

    @Override
    public void process(CtMethod<?> method) {
        if (methodsToProcess.contains(method.getPath().toString())) {
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
            method.setDocComment(description + parameters + returned + thrownExceptions);
            LOGGER.info("Javadoc generated for " + method.getPath());
            if (!sourceOutput) {
                System.out.println(getJavaDoc(description, parameters, returned, thrownExceptions));
            }
        }
    }
}
