package analysis;

import charts.HistogramChart;
import configs.SimulationConfig;
import contracts.Aggregate;
import org.jfree.chart.ChartColor;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.*;
import statistics.TConfInterval;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;

public class SinkAnalysisAggregator {
    protected ArrayList<SinkAnalysis> sinkAnalyses = new ArrayList<>();

    protected String productType;

    public SinkAnalysisAggregator(String productType) {
        this.productType = productType;
    }

    public void add(SinkAnalysis analysis) {
        this.sinkAnalyses.add(analysis);
    }

    public int count() {
        return this.sinkAnalyses.size();
    }


    protected double[] aggregatePerHour(Aggregate<SinkAnalysis, double[]> aggr) {
        double[] aggregated = new double[24];

        for (SinkAnalysis analysis : this.sinkAnalyses) {
            double[] data = aggr.group(analysis);
            for (int h = 0; h < 24; h++) {
                double value = data[h];
                if (Double.isNaN(value)) {
                    value = 0;
                }
                aggregated[h] += (value / (double) this.count());
            }
        }

        return aggregated;
    }

    protected double[] aggregatePerMinute(Aggregate<SinkAnalysis, double[]> aggr) {
        double[] aggregated = new double[24 * 60];


        for (SinkAnalysis analysis : this.sinkAnalyses) {
            double[] data = aggr.group(analysis);
            for (int h = 0; h < 24; h++) {
                for (int m = 0; m < 60; m++) {
                    double value = data[60 * h + m];
                    if (Double.isNaN(value)) {
                        value = 0;
                    }
                    aggregated[60 * h + m] += (value / (double) this.count());

                }
            }
        }

        return aggregated;
    }

    protected double[] countPerMinute(Aggregate<SinkAnalysis, double[]> aggr) {
        double[] aggregated = new double[24 * 60];


        for (SinkAnalysis analysis : this.sinkAnalyses) {
            double[] data = aggr.group(analysis);
            for (int h = 0; h < 24; h++) {
                for (int m = 0; m < 60; m++) {
                    double value = data[60 * h + m];
                    if (Double.isNaN(value)) {
                        value = 0;
                    }
                    aggregated[60 * h + m] += (value);

                }
            }
        }

        return aggregated;
    }

    public double[] avgServiceTimesPerSimulation() {
        double times[] = new double[this.sinkAnalyses.size()];
        int counter = 0;
        for (SinkAnalysis analysis : this.sinkAnalyses) {
            times[counter] += analysis.getAvgProductionTime() / this.sinkAnalyses.size();
            counter++;
        }

        return times;
    }

    public double avgProbabilityQueueTimeLessThan(double duration, double minTime, double maxTime) {
        double probability = 0;
        for (SinkAnalysis analysis : this.sinkAnalyses) {
            probability += analysis.probabilityOfQueueTimeLessThan(duration, minTime, maxTime);
        }

        return probability / ((double) this.count());
    }

    public double[] avgProbabilityQueueTimeLessThanWithConfidence(double duration, double minTime, double maxTime, double confidence) {
        double[] probabilities = new double[this.sinkAnalyses.size()];
        double probability = 0;
        int count = 0;
        for (SinkAnalysis analysis : this.sinkAnalyses) {
            probabilities[count] = analysis.probabilityOfQueueTimeLessThan(duration, minTime, maxTime);
            probability += probabilities[count];
            count++;
        }


        TConfInterval tInterval = new TConfInterval(probabilities, confidence);
        double lowerBound = tInterval.lowerBound();

        double[] probabilityWithConfidence = new double[3];
        probabilityWithConfidence[0] = lowerBound < 0 ? 0 : lowerBound;
        probabilityWithConfidence[1] = tInterval.upperBound();
        probabilityWithConfidence[2] = probability / count;

        return probabilityWithConfidence;
    }

    public double avgProbabilityQueueTimeLessThan(double duration) {
        return this.avgProbabilityQueueTimeLessThan(duration, 0, SimulationConfig.SIMULATION_RUNTIME);
    }


