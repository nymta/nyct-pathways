package com.nyct.dos.tpc.pathways.validator;

import com.nyct.dos.tpc.pathways.PathwaysLoader;
import com.nyct.dos.tpc.pathways.model.PathwaysData;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "nyct-pathways-validator", mixinStandardHelpOptions = true, version = "1.0")
public class PathwaysValidatorCli implements Callable<Integer> {

    @Parameters(index = "0")
    private File basePath;

    @Override
    public Integer call() throws IOException {
        final PathwaysLoader pl = new PathwaysLoader(basePath);
        final PathwaysData pd = pl.load();

        new PathwaysValidator(pd).validate();

        return 0;
    }

    public static void main(String... args) {
        System.exit(new CommandLine(new PathwaysValidatorCli()).execute(args));
    }

}
