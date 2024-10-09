package net.lenni0451.sourcegen.steps;

public interface GeneratorStep {

    void printStep();

    void run() throws Exception;

}