    public double[] avgQueueTimesPerHour() {
        return this.aggregatePerHour(a -> a.avgQueueTimePerHour());
    }

    public double[] avgQueueTimesPerMinute() {
        return this.aggregatePerMinute(a -> a.avgQueueTimePerMinute());
    }

    public double[] totalAvgQueueTimePerSimulation() {
        double[] times = new double[this.sinkAnalyses.size()];
        int counter = 0;
        for (SinkAnalysis analysis : this.sinkAnalyses) {
            times[counter] = analysis.avgDailyQueueTime() / this.sinkAnalyses.size();
            counter++;
        }
        return times;
    }

    public double[] avgServiceTimeProbabilities() {
        double[] times = new double[1000];

        for (SinkAnalysis analysis : this.sinkAnalyses) {
            double[] simulationTimes = analysis.getAvgProductionTimeProbabilities();
            for (int i = 0; i < simulationTimes.length; i++) {
                times[i] += simulationTimes[i] / sinkAnalyses.size();

            }
        }

        return times;
    }

    public double[] avgServiceTimeFrequencies() {
        double[] times = new double[1000];

        for (SinkAnalysis analysis : this.sinkAnalyses) {
            double[] simulationTimes = analysis.getAvgProductionTimeFrequencies();
            for (int i = 0; i < simulationTimes.length; i++) {
                times[i] += simulationTimes[i];
            }
        }

        return times;
    }


    public double[] totalQueueHourTimePerSimulation() {
        double[] times = new double[this.sinkAnalyses.size()];
        int counter = 0;
        for (SinkAnalysis analysis : this.sinkAnalyses) {
            times[counter] = (analysis.totalDailyQueueTime() / 3600);
            counter++;
        }
        return times;
    }

    public double[] avgQueueHourTimePerSimulation() {
        double[] times = new double[this.sinkAnalyses.size()];
        int counter = 0;
        for (SinkAnalysis analysis : this.sinkAnalyses) {
            times[counter] = (analysis.avgDailyQueueTime() / 3600);
            counter++;
        }
        return times;
    }

    public double[][] avgQueueTimesPerMinutePerSimulation() {
        double[][] simulationQueueTimesPerMinute = new double[24 * 60][this.sinkAnalyses.size()];

        int i = 0;
        for (SinkAnalysis analysis : this.sinkAnalyses) {
            double[] data = analysis.avgQueueTimePerMinute();
            for (int h = 0; h < 24; h++) {
                for (int m = 0; m < 60; m++) {
                    double value = data[60 * h + m];
                    if (Double.isNaN(value)) {
                        value = 0;
                    }
                    simulationQueueTimesPerMinute[60 * h + m][i] = value;
                }
            }
            i++;
        }

        return simulationQueueTimesPerMinute;
    }


    public double[][] calculateConfidenceQueueTimesPerMinute(double confidence) {
        double[][] confidencePerMinute = new double[2][24 * 60];

        double[][] queueTimes = this.avgQueueTimesPerMinutePerSimulation();

        int counter = 0;
        String test;
        for (double[] simulationMinuteQueueTime : queueTimes) {

            if (counter > 16 * 60)
                test = "";
            TConfInterval tInterval = new TConfInterval(simulationMinuteQueueTime, confidence);
            double lowerBound = tInterval.lowerBound();
            confidencePerMinute[0][counter] = lowerBound < 0 ? 0 : lowerBound;
            confidencePerMinute[1][counter] = tInterval.upperBound();
            counter++;
        }
        return confidencePerMinute;
    }

    public double[] avgArrivalsPerHour() {
        return this.aggregatePerHour(a -> a.arrivalsPerHour());
    }

    public double[] avgArrivalsPerMinute() {
        return this.aggregatePerMinute(a -> a.arrivalsPerMinute());
    }

    public double[] countArrivalsPerMinute() {
        return this.countPerMinute(a -> a.arrivalsPerMinute());
    }

