package Bridge;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PythonScriptLoader {

    private static final String SCRIPT_RESOURCE = "/DatabaseManager.py";

    /** Copies the bundled DatabaseManager.py resource into a 'temp' folder next to the running jar
     * (or the project root when running unpacked from the IDE) and returns its path. JEP needs a
     * real file path on disk since the script can't be run directly out of the jar, and placing it
     * as a sibling of 'data' lets the script find data/StoreData.db on its own, purely from its own
     * file location - see init_paths() in DatabaseManager.py. */
    public static Path tempCopy() {
        try {
            Path tempScriptPath = tempDir().resolve("DatabaseManager.py");
            try (InputStream scriptStream = PythonScriptLoader.class.getResourceAsStream(SCRIPT_RESOURCE)) {
                if (scriptStream == null) {
                    throw new IOException("Could not find " + SCRIPT_RESOURCE + " on the classpath");
                }
                Files.copy(scriptStream, tempScriptPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return tempScriptPath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** A 'temp' directory next to the running jar (or the project root when running unpacked
     * from the IDE), i.e. a sibling of 'data'. */
    private static Path tempDir() {
        try {
            Path dir = appHome().resolve("temp");
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** The directory the app is running from: the jar's own directory when packaged, or the
     * working directory when running unpacked classes from the IDE. */
    private static Path appHome() {
        try {
            Path codeSource = Path.of(PythonScriptLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(codeSource)) {
                return codeSource.getParent();
            }
        } catch (URISyntaxException ignored) {
            // fall through to the working-directory default below
        }
        return Path.of(System.getProperty("user.dir"));
    }
}
