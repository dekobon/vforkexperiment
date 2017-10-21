import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class VForkMe {
    private static final File LOCK_FILE = new File("/var/tmp/vforkme.pid");
    private static int maxProcesses = 4;

    private static boolean isParentProcess = false;

    public static void main(final String[] argv) throws Exception {
        final int secondsToRun;

        if (argv.length < 1) {
            secondsToRun = 60;
        } else {
            secondsToRun = Integer.parseInt(argv[0]);
        }

        if (argv.length < 2) {
            maxProcesses = 2;
        } else {
            maxProcesses = Integer.parseInt(argv[1]);
        }

        if (!LOCK_FILE.exists()) {
            isParentProcess = true;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (!LOCK_FILE.exists()) {
                    return;
                }

                try {
                    Files.delete(LOCK_FILE.toPath());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }));
        } else {
            final int totalProcesses = countPidsFromLockFile();
            System.out.printf("%d processes running\n", totalProcesses);

            if (totalProcesses >= maxProcesses) {
                return;
            }
        }

        writePidToLockFile();
        byte[][] memoryAllocated = allocateMemory(1);
        JavaProcess.exec(VForkMe.class, argv);

        if (isParentProcess) {
            int sleepCount = secondsToRun;

            for (int i = 0; i < sleepCount; i++) {
                Thread.sleep(1_000);
                System.out.println("*** parent is alive ***");
            }
        } else {
            Thread.sleep(1_000 * secondsToRun);
        }
    }

    private static byte[][] allocateMemory(final int gb) {
        return new byte[gb * 1_024][1_048_576];
    }

    private static int countPidsFromLockFile() throws IOException {
        if (!LOCK_FILE.exists()) {
            return 0;
        }

        try (Scanner scanner = new Scanner(readLockFileContents())) {
            int lines = 0;

            while (scanner.hasNextLine()) {
                scanner.nextLine();
                lines++;
            }

            return lines;
        }
    }

    private static String readLockFileContents() throws IOException {
        byte[] encoded = Files.readAllBytes(LOCK_FILE.toPath());
        return new String(encoded, StandardCharsets.UTF_8);
    }

    private static void writePidToLockFile() throws IOException {
        String pidAndHost = ManagementFactory.getRuntimeMXBean().getName();
        String pid = pidAndHost.split("@")[0];

        System.out.printf("pid %s started\n", pid);

        byte[] pidBytes = (pid + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);

        if (LOCK_FILE.exists()) {
            Files.write(LOCK_FILE.toPath(), pidBytes, StandardOpenOption.APPEND);
        } else {
            Files.write(LOCK_FILE.toPath(), pidBytes, StandardOpenOption.CREATE_NEW);
        }
    }
}
