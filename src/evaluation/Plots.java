package evaluation;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import Logging.Log;
import evaluation.SamplingProcedureRecord.SamplingStepRecord;

public class Plots {
	public static void plotScore(SamplingProcedureRecord record) {
		Log.d("%s recorded steps", record.samplingSteps.size());
		XYSeriesCollection dataset = new XYSeriesCollection();

		XYSeries precisions = getPrecisions(record);
		XYSeries recalls = getRecalls(record);
		XYSeries scores = getObjectiveFunctionScores(record);
		XYSeries modelScores = getModelScores(record);

		dataset.addSeries(precisions);
		dataset.addSeries(recalls);
		dataset.addSeries(scores);
		dataset.addSeries(modelScores);

		JFreeChart chart = ChartFactory.createXYLineChart("Scores", "steps",
				"score", dataset);

		ChartPanel chartPanel = new ChartPanel(chart);
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setContentPane(chartPanel);
		frame.setSize(500, 300);
	}

	public static XYSeries getPrecisions(SamplingProcedureRecord record) {

		XYSeries series = new XYSeries("precision");

		for (int i = 0; i < record.samplingSteps.size(); i++) {
			SamplingStepRecord s = record.samplingSteps.get(i);
			series.add(i, s.acceptedState.objectiveFunctionScore.precision);
		}
		return series;
	}

	public static XYSeries getRecalls(SamplingProcedureRecord record) {
		XYSeries series = new XYSeries("recall");

		for (int i = 0; i < record.samplingSteps.size(); i++) {
			SamplingStepRecord s = record.samplingSteps.get(i);
			series.add(i, s.acceptedState.objectiveFunctionScore.recall);
		}
		return series;
	}

	public static XYSeries getObjectiveFunctionScores(
			SamplingProcedureRecord record) {
		XYSeries series = new XYSeries("score");

		for (int i = 0; i < record.samplingSteps.size(); i++) {
			SamplingStepRecord s = record.samplingSteps.get(i);
			series.add(i, s.acceptedState.objectiveFunctionScore.score);
		}
		return series;
	}

	public static XYSeries getModelScores(SamplingProcedureRecord record) {
		XYSeries series = new XYSeries("model score");

		for (int i = 0; i < record.samplingSteps.size(); i++) {
			SamplingStepRecord s = record.samplingSteps.get(i);
			series.add(i, s.acceptedState.modelScore);
		}
		return series;
	}
}
