package org.rr.jeborker;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import it.sauronsoftware.junique.MessageHandler;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.commons.utils.compression.rar.RarUtils;
import org.rr.jeborker.app.JeboorkerLogger;
import org.rr.jeborker.gui.MainController;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static org.rr.commons.utils.StringUtil.EMPTY;

public class Jeboorker {

    public static final ExecutorService APPLICATION_THREAD_POOL = new ThreadPoolExecutor(0, 1024,
            60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ApplicationThreadFactory()) {
    };

    public static final String APP = "Jeboorker";

    public static final String URL = "https://github.com/meerkatzenwildschein/jeboorker";

    private static MainController mainController;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        //setup the logger
        LoggerFactory.addHandler(new JeboorkerLogger());

        //Setup the location for the rar executables.
        if (ReflectionUtils.getOS() == ReflectionUtils.OS_WINDOWS) {
            RarUtils.setRarExecFolder(getAppFolder() + File.separator + "exec");
        } else {
            RarUtils.setRarExecFolder("/usr/bin");
        }

        boolean isRunning = handleAlreadyRunning();

        if (!isRunning) {
            try {
                logSystemInfo();

                mainController = MainController.getController();
            } catch (Exception e) {
                LoggerFactory.log(Level.SEVERE, null, "Main failed", e);
                System.exit(-1);
            }
        } else {
            // Sends arguments to the already active instance.
            JUnique.sendMessage(Jeboorker.class.getName(), EMPTY);

            LoggerFactory.log(Level.INFO, Jeboorker.class, "Jeboorker " + getAppVersion() + " is already running.");
        }
        closeSplashScreen();
    }

    private static boolean handleAlreadyRunning() {
        try {
            JUnique.acquireLock(Jeboorker.class.getName(), new MessageHandler() {
                public String handle(String message) {
                    if (mainController != null) {
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                int state = mainController.getMainWindow().getExtendedState();
                                state &= ~Frame.ICONIFIED;

                                mainController.getMainWindow().setExtendedState(state);
                                mainController.getMainWindow().setFocusableWindowState(false);
                                mainController.getMainWindow().toFront();
                                mainController.getMainWindow().setFocusableWindowState(true);
                            }
                        });
                    }
                    return null;
                }
            });
            return false;
        } catch (AlreadyLockedException e) {
            // Application already running.
            return true;
        }
    }

    public static void closeSplashScreen() {
        if (SplashScreen.getSplashScreen() != null) {
            SplashScreen.getSplashScreen().close();
        }
    }

    public static String getAppVersion() {
        Properties properties = new Properties();
        try {
            URL resource = Jeboorker.class.getResource("/META-INF/maven/org.rr/jeboorker-main/pom.properties");
            if (resource != null) {
                try (InputStream pomProperties = resource.openStream()) {
                    properties.load(pomProperties);
                    return properties.getProperty("version");
                }
            } else {
                LoggerFactory.log(Level.SEVERE, Jeboorker.class, "No version file found.");
            }
        } catch (IOException e) {
            LoggerFactory.log(Level.SEVERE, Jeboorker.class, "Failed to load version.");
        }
        return "UNKNOWN"; // fallback version
    }

    /**
     * Dumps the application version and the vm start parameters to the log
     */
    private static void logSystemInfo() {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        List<String> args = bean.getInputArguments();
        String argsString = ListUtils.join(args, " ");

        LoggerFactory.getLogger().info("Jeboorker " + getAppVersion() + " started with " + argsString);

        Properties props = System.getProperties();
        Set<Object> keys = props.keySet();
        for (Object key : keys) {
            Object value = props.get(key);
            LoggerFactory.getLogger().info(StringUtil.toString(key) + "=" + StringUtil.toString(value));
        }
    }

    /**
     * Get the application folder. Jeboorker must be configured that the app folder
     * is the current directory.
     */
    public static String getAppFolder() {
        return System.getProperties().getProperty("user.dir");
    }

    private static class ApplicationThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final ThreadGroup group;

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final String namePrefix;

        ApplicationThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(final Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

}
