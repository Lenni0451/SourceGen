package net.lenni0451.sourcegen.steps;

import java.io.IOException;

public interface GeneratorStep {

    void printStep();

    void run() throws IOException;

}
