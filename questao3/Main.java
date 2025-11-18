package questao3;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        EnvironmentalSensor sensorSul = new EnvironmentalSensor("SUL-01", "Sul");
        EnvironmentalSensor sensorNorte = new EnvironmentalSensor("NORTE-02", "Norte");

        SensorObserver painel = new ControlPanelModule();
        SensorObserver alertas = new AlertModule();
        SensorObserver historico = new HistoryModule();

        sensorSul.addObserver(painel);
        sensorSul.addObserver(alertas);
        sensorSul.addObserver(historico);

        sensorNorte.addObserver(painel);
        sensorNorte.addObserver(alertas);

        sensorSul.updateReading(26.5, 0.65, 45.0);
        sensorSul.updateReading(32.1, 0.40, 120.0);

        sensorNorte.updateReading(29.0, 0.55, 80.0);
        sensorNorte.updateReading(35.2, 0.30, 180.0);

        sensorSul.removeObserver(alertas);
        sensorSul.updateReading(24.0, 0.70, 30.0);
    }
}

class SensorData {
    private final String sensorId;
    private final String regiao;
    private final double temperatura;
    private final double umidade;
    private final double indicePoluicao;
    private final long timestamp;

    public SensorData(String sensorId, String regiao, double temperatura, double umidade, double indicePoluicao, long timestamp) {
        this.sensorId = sensorId;
        this.regiao = regiao;
        this.temperatura = temperatura;
        this.umidade = umidade;
        this.indicePoluicao = indicePoluicao;
        this.timestamp = timestamp;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getRegiao() {
        return regiao;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public double getUmidade() {
        return umidade;
    }

    public double getIndicePoluicao() {
        return indicePoluicao;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

interface SensorObserver {
    void update(SensorData data);
}

interface SensorSubject {
    void addObserver(SensorObserver observer);
    void removeObserver(SensorObserver observer);
    void notifyObservers();
}

class EnvironmentalSensor implements SensorSubject {
    private final String id;
    private final String regiao;
    private final List<SensorObserver> observers = new ArrayList<>();
    private SensorData lastData;

    public EnvironmentalSensor(String id, String regiao) {
        this.id = id;
        this.regiao = regiao;
    }

    public void updateReading(double temperatura, double umidade, double indicePoluicao) {
        this.lastData = new SensorData(id, regiao, temperatura, umidade, indicePoluicao, System.currentTimeMillis());
        notifyObservers();
    }

    @Override
    public void addObserver(SensorObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(SensorObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        if (lastData == null) {
            return;
        }
        for (SensorObserver observer : observers) {
            observer.update(lastData);
        }
    }
}

class ControlPanelModule implements SensorObserver {
    @Override
    public void update(SensorData data) {
        System.out.println("Painel de Controle - Atualização recebida:");
        System.out.println("Sensor: " + data.getSensorId() + " e Região: " + data.getRegiao());
        System.out.println("Temperatura: " + data.getTemperatura() + " °C");
        System.out.println("Umidade: " + (data.getUmidade() * 100) + " %");
        System.out.println("Índice de Poluição: " + data.getIndicePoluicao());
        System.out.println("---------");
    }
}

class AlertModule implements SensorObserver {
    @Override
    public void update(SensorData data) {
        boolean alertaTemperatura = data.getTemperatura() > 30.0;
        boolean alertaPoluicao = data.getIndicePoluicao() > 100.0;
        if (!alertaTemperatura && !alertaPoluicao) {
            return;
        }
        System.out.println("Módulo de Alertas - Atenção na região " + data.getRegiao());
        if (alertaTemperatura) {
            System.out.println("Alerta: Temperatura elevada (" + data.getTemperatura() + " °C)");
        }
        if (alertaPoluicao) {
            System.out.println("Alerta: Índice de poluição crítico (" + data.getIndicePoluicao() + ")");
        }
        System.out.println("-------");
    }
}

class HistoryModule implements SensorObserver {
    private final List<SensorData> historico = new ArrayList<>();

    @Override
    public void update(SensorData data) {
        historico.add(data);
        System.out.println("Módulo Histórico - Registro adicionado para o sensor " + data.getSensorId());
        System.out.println("Total de registros armazenados: " + historico.size());
        System.out.println("-");
    }
}
