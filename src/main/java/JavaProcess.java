import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Thank you hallidave: https://stackoverflow.com/a/723914/33611
 */
public final class JavaProcess {

    private JavaProcess() {}

    public static void exec(final Class klass, final String[] argv) throws IOException,
            InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = klass.getCanonicalName();

        /* Runtime.exec internally uses ProcessBuilder:

             public Process exec(String[] cmdarray, String[] envp, File dir)
               throws IOException {
                return new ProcessBuilder(cmdarray)
                    .environment(envp)
                    .directory(dir)
                    .start();
            }
        */

        List<String> cmdArgs = new LinkedList<>();
        cmdArgs.add(javaBin);
        cmdArgs.add("-cp");
        cmdArgs.add(classpath);
        cmdArgs.add(className);
        cmdArgs.addAll(Arrays.asList(argv));

        ProcessBuilder builder = new ProcessBuilder(cmdArgs).inheritIO();
        Process process = builder.start();
    }
}
