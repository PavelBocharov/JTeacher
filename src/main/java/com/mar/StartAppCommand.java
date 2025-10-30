package com.mar;

import com.mar.service.BotService;
import com.mar.service.db.DatabaseServiceImpl;
import com.mar.service.library.DBLibraryService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
        name = "java -jar jteach.jar",
        description = "Telegram Bot teacher."
)
public class StartAppCommand implements Runnable {

    @CommandLine.Option(
            names = {"-BT", "--botToken"},
            description = "Bot token from @BotFather. Example: '123456789:AAAA-abcdabcdabcdabcdabcdabcdabcdabcdabcdabc'.",
            required = true
    )
    private String botToken;

    @CommandLine.Option(
            names = {"-D", "--dir"},
            description = "Dir path to JSON-files with Q&A. Example: 'g/temp/to/gif/files/'",
            required = true
    )
    private String rootDir;

    @CommandLine.Option(
            names = {"-SI", "--startImage"},
            description = "Path to welcome image. Example: 'g/temp/to/gif/files/start.png'",
            required = false
    )
    private String startImage;

    @CommandLine.Option(
            names = {"-BI", "--baseImage"},
            description = "Path to default image. Example: 'g/temp/to/gif/files/base.png'",
            required = false
    )
    private String baseImage;


    @Override
    public void run() {
        new BotService(botToken, new DBLibraryService(rootDir), new DatabaseServiceImpl(), startImage, baseImage);
//        new BotService(botToken, new JSONLibraryService(rootDir), new DatabaseServiceImpl(), startImage, baseImage);
    }
}