    public void plotAvgMinutelyQueueTimesWithConfidence(double confidence, boolean autoscale) {
        double[][] confidenceInterval = this.calculateConfidenceQueueTimesPerMinute(confidence);

        String confidenceString = confidence * 100 + " %";
        HistogramChart chart = new HistogramChart(confidenceString + " Interval " + this.productType + " Queue times ( " + this.count() + " simulation days) ", "hour", "queue time (min)");

        XYPlot plot3 = chart.getChart().getXYPlot();
        plot3.setRenderer(0, new SamplingXYLineRenderer());
        XYItemRenderer xyir3 = plot3.getRenderer(0);
        xyir3.setSeriesPaint(0, ChartColor.LIGHT_BLUE);
        chart.addSeries("Avg Queue time", this.avgQueueTimesPerMinute());

        XYBarRenderer bar1 = new XYBarRenderer();
        bar1.setDrawBarOutline(false);
        bar1.setShadowVisible(false);
        XYPlot plot1 = chart.getChart().getXYPlot();
        plot1.setRenderer(1, bar1);
        plot1.setOutlineVisible(false);
        XYItemRenderer xyir1 = plot1.getRenderer(1);
        xyir1.setSeriesPaint(0, ChartColor.LIGHT_GREEN);
        chart.addSeries("Avg Queue time Lower Bound", confidenceInterval[0]);

        XYBarRenderer bar = new XYBarRenderer();
        bar.setDrawBarOutline(false);
        bar.setShadowVisible(false);
        XYPlot plot2 = chart.getChart().getXYPlot();
        plot2.setRenderer(2, bar);
        plot2.setOutlineVisible(false);
        XYItemRenderer xyir2 = plot1.getRenderer(2);
        xyir2.setSeriesPaint(0, ChartColor.LIGHT_RED);
        chart.addSeries("Avg Queue time Upper Bound", confidenceInterval[1], 0.46);

        //chart.getRangeAxis().setRange(0, 31.8);
        //chart.getRangeAxis().setTickUnit(new NumberTickUnit(2));

        if (!autoscale)
            chart.getRangeAxis().setRange(0, 5);

        NumberTickUnit tickUnit = new NumberTickUnit(60) {
            @Override
            public String valueToString(double value) {
                return new DecimalFormat("##").format(value / 60) + "h";
            }
        };

        chart.getDomainAxis().setTickUnit(tickUnit);
        chart.getDomainAxis().setRange(0, 24 * 60-10);

        chart.render();
    }

    public void plotAvgHourlyQueueTimes() {
        HistogramChart chart = new HistogramChart(this.productType + " Queue times ( " + this.count() + " simulation days)", "hour", "queue time (min)");
        chart.addSeries(this.productType + " queue times", this.avgQueueTimesPerHour());

        NumberTickUnit tickUnit = new NumberTickUnit(1) {
            @Override
            public String valueToString(double value) {
                return new DecimalFormat("##").format(value) + "h";
            }
        };

        chart.getDomainAxis().setTickUnit(tickUnit);
        chart.render();
    }

    public void plotAvgServiceTimes() {
        HistogramChart chart = new HistogramChart(this.productType + " Avg Service times ( " + this.count() + " simulation days)", "simulations", "Service time (sec)");
        chart.addSeries(this.productType + " Service times", this.avgServiceTimesPerSimulation());

        NumberTickUnit tickUnit = new NumberTickUnit(10);

        chart.getDomainAxis().setTickUnit(tickUnit);
        chart.render();
    }


    public void plotAvgServiceTimeProbabilities() {
        HistogramChart chart = new HistogramChart(this.productType + " Service Time Probability Density ( " + this.count() + " simulation days)", "Service Time (sec)", "Probability");
        chart.addSeries(this.productType + " Service times", this.avgServiceTimeProbabilities());

        NumberTickUnit tickUnit = new NumberTickUnit(0.001) {
            @Override
            public String valueToString(double value) {
                return new DecimalFormat("##.00").format(value * 100) + " %";
            }
        };

        chart.getRangeAxis().setTickUnit(tickUnit);
        chart.getDomainAxis().setRange(0, 500);
        chart.render();
    }

