package com.mar;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
public class Main {

    public static void main(String[] args) {
        new CommandLine(new StartAppCommand()).execute(args);
    }

}