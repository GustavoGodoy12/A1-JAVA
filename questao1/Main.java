import java.util.*;

public class Main {
    public static void main(String[] args) {
        ReportCreatorRegistry registry = ReportCreatorRegistry.getInstance();
        //aqui pode usar tambem sem registry e deixar com factory method meio que puro
        //ReportCreator dailyCreator = new DailyReportCreator();
        //reportCreator.weeklyCreator = new WeeklyReportCreator();
        //dailyCreator.generateReport();
        //weeklyCreator.generateReport();
        
        
        registry.register("diario", new DailyReportCreator());
        registry.register("semanal", new WeeklyReportCreator());

        ReportCreator dailyCreator = registry.get("diario");
        ReportCreator weeklyCreator = registry.get("semanal");

        if (dailyCreator != null) {
            dailyCreator.generateReport();
        }

        if (weeklyCreator != null) {
            weeklyCreator.generateReport();
        }
    }
}

interface Report {
    void generate();
}

abstract class BaseReport implements Report {
    protected final DataSourceStrategy dataSourceStrategy;
    protected final FormatStrategy formatStrategy;
    protected final String title;

    protected BaseReport(String title, DataSourceStrategy dataSourceStrategy, FormatStrategy formatStrategy) {
        this.title = title;
        this.dataSourceStrategy = dataSourceStrategy;
        this.formatStrategy = formatStrategy;
    }

    @Override
    public void generate() {
        List<String> metrics = dataSourceStrategy.loadRawMetrics();
        String formatted = formatStrategy.format(title, metrics);
        System.out.println(formatted);
    }
}

class DailyReport extends BaseReport {
    public DailyReport(DataSourceStrategy dataSourceStrategy, FormatStrategy formatStrategy) {
        super("Relatório Diário", dataSourceStrategy, formatStrategy);
    }
}

class WeeklyReport extends BaseReport {
    public WeeklyReport(DataSourceStrategy dataSourceStrategy, FormatStrategy formatStrategy) {
        super("Relatório Semanal", dataSourceStrategy, formatStrategy);
    }
}

interface DataSourceStrategy {
    List<String> loadRawMetrics();
}

class DailyDataSourceStrategy implements DataSourceStrategy {
    @Override
    public List<String> loadRawMetrics() {
        return Arrays.asList(
                "Pacotes entregues: 420",
                "Atrasos: 7",
                "Rotas críticas monitoradas: 3"
        );
    }
}

class WeeklyDataSourceStrategy implements DataSourceStrategy {
    @Override
    public List<String> loadRawMetrics() {
        return Arrays.asList(
                "Pacotes entregues na semana: 2980",
                "Custo operacional: R$ 154.300,00",
                "Nível médio de satisfação: 4,6/5"
        );
    }
}

interface FormatStrategy {
    String format(String title, List<String> metrics);
}

class SimpleTextFormatStrategy implements FormatStrategy {
    @Override
    public String format(String title, List<String> metrics) {
        StringBuilder builder = new StringBuilder();
        builder.append("=== ").append(title).append(" ===\n");
        for (String metric : metrics) {
            builder.append(metric).append("\n");
        }
        builder.append("------------------------------\n");
        return builder.toString();
    }
}

class DetailedTextFormatStrategy implements FormatStrategy {
    @Override
    public String format(String title, List<String> metrics) {
        StringBuilder builder = new StringBuilder();
        builder.append(" ").append(title.toUpperCase()).append(" \n");
        builder.append("Resumo das métricas priorizadas:\n");
        int index = 1;
        for (String metric : metrics) {
            builder.append(index++).append(") ").append(metric).append("\n");
        }
        builder.append(" FIM DO RELATÓRIO \n");
        return builder.toString();
    }
}

abstract class ReportCreator {
    public abstract Report createReport();

    public void generateReport() {
        Report report = createReport();
        report.generate();
    }
}

class DailyReportCreator extends ReportCreator {
    @Override
    public Report createReport() {
        return new DailyReport(
                new DailyDataSourceStrategy(),
                new SimpleTextFormatStrategy()
        );
    }
}

class WeeklyReportCreator extends ReportCreator {
    @Override
    public Report createReport() {
        return new WeeklyReport(
                new WeeklyDataSourceStrategy(),
                new DetailedTextFormatStrategy()
        );
    }
}

class ReportCreatorRegistry {
    private static final ReportCreatorRegistry INSTANCE = new ReportCreatorRegistry();
    private final Map<String, ReportCreator> creators = new HashMap<>();

    private ReportCreatorRegistry() {
    }

    public static ReportCreatorRegistry getInstance() {
        return INSTANCE;
    }

    public void register(String type, ReportCreator creator) {
        creators.put(type, creator);
    }

    public ReportCreator get(String type) {
        return creators.get(type);
    }
}
