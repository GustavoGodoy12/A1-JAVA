
package questao2;

import java.util.Locale;
import java.util.Scanner;

public class MainRisk {
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        Scanner sc = new Scanner(System.in);

        RiskContext cliente1 = new RiskContext(
                "Cliente A",
                35,
                8000.0,
                150000.0,
                10,
                0.8,
                15
        );

        RiskContext cliente2 = new RiskContext(
                "Cliente B",
                62,
                4000.0,
                300000.0,
                3,
                0.2,
                5
        );

        RiskAnalyzer analyzer = new RiskAnalyzer();

        boolean continuar = true;
        while (continuar) {
            System.out.println("Escolha o cliente:");
            System.out.println("1 - Cliente A");
            System.out.println("2 - Cliente B");
            int opcCliente = sc.nextInt();

            RiskContext selecionado;
            if (opcCliente == 1) {
                selecionado = cliente1;
            } else {
                selecionado = cliente2;
            }

            System.out.println("Escolha o modelo de cálculo de risco:");
            System.out.println("1 - Agressivo");
            System.out.println("2 - Moderado");
            System.out.println("3 - Conservador");
            int opcModelo = sc.nextInt();

            RiskModelType type;
            if (opcModelo == 1) {
                type = RiskModelType.AGRESSIVO;
            } else if (opcModelo == 2) {
                type = RiskModelType.MODERADO;
            } else {
                type = RiskModelType.CONSERVADOR;
            }

            analyzer.setStrategy(RiskStrategyFactory.create(type));
            RiskResult result = analyzer.analyze(selecionado);
            System.out.println(result);

            System.out.println("Deseja calcular outro perfil? (s/n)");
            String resp = sc.next();
            if (!resp.equalsIgnoreCase("s")) {
                continuar = false;
            }
        }

        sc.close();
    }
}

class RiskContext {
    private final String nomeCliente;
    private final int idade;
    private final double rendaMensal;
    private final double patrimonioTotal;
    private final int anosExperienciaInvestimentos;
    private final double toleranciaRisco;
    private final int horizonteInvestimentoAnos;

    public RiskContext(String nomeCliente, int idade, double rendaMensal, double patrimonioTotal, int anosExperienciaInvestimentos, double toleranciaRisco, int horizonteInvestimentoAnos) {
        this.nomeCliente = nomeCliente;
        this.idade = idade;
        this.rendaMensal = rendaMensal;
        this.patrimonioTotal = patrimonioTotal;
        this.anosExperienciaInvestimentos = anosExperienciaInvestimentos;
        this.toleranciaRisco = toleranciaRisco;
        this.horizonteInvestimentoAnos = horizonteInvestimentoAnos;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public int getIdade() {
        return idade;
    }

    public double getRendaMensal() {
        return rendaMensal;
    }

    public double getPatrimonioTotal() {
        return patrimonioTotal;
    }

    public int getAnosExperienciaInvestimentos() {
        return anosExperienciaInvestimentos;
    }

    public double getToleranciaRisco() {
        return toleranciaRisco;
    }

    public int getHorizonteInvestimentoAnos() {
        return horizonteInvestimentoAnos;
    }
}

interface RiskCalculationStrategy {
    double calculateScore(RiskContext context);
    String getName();
}

class AggressiveRiskStrategy implements RiskCalculationStrategy {
    @Override
    public double calculateScore(RiskContext context) {
        double idadeFactor = (100.0 - context.getIdade()) / 100.0;
        double patrimonioFactor = Math.min(context.getPatrimonioTotal() / 300000.0, 1.0);
        double experienciaFactor = Math.min(context.getAnosExperienciaInvestimentos() / 15.0, 1.0);
        double horizonteFactor = Math.min(context.getHorizonteInvestimentoAnos() / 20.0, 1.0);

        double score = 0.35 * context.getToleranciaRisco()
                + 0.2 * idadeFactor
                + 0.2 * patrimonioFactor
                + 0.15 * experienciaFactor
                + 0.1 * horizonteFactor;

        return clamp(score);
    }

    @Override
    public String getName() {
        return "Modelo Agressivo";
    }

    private double clamp(double value) {
        if (value < 0.0) return 0.0;
        if (value > 1.0) return 1.0;
        return value;
    }
}

class ModerateRiskStrategy implements RiskCalculationStrategy {
    @Override
    public double calculateScore(RiskContext context) {
        double idadeFactor = (80.0 - context.getIdade()) / 80.0;
        idadeFactor = Math.max(idadeFactor, 0.0);

        double rendaFactor = Math.min(context.getRendaMensal() / 10000.0, 1.0);
        double patrimonioFactor = Math.min(context.getPatrimonioTotal() / 250000.0, 1.0);
        double experienciaFactor = Math.min(context.getAnosExperienciaInvestimentos() / 10.0, 1.0);
        double horizonteFactor = Math.min(context.getHorizonteInvestimentoAnos() / 15.0, 1.0);

        double score = 0.25 * context.getToleranciaRisco()
                + 0.2 * idadeFactor
                + 0.2 * rendaFactor
                + 0.2 * patrimonioFactor
                + 0.1 * experienciaFactor
                + 0.05 * horizonteFactor;

        return clamp(score);
    }

    @Override
    public String getName() {
        return "Modelo Moderado";
    }

    private double clamp(double value) {
        if (value < 0.0) return 0.0;
        if (value > 1.0) return 1.0;
        return value;
    }
}

class ConservativeRiskStrategy implements RiskCalculationStrategy {
    @Override
    public double calculateScore(RiskContext context) {
        double idadeFactor = context.getIdade() / 100.0;
        double baixaToleranciaFactor = 1.0 - context.getToleranciaRisco();
        double patrimonioFactor = Math.min(context.getPatrimonioTotal() / 500000.0, 1.0);
        double curtoPrazoFactor = 1.0 - Math.min(context.getHorizonteInvestimentoAnos() / 20.0, 1.0);

        double score = 0.4 * baixaToleranciaFactor
                + 0.3 * idadeFactor
                + 0.2 * patrimonioFactor
                + 0.1 * curtoPrazoFactor;

        score = 1.0 - score;
        return clamp(score);
    }

    @Override
    public String getName() {
        return "Modelo Conservador";
    }

    private double clamp(double value) {
        if (value < 0.0) return 0.0;
        if (value > 1.0) return 1.0;
        return value;
    }
}

class RiskAnalyzer {
    private RiskCalculationStrategy strategy;

    public void setStrategy(RiskCalculationStrategy strategy) {
        this.strategy = strategy;
    }

    public RiskResult analyze(RiskContext context) {
        if (strategy == null) {
            throw new IllegalStateException("Nenhuma estratégia de cálculo configurada");
        }
        double score = strategy.calculateScore(context);
        String faixa = mapToRiskBand(score);
        return new RiskResult(context.getNomeCliente(), strategy.getName(), score, faixa);
    }

    private String mapToRiskBand(double score) {
        if (score < 0.33) return "Perfil Conservador";
        if (score < 0.66) return "Perfil Moderado";
        return "Perfil Agressivo";
    }
}

class RiskResult {
    private final String nomeCliente;
    private final String modeloUtilizado;
    private final double score;
    private final String faixaPerfil;

    public RiskResult(String nomeCliente, String modeloUtilizado, double score, String faixaPerfil) {
        this.nomeCliente = nomeCliente;
        this.modeloUtilizado = modeloUtilizado;
        this.score = score;
        this.faixaPerfil = faixaPerfil;
    }

    @Override
    public String toString() {
        return "Cliente: " + nomeCliente +
                "\nModelo: " + modeloUtilizado +
                "\nScore de risco: " + String.format(Locale.US, "%.2f", score) +
                "\nFaixa: " + faixaPerfil +
                "\n-------------------------";
    }
}

enum RiskModelType {
    AGRESSIVO,
    MODERADO,
    CONSERVADOR
}

class RiskStrategyFactory {
    public static RiskCalculationStrategy create(RiskModelType type) {
        if (type == null) {
            throw new IllegalArgumentException("Tipo de modelo não pode ser nulo");
        }
        switch (type) {
            case AGRESSIVO:
                return new AggressiveRiskStrategy();
            case MODERADO:
                return new ModerateRiskStrategy();
            case CONSERVADOR:
                return new ConservativeRiskStrategy();
            default:
                throw new IllegalArgumentException("Tipo de modelo não suportado: " + type);
        }
    }
}
