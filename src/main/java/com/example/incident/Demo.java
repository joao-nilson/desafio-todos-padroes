package com.example.incident;

import com.example.incident.alert.Alert;
import com.example.incident.channel.ChannelFactory;
import com.example.incident.channel.EmailChannelFactory;
import com.example.incident.channel.NotificationChannel;
import com.example.incident.channel.NotificationSender;
import com.example.incident.channel.PushChannelFactory;
import com.example.incident.channel.SmsChannelFactory;
import com.example.incident.channel.StandardChannel;
import com.example.incident.config.AppConfig;
import com.example.incident.decorator.LoggingSender;
import com.example.incident.decorator.RetryingSender;
import com.example.incident.factory.AlertFactory;
import com.example.incident.factory.ServiceOutageAlertFactory;
import com.example.incident.factory.ThresholdAlertFactory;
import com.example.incident.model.Recipient;
import com.example.incident.model.RecipientDirectory;
import com.example.incident.model.Severity;
import com.example.incident.observer.AlertListener;
import com.example.incident.observer.AuditLogListener;
import com.example.incident.observer.DashboardListener;
import com.example.incident.processing.AlertProcessor;
import com.example.incident.processing.EscalatingAlertProcessor;
import com.example.incident.processing.ProcessingReport;
import com.example.incident.processing.StandardAlertProcessor;
import com.example.incident.rules.AlertRule;
import com.example.incident.rules.DeduplicationRule;
import com.example.incident.rules.QuietHoursRule;
import com.example.incident.rules.RecipientEligibilityRule;
import com.example.incident.rules.RuleChain;
import com.example.incident.rules.SeverityFilterRule;
import com.example.incident.state.InvalidTransitionException;

import java.io.PrintStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Composition root: wires everything together and walks through the whole flow. */
public class Demo {

    public static void main(String[] args) {
        run(System.out);
    }

    public static void run(PrintStream out) {
        // Fixed clock (14:00 UTC) so the demo is deterministic and outside quiet hours.
        Clock clock = Clock.fixed(Instant.parse("2026-10-27T14:00:00Z"), ZoneOffset.UTC);
        AppConfig config = AppConfig.getInstance(); // Singleton, read once here

        // --- Abstract Factory + Decorator: build the channels ---
        NotificationChannel email = StandardChannel.from(new EmailChannelFactory());
        NotificationChannel push = StandardChannel.from(new PushChannelFactory());

        ChannelFactory smsFactory = new SmsChannelFactory(1); // first SMS send fails
        NotificationSender smsSender = new RetryingSender(
                new LoggingSender(smsFactory.createSender(), line -> out.println("    [sms-log] " + line)),
                config.getMaxRetryAttempts());
        NotificationChannel sms = new StandardChannel(
                smsSender, smsFactory.createFormatter(), smsFactory.createStatusChecker());

        // --- Recipients ---
        RecipientDirectory directory = new RecipientDirectory()
                .add(new Recipient("Ana", "ana@example.com", "+55 32 90000-0001", "ana-device", Severity.MEDIUM))
                .add(new Recipient("Bruno", "bruno@example.com", null, "bruno-device", Severity.HIGH))
                .add(new Recipient("Carla", "carla@example.com", "+55 32 90000-0003", null, Severity.LOW, false));

        // --- Observer: listeners shared by every alert ---
        AuditLogListener audit = new AuditLogListener();
        DashboardListener dashboard = new DashboardListener();
        List<AlertListener> listeners = Arrays.<AlertListener>asList(audit, dashboard);

        // --- Factory Method: one factory per alert type, each with its own channels (Bridge) ---
        AlertFactory thresholdFactory = new ThresholdAlertFactory(Arrays.asList(email, push), listeners);
        AlertFactory outageFactory = new ServiceOutageAlertFactory(Arrays.asList(email, sms, push), listeners);

        // --- Chain of Responsibility: easy to reorder or extend ---
        AlertRule chain = RuleChain.of(
                new SeverityFilterRule(config.getMinSeverity()),
                new QuietHoursRule(clock, config.getQuietHoursStart(), config.getQuietHoursEnd()),
                new RecipientEligibilityRule(directory),
                new DeduplicationRule());

        // --- Template Method: two processors sharing one workflow ---
        AlertProcessor standard = new StandardAlertProcessor(chain, directory, clock);
        AlertProcessor escalating = new EscalatingAlertProcessor(chain, directory, clock);

        // 1. HIGH threshold alert -> email + push
        out.println("== 1. Threshold alert (HIGH) via email + push");
        Alert cpu = thresholdFactory.create("A-1", "High CPU", Severity.HIGH,
                details("metric", "cpu_usage", "value", "97", "threshold", "90"));
        print(out, standard.process(cpu));

        // 2. CRITICAL outage -> email + sms (retried) + push
        out.println("== 2. Service outage (CRITICAL) via email + sms + push");
        Alert outage = outageFactory.create("A-2", "Checkout outage", Severity.CRITICAL,
                details("service", "checkout", "region", "sa-east-1"));
        print(out, escalating.process(outage));
        out.println("    metadata: " + outage.getMetadata());

        // 3. Same incident again -> rejected by deduplication
        out.println("== 3. Same threshold incident again");
        Alert cpuAgain = thresholdFactory.create("A-3", "High CPU", Severity.HIGH,
                details("metric", "cpu_usage", "value", "98", "threshold", "90"));
        print(out, standard.process(cpuAgain));

        // 4. LOW severity -> rejected by severity filter
        out.println("== 4. Threshold alert (LOW)");
        Alert disk = thresholdFactory.create("A-4", "Disk usage", Severity.LOW,
                details("metric", "disk_usage", "value", "81", "threshold", "80"));
        print(out, standard.process(disk));

        // 5. Lifecycle: valid transitions, then an invalid one
        out.println("== 5. Lifecycle of A-1");
        out.println("    status: " + cpu.getStatus());
        cpu.acknowledge();
        out.println("    status: " + cpu.getStatus());
        cpu.resolve();
        out.println("    status: " + cpu.getStatus());
        try {
            cpu.acknowledge();
        } catch (InvalidTransitionException e) {
            out.println("    rejected: " + e.getMessage());
        }

        out.println("== Audit log");
        for (String entry : audit.getEntries()) {
            out.println("    " + entry);
        }
        out.println("== Dashboard");
        out.println("    " + dashboard.summary());
    }

    private static Map<String, String> details(String... keyValues) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private static void print(PrintStream out, ProcessingReport report) {
        if (report.isRejected()) {
            out.println("    " + report.getAlertId() + " " + report.getRejection());
            return;
        }
        out.println("    " + report.getAlertId() + " processed, " + report.getRecipients().size() + " recipient(s):");
        report.getResults().forEach(r -> out.println("      " + r));
    }
}
