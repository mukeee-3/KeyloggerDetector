import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class KeyloggerDetector {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001B[36m";

    private static final List<String> SUSPICIOUS_KEYWORDS = List.of(
            "keylogger", "keyboard", "hook", "spy", "capture", "record"
    );

    public static void main(String[] args) {
        boolean simulate = args.length > 0 && args[0].equalsIgnoreCase("--simulate");
        printBanner();
        if (simulate) {
            simulatePositiveOutput();
        } else {
            runActualScan();
        }
        System.out.println(ANSI_CYAN + "\nScan complete. Stay secure." + ANSI_RESET);
    }

    private static void printBanner() {
        System.out.println(ANSI_CYAN);
        System.out.println("===============================================================");
        System.out.println("                    K E Y L O G G E R   D E T E C T O R");
        System.out.println("===============================================================");
        System.out.println("                           Version 1.0");
        System.out.println("===============================================================");
        System.out.println(ANSI_RESET);
    }

    private static void simulatePositiveOutput() {
        System.out.println(ANSI_YELLOW + "[SIMULATION MODE ENABLED]" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "\n[MODULE 1] Scanning Active Processes..." + ANSI_RESET);
        System.out.println(ANSI_RED + "[THREAT] keylogger.exe (PID 4521)" + ANSI_RESET);
        System.out.println(ANSI_RED + "[THREAT] system_hook.dll (PID 9982)" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "\n[MODULE 2] Scanning Persistence (Startup Entries)..." + ANSI_RESET);
        System.out.println(ANSI_RED + "[THREAT] HKCU\\Software\\Microsoft\\Windows\\Run\\KeyCapture" + ANSI_RESET);
        System.out.println(ANSI_RED + "[THREAT] HKLM\\Software\\Microsoft\\Windows\\Run\\SpyService" + ANSI_RESET);
    }

    private static void runActualScan() {
        System.out.println(ANSI_YELLOW + "[MODULE 1] Scanning Active Processes..." + ANSI_RESET);
        System.out.println(scanProcesses());
        System.out.println(ANSI_YELLOW + "\n[MODULE 2] Scanning Persistence (Startup Entries)..." + ANSI_RESET);
        System.out.println(scanPersistence());
    }

    private static String scanProcesses() {
        StringBuilder findings = new StringBuilder();
        List<String> processes = getCommandOutput(getProcessCommand());
        boolean found = false;
        for (String process : processes) {
            for (String keyword : SUSPICIOUS_KEYWORDS) {
                if (process.toLowerCase().contains(keyword)) {
                    findings.append(ANSI_RED + "[THREAT] " + ANSI_RESET).append(process).append("\n");
                    found = true;
                }
            }
        }
        if (!found)
            findings.append(ANSI_GREEN + "[OK] No suspicious processes detected." + ANSI_RESET);
        return findings.toString();
    }

    private static String scanPersistence() {
        StringBuilder findings = new StringBuilder();
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win"))
            return ANSI_YELLOW + "[INFO] Persistence scan only available on Windows." + ANSI_RESET;
        List<String> reg = new ArrayList<>();
        reg.addAll(getCommandOutput("reg", "query", "HKLM\\Software\\Microsoft\\Windows\\CurrentVersion\\Run"));
        reg.addAll(getCommandOutput("reg", "query", "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run"));
        boolean found = false;
        for (String line : reg) {
            for (String keyword : SUSPICIOUS_KEYWORDS) {
                if (line.toLowerCase().contains(keyword)) {
                    findings.append(ANSI_RED + "[THREAT] " + ANSI_RESET).append(line.trim()).append("\n");
                    found = true;
                }
            }
        }
        if (!found)
            findings.append(ANSI_GREEN + "[OK] No suspicious startup programs detected." + ANSI_RESET);
        return findings.toString();
    }

    private static List<String> getCommandOutput(String... command) {
        List<String> out = new ArrayList<>();
        try {
            Process p = new ProcessBuilder(command).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null)
                out.add(line);
        } catch (Exception e) {
            out.add("Error: " + e.getMessage());
        }
        return out;
    }

    private static String[] getProcessCommand() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            return new String[]{"cmd", "/c", "tasklist /fo csv /nh"};
        return new String[]{"/bin/sh", "-c", "ps -e"};
    }
}