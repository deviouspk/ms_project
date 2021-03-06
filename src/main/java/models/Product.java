package models;

import configs.SimulationConfig;
import enums.MachineType;
import enums.ProductType;

import java.util.ArrayList;

/**
 * Product that is send trough the system
 *
 * @author Joel Karel
 * @version %I%, %G%
 */
public class Product {
    /**
     * Stamps for the products
     */
    protected ArrayList<Double> times = new ArrayList<>();
    protected ArrayList<String> events = new ArrayList<>();
    protected ArrayList<String> stations = new ArrayList<>();

    protected final ProductType type;

    protected MachineType servicedBy;

    protected double productionTime = -1;

    protected double queueTime = -1;

    protected double additionalQueueTime = 0.0;

    protected double arrivalTime = -1.2345;

    /**
     * Constructor for the product
     * Pass the product type as param
     */
    public Product(ProductType type) {
        this.type = type;
    }

    public void setServicedBy(MachineType type) {
        this.servicedBy = type;
    }

    public MachineType getServicedBy() {
        if (this.servicedBy == null)
            throw new RuntimeException("Product has not been serviced by any machine");

        return servicedBy;
    }

    public void setProductionTime(double time) {
        if (productionTime != -1)
            throw new RuntimeException("Can only set production time when it's not initialized yet");

        this.productionTime = time;
    }

    public double getTimeInProduction() {
        if (this.productionTime == -1)
            this.productionTime = this.type().getServiceTimeDistribution().sample();

        return this.productionTime;
    }

    public double backTrackTimeForAnalysis(double time) {
        if (time < 0) {
            return this.backTrackTimeForAnalysis(time + SimulationConfig.SIMULATION_RUNTIME);
        }
        return time;
    }

    public void setQueueTime(double time) {
        if (queueTime != -1)
            throw new RuntimeException("Can only set production time when it's not initialized yet");

        this.queueTime = time;
    }

    public double getQueueTime() {
        if (!this.hasQueueTime())
            throw new RuntimeException("Product queue time not initialized");

        return this.queueTime + this.additionalQueueTime;
    }

    public boolean hasQueueTime() {
        return this.queueTime >= 0;
    }

    public void setArrivalTime(double time) {
        if (arrivalTime != -1.2345)
            throw new RuntimeException("Can only set production time when it's not initialized yet");

        this.arrivalTime = time;
    }

    public double getArrivalTime() {
        if (this.arrivalTime == -1.2345)
            throw new RuntimeException("Arrival time not initialized");

        return this.arrivalTime;
    }

    public ProductType type() {
        return this.type;
    }

    public void stamp(double time, String event, String station) {
        times.add(time);
        events.add(event);
        stations.add(station);
    }

    public ArrayList<Double> getTimes() {
        return times;
    }

    public ArrayList<String> getEvents() {
        return events;
    }

    public ArrayList<String> getStations() {
        return stations;
    }

    public double[] getTimesAsArray() {
        times.trimToSize();
        double[] tmp = new double[times.size()];
        for (int i = 0; i < times.size(); i++) {
            tmp[i] = (times.get(i)).doubleValue();
        }
        return tmp;
    }

    public String[] getEventsAsArray() {
        String[] tmp = new String[events.size()];
        tmp = events.toArray(tmp);
        return tmp;
    }

    public String[] getStationsAsArray() {
        String[] tmp = new String[stations.size()];
        tmp = stations.toArray(tmp);
        return tmp;
    }

    public ProductType getType() {
        return type;
    }

    public double getProductionTime() {
        return productionTime;
    }

    public double addAdditionalQueueTime(double time) {
        return additionalQueueTime += time;
    }
}