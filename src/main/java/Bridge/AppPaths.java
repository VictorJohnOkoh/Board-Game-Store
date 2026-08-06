package Bridge;

import java.net.URISyntaxException;
import java.nio.file.Path;

/**
 * Resolves the directories the app works from, so Java and the Python script agree
 * on where things live.
 * <p>
 * {@code DatabaseManager.py} derives the same home by going two levels up from its own
 * location in {@code temp/} - see {@code init_paths()} there. Keeping the Java side in
 * one place stops the two drifting apart.
 */
public final class AppPaths {

    private AppPaths() {
    }

    /** The directory the app runs from: the jar's own directory when packaged, or the
     * working directory when running unpacked classes from the IDE. */
    public static Path appHome() {
        try {
            Path codeSource = Path.of(AppPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (java.nio.file.Files.isRegularFile(codeSource)) {
                return codeSource.getParent();
            }
        } catch (URISyntaxException ignored) {
            // fall through to the working-directory default below
        }
        return Path.of(System.getProperty("user.dir"));
    }

    /** Where StoreData.db and its backups live. */
    public static Path dataDir() {
        return appHome().resolve("data");
    }

    /** Where saved receipts are written. */
    public static Path receiptsDir() {
        return dataDir().resolve("receipts");
    }
}
