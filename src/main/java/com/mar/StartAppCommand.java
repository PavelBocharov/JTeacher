package com.mar;

import com.mar.config.DBConfigurate;
import com.mar.service.BotService;
import com.mar.service.PeeAndPoopService;
import com.mar.service.db.DatabaseServiceImpl;
import com.mar.service.library.DBLibraryService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@CommandLine.Command(
        name = "java -jar jteach.jar",
        description = "Telegram Bot teacher."
)
public class StartAppCommand implements Runnable {

    public static final String ROOT_DIR = "root_dir_for_mount";

    @CommandLine.Option(
            names = {"-BT", "--botToken"},
            description = "Bot token from @BotFather. Example: '123456789:AAAA-abcdabcdabcdabcdabcdabcdabcdabcdabcdabc'.",
            required = true
    )
    private String botToken;

    @CommandLine.Option(
            names = {"-BPT", "--botPaPToken"},
            description = "Bot token from @BotFather. Example: '123456789:AAAA-abcdabcdabcdabcdabcdabcdabcdabcdabcdabc'.",
            required = false
    )
    private String poopBot;

    @CommandLine.Option(
            names = {"-D", "--dir"},
            description = "Dir path to JSON-files with Q&A. Example: 'g/temp/to/gif/files/'",
            required = true
    )
    private String rootDir = "./";

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
        try (InputStream banner = this.getClass().getClassLoader().getResourceAsStream("banner.txt")) {
            if (banner != null) {
                System.out.println(new String(banner.readAllBytes(), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.debug("Bot token: {}", botToken.substring(0, 11) + "******************************" + botToken.substring(40));
        log.debug("Root dir: {}", rootDir);
        log.debug("Start image: {}", startImage);

        System.setProperty(ROOT_DIR, rootDir);

        List<Thread> workers = new LinkedList<>();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.debug("Shutdown hook...");
            workers.forEach(Thread::interrupt);
            DBConfigurate.shutdown();
            log.debug("Shutdown hook... OK");
        }));

        if (botToken != null && botToken.length() == 46) {
            Thread botThread = new Thread(() -> {
                DBLibraryService dbLibraryService = new DBLibraryService(rootDir);
                log.debug("LibraryService init: {}", dbLibraryService);
                new BotService(botToken, dbLibraryService, new DatabaseServiceImpl(), startImage, baseImage);
            });
            workers.add(botThread);
        }

        if (poopBot != null && poopBot.length() == 46) {
            log.debug("Pee&Poop bot token: {}", poopBot.substring(0, 11) + "******************************" + poopBot.substring(40));
            Thread ppThread = new Thread(() -> {
                new PeeAndPoopService(poopBot, new DatabaseServiceImpl());
            });
            workers.add(ppThread);
        }

        workers.forEach(Thread::start);
    }
}
