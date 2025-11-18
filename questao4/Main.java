package questao4;

public class Main {
    public static void main(String[] args) {
        FraudCheck cadeia = new SuspiciousAmountCheck(5000.0)
                .linkWith(new GeoLocationCheck("BR"))
                .linkWith(new UserHistoryCheck())
                .linkWith(new DeviceCheck());

        Transaction tx1 = new Transaction(
                "TX-001",
                3000.0,
                "BRL",
                "BR",
                "user-01",
                10,
                false,
                "device-001",
                true
        );

        Transaction tx2 = new Transaction(
                "TX-002",
                15000.0,
                "BRL",
                "BR",
                "user-02",
                30,
                false,
                "device-010",
                true
        );

        Transaction tx3 = new Transaction(
                "TX-003",
                4200.0,
                "BRL",
                "US",
                "user-03",
                20,
                false,
                "device-050",
                false
        );

        Transaction tx4 = new Transaction(
                "TX-004",
                4500.0,
                "BRL",
                "BR",
                "user-04",
                85,
                true,
                "device-999",
                true
        );

        processarTransacao(cadeia, tx1);
        processarTransacao(cadeia, tx2);
        processarTransacao(cadeia, tx3);
        processarTransacao(cadeia, tx4);
    }

    private static void processarTransacao(FraudCheck cadeia, Transaction tx) {
        System.out.println("Processando transação " + tx.getId());
        FraudCheckResult resultado = cadeia.check(tx);
        System.out.println("Aprovada: " + resultado.isApproved());
        System.out.println("Motivo: " + resultado.getMessage());
        System.out.println("--");
    }
}

class Transaction {
    private final String id;
    private final double amount;
    private final String currency;
    private final String country;
    private final String userId;
    private final int userRiskScore;
    private final boolean userHasChargebacks;
    private final String deviceId;
    private final boolean knownDevice;

    public Transaction(String id, double amount, String currency, String country, String userId, int userRiskScore, boolean userHasChargebacks, String deviceId, boolean knownDevice) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.country = country;
        this.userId = userId;
        this.userRiskScore = userRiskScore;
        this.userHasChargebacks = userHasChargebacks;
        this.deviceId = deviceId;
        this.knownDevice = knownDevice;
    }

    public String getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCountry() {
        return country;
    }

    public String getUserId() {
        return userId;
    }

    public int getUserRiskScore() {
        return userRiskScore;
    }

    public boolean isUserHasChargebacks() {
        return userHasChargebacks;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isKnownDevice() {
        return knownDevice;
    }
}

class FraudCheckResult {
    private final boolean approved;
    private final String message;

    private FraudCheckResult(boolean approved, String message) {
        this.approved = approved;
        this.message = message;
    }

    public static FraudCheckResult approved(String message) {
        return new FraudCheckResult(true, message);
    }

    public static FraudCheckResult rejected(String message) {
        return new FraudCheckResult(false, message);
    }

    public boolean isApproved() {
        return approved;
    }

    public String getMessage() {
        return message;
    }
}

abstract class FraudCheck {
    private FraudCheck next;

    public FraudCheck linkWith(FraudCheck next) {
        this.next = next;
        return next;
    }

    public FraudCheckResult check(Transaction tx) {
        FraudCheckResult result = doCheck(tx);
        if (!result.isApproved()) {
            return result;
        }
        if (next == null) {
            return result;
        }
        return next.check(tx);
    }

    protected abstract FraudCheckResult doCheck(Transaction tx);
}

class SuspiciousAmountCheck extends FraudCheck {
    private final double maxTrustedAmount;

    public SuspiciousAmountCheck(double maxTrustedAmount) {
        this.maxTrustedAmount = maxTrustedAmount;
    }

    @Override
    protected FraudCheckResult doCheck(Transaction tx) {
        if (tx.getAmount() > maxTrustedAmount) {
            return FraudCheckResult.rejected("Valor suspeito: " + tx.getAmount());
        }
        return FraudCheckResult.approved("Valor dentro do limite");
    }
}

class GeoLocationCheck extends FraudCheck {
    private final String allowedCountry;

    public GeoLocationCheck(String allowedCountry) {
        this.allowedCountry = allowedCountry;
    }

    @Override
    protected FraudCheckResult doCheck(Transaction tx) {
        if (!allowedCountry.equalsIgnoreCase(tx.getCountry())) {
            return FraudCheckResult.rejected("País não permitido: " + tx.getCountry());
        }
        return FraudCheckResult.approved("Geolocalização permitida");
    }
}

class UserHistoryCheck extends FraudCheck {
    @Override
    protected FraudCheckResult doCheck(Transaction tx) {
        if (tx.isUserHasChargebacks()) {
            return FraudCheckResult.rejected("Usuário com histórico de chargeback");
        }
        if (tx.getUserRiskScore() > 70) {
            return FraudCheckResult.rejected("Score de risco do usuário muito alto: " + tx.getUserRiskScore());
        }
        return FraudCheckResult.approved("Histórico do usuário aceitável");
    }
}

class DeviceCheck extends FraudCheck {
    @Override
    protected FraudCheckResult doCheck(Transaction tx) {
        if (!tx.isKnownDevice()) {
            return FraudCheckResult.rejected("Dispositivo incomum: " + tx.getDeviceId());
        }
        return FraudCheckResult.approved("Dispositivo reconhecido");
    }
}
