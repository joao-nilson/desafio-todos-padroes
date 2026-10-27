# Incident Alert System

A small, runnable proof of concept that uses nine design patterns to create incident alerts,
check them against rules, and deliver them through simulated email, SMS and push channels.
Everything is in memory: no database, network or API keys.

## Run it

```bash
mvn test              # run the unit tests
mvn compile exec:java # run the demo (com.example.incident.Demo)
```

Requires JDK 11 or newer and Maven 3.6+.

## How the pieces fit

```
AlertFactory (Factory Method) ──creates──▶ Alert ◀── subclasses: ThresholdAlert, ServiceOutageAlert
                                              │ has a list of
                                              ▼
                                   NotificationChannel (Bridge implementor)
                                              ▲ built from
                  ChannelFactory (Abstract Factory): sender + formatter + status checker
                                              ▲ sender may be wrapped by
                          RetryingSender / LoggingSender (Decorator)

AlertProcessor.process() (Template Method)
   validate ──▶ enrich ──▶ selectRecipients ──▶ deliver ──▶ recordResults
      │                                            │              │
  AlertRule chain                          alert.dispatchTo()   alert.recordDelivery()
  (Chain of Responsibility)                (Bridge)             (Observer notifies listeners)

Alert lifecycle: NEW ──▶ ACKNOWLEDGED ──▶ RESOLVED   (State; NEW ──▶ RESOLVED also allowed)
```

## Pattern → class mapping

| Pattern | Where | Purpose here |
|---|---|---|
| Factory Method | `factory.AlertFactory` (abstract `createAlert`), `ThresholdAlertFactory`, `ServiceOutageAlertFactory` | Each factory decides which `Alert` subclass to build and reads that type's details. The base class gives every alert its channels and listeners. |
| Abstract Factory | `channel.ChannelFactory`, `EmailChannelFactory`, `SmsChannelFactory`, `PushChannelFactory` | Each factory returns a matching sender, formatter and status checker for one channel. `StandardChannel` refuses mismatched parts. |
| Template Method | `processing.AlertProcessor` (final `process`), `StandardAlertProcessor`, `EscalatingAlertProcessor` | Fixes the order of the workflow; subclasses customize steps. |
| Chain of Responsibility | `rules.AlertRule`, `SeverityFilterRule`, `QuietHoursRule`, `RecipientEligibilityRule`, `DeduplicationRule`, `RuleChain` | Independent checks that either reject (stop the chain) or pass to the next. `RuleChain.of(a, b, c)` configures order. |
| Bridge | `alert.Alert` (abstraction) → `channel.NotificationChannel` (implementor) | Alert types own *content*; channels own *delivery*. Any alert type can use any combination of channels with no combined subclasses. |
| Singleton | `config.AppConfig` | One shared, immutable set of defaults (min severity, quiet hours, retry count). |
| State | `state.AlertState`, `NewState`, `AcknowledgedState`, `ResolvedState` | Each state defines which transitions are allowed and returns the next state; everything else throws `InvalidTransitionException`. |
| Observer | `observer.AlertListener`, `AuditLogListener`, `DashboardListener`, `AlertEvent`; subject is `Alert` | Listeners hear about status changes and recorded delivery results. |
| Decorator | `decorator.SenderDecorator`, `RetryingSender`, `LoggingSender` | Add retry and logging around any `NotificationSender` without changing it. |

## Where patterns overlap, and who owns what

- **Template Method vs. Chain of Responsibility.** The template decides *when* validation happens in the
  workflow. The chain decides *what* validation means. `StandardAlertProcessor.validate` simply calls
  the chain, so rules can be added or reordered without touching the workflow.
- **Abstract Factory vs. Bridge.** The Abstract Factory builds the *parts* of a channel and guarantees
  they match. The Bridge is the *relationship* between alerts and finished channels. `StandardChannel`
  is the join: it turns factory parts into a Bridge implementor.
- **Factory Method vs. Abstract Factory.** Factory Method answers "which kind of alert?"; Abstract
  Factory answers "which family of channel components?". They never create each other's products.
- **State vs. Observer.** State decides whether a transition is *allowed*. Observer reports that it
  *happened*. A rejected transition publishes nothing.
- **Decorator vs. Chain.** Decorators wrap one sender to change *how* it sends. The chain decides
  *whether* an alert is processed at all.

## Design choices and assumptions

- **Singleton scope.** Only `AppConfig` is a singleton, because it is read-only shared data. It is
  immutable, so sharing it needs no locking and tests cannot affect each other through it. Components
  never call `getInstance()`; `Demo` reads it once and passes values in. The public constructor is
  deliberate: tests build their own configs.
- **Dependency injection.** Clocks, directories, channels and listeners are passed through
  constructors. Tests use `Clock.fixed(...)`, so nothing depends on real time.
- **Invalid transitions throw** `InvalidTransitionException` instead of returning `false`, so a
  mistake cannot be ignored silently.
- **Observer subject is `Alert`.** Both status changes and delivery results go through one place.
  Rule rejections are reported in `ProcessingReport`, not as events.
- **Dedup rule is simple.** It remembers incidents forever (key = alert type + dedup key). A real
  system would expire keys. It sits last in the demo chain so alerts stopped earlier are not remembered.
- **Quiet hours** reject non-critical alerts (they are not queued for later). `CRITICAL` bypasses them.
- **Recipients without an address** for a channel get a `SKIPPED` result rather than an error.
- **Retry has no delay** (no sleeping), to keep it deterministic. A real one would back off.
- **Simulated delivery.** `SimulatedSender` returns receipts like `email-1`; `ReceiptStatusChecker`
  only recognizes receipts from its own channel. `SmsChannelFactory(1)` simulates a flaky provider
  that fails once.
- **Package layout.** A `model` package holds shared value types so that `channel` and `alert` do not
  depend on each other in a cycle: channels only see the `AlertContent` interface.
- **Java 11** is the target, matching the reference project. No records or newer syntax.