    public void plotTotalDailyQueueTime() {
        HistogramChart chart = new HistogramChart(this.productType + " Total Queue Time Per Simulation", "simulation iteration", "queue time (min)");

        XYPlot plot3 = chart.getChart().getXYPlot();
        plot3.setRenderer(0, new SamplingXYLineRenderer());
        XYItemRenderer xyir3 = plot3.getRenderer(0);
        xyir3.setSeriesPaint(0, ChartColor.LIGHT_BLUE);
        chart.addSeries("Total Daily Queue time", this.totalQueueHourTimePerSimulation());

        NumberTickUnit tickUnit = new NumberTickUnit(1);

        chart.getDomainAxis().setTickUnit(tickUnit);
        chart.render();
    }

    public void plotAvgMinutelyQueueTimes() {
        HistogramChart chart = new HistogramChart(this.productType + " Queue times ( " + this.count() + " simulation days)", "hour", "queue time (min)");
        chart.addSeries(this.productType + " queue times", this.avgQueueTimesPerMinute());
        chart.getRangeAxis().setRange(0, 10);

        NumberTickUnit tickUnit = new NumberTickUnit(60) {
            @Override
            public String valueToString(double value) {
                return new DecimalFormat("##").format(value / 60) + "h";
            }
        };

        chart.getDomainAxis().setTickUnit(tickUnit);
        chart.getDomainAxis().setRange(0, 24 * 60);

        chart.render();
    }

    public void plotAvgHourlyArrivals() {
        HistogramChart chart = new HistogramChart(this.productType + " Arrivals ( " + this.count() + " simulation days)", "hour", "arrivals");
        chart.addSeries(this.productType + " arrivals", this.avgArrivalsPerHour());

        chart.getRangeAxis().setRange(0, 60);

        NumberTickUnit tickUnit = new NumberTickUnit(1) {
            @Override
            public String valueToString(double value) {
                return new DecimalFormat("##").format(value) + "h";
            }
        };

        chart.getDomainAxis().setTickUnit(tickUnit);

        chart.render();
    }

    public void plotAvgMinutelyArrivals() {
        HistogramChart chart = new HistogramChart(this.productType + " Arrivals ( " + this.count() + " simulation days)", "hour", "arrivals");
        chart.addSeries(this.productType + " arrivals", this.avgArrivalsPerMinute());

        NumberTickUnit tickUnit = new NumberTickUnit(60) {
            @Override
            public String valueToString(double value) {
                return new DecimalFormat("##").format(value / 60) + "h";
            }
        };

        chart.getDomainAxis().setTickUnit(tickUnit);

        chart.getDomainAxis().setRange(0, 24 * 60 -9);
        chart.render();
    }

    public void plotQueueTimeProbabilities(double startHour, double endHour) {
        HistogramChart chart = new HistogramChart(this.productType + " Queue times ( " + this.count() + " simulation days)", "minute", "Probability");

        double[] probabilities = new double[10];

        for (int i = 1; i <= 10; i++) {
            probabilities[i - 1] = this.avgProbabilityQueueTimeLessThan(((double) i) * 60.0, startHour * 3600, endHour * 3600);
        }

        chart.addSeries("Queue time probabilities", probabilities, +0.5);

        chart.getRangeAxis().setRange(0, 1.0);

        NumberTickUnit tickUnit = new NumberTickUnit(0.1) {
            @Override
            public String valueToString(double value) {
                return new DecimalFormat("##.00").format(value * 100) + " %";
            }
        };

        chart.getRangeAxis().setTickUnit(tickUnit);

        chart.render();
    }

    public void plotQueueTimeProbabilities() {
        this.plotQueueTimeProbabilities(0, 24);
    }

}
