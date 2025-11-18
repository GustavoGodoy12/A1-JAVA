package questao5;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Runnable tarefa = () -> {
            AppLogger logger = AppLogger.getInstance();
            String threadName = Thread.currentThread().getName();

            logger.logEvent("Thread " + threadName + " iniciou processamento");
            logger.logAudit("Thread " + threadName + " acessou recurso sensivel");
            logger.logError("Thread " + threadName + " encontrou um erro simulado");
        };

        Thread t1 = new Thread(tarefa, "T1");
        Thread t2 = new Thread(tarefa, "T2");
        Thread t3 = new Thread(tarefa, "T3");

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        AppLogger logger = AppLogger.getInstance();
        logger.logEvent("Processamento finalizado em todas as threads");
    }
}

class AppLogger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_FILE_PATH = "app.log";
    private final Object fileLock = new Object();

    private AppLogger() {
    }

    private static class Holder {
        private static final AppLogger INSTANCE = new AppLogger();
    }

    public static AppLogger getInstance() {
        return Holder.INSTANCE;
    }

    public void logError(String message) {
        log("ERROR", message);
    }

    public void logEvent(String message) {
        log("EVENT", message);
    }

    public void logAudit(String message) {
        log("AUDIT", message);
    }

    private void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String threadName = Thread.currentThread().getName();
        String line = timestamp + " [" + level + "] [" + threadName + "] " + message;
        writeToFile(line);
        sendToExternalServer(line);
    }

    private void writeToFile(String line) {
        synchronized (fileLock) {
            try (FileWriter fw = new FileWriter(LOG_FILE_PATH, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {
                out.println(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendToExternalServer(String line) {
        System.out.println("Enviando para servidor externo: " + line + "  Logger@" + System.identityHashCode(this));
    }
}
