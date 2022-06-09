package test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Commands {

	// Default IO interface
	public interface DefaultIO {
		public String readText();

		public void write(String text);

		public float readVal();

		public void write(float val);

		public default int readToNewCsvUntilDone(String fileName) {
			try {
				int numOfLines = 0;
				PrintWriter out = new PrintWriter(new FileWriter(fileName));
				String line;
				while (!(line = this.readText()).equals("done")) {
					out.println(line);
					numOfLines++;
				}
				out.close();
				return numOfLines;
			} catch (IOException e) {
				e.printStackTrace();
				return 0;
			}
		}

		// you may add default methods here
	}

	// the default IO to be used in all commands
	DefaultIO dio;

	public Commands(DefaultIO dio) {
		this.dio = dio;
	}

	private class fullAnomalyReport {
		public final String description;
		public final long timeStepStart;
		public long timeStepEnd;

		public fullAnomalyReport(String description, long timeStepStart, long timeStepEnd) {
			this.description = description;
			this.timeStepStart = timeStepStart;
			this.timeStepEnd = timeStepEnd;
		}
	}

	// the shared state of all commands
	private class SharedState {
		SimpleAnomalyDetector anomalyDetector = new SimpleAnomalyDetector();
		List<AnomalyReport> reportList = new ArrayList<AnomalyReport>();
		ArrayList<fullAnomalyReport> fullReportList = new ArrayList<fullAnomalyReport>();
		int numOfTimeStepsInTestFile;
	}

	private SharedState sharedState = new SharedState();

	// Command abstract class
	public abstract class Command {
		protected String description;

		public Command(String description) {
			this.description = description;
		}

		public abstract void execute();
	}

	// Commands

	public class UploadFileCommand extends Command {

		public UploadFileCommand() {
			super("upload a time series csv file");
		}

		@Override
		public void execute() {
			dio.write("Please upload your local train CSV file.\n");
			dio.readText(); // reads an empty line for some reason
			dio.readToNewCsvUntilDone("anomalyTrain.csv");
			dio.write("Upload complete.\n");
			dio.write("Please upload your local test CSV file.\n");
			sharedState.numOfTimeStepsInTestFile = dio.readToNewCsvUntilDone("anomalyTest.csv");
			dio.write("Upload complete.\n");
		}
	}

	public class AlgoSettingsCommand extends Command {

		public AlgoSettingsCommand() {
			super("algorithm settings");
		}

		@Override
		public void execute() {
			dio.write("The current correlation threshold is " + sharedState.anomalyDetector.getMinCorrelation() + "\n");
			int validNewThreshold = 0;
			while (validNewThreshold == 0) {
				dio.write("Type a new threshold\n");
				float newThreshold = dio.readVal();
				if (newThreshold > 0 && newThreshold < 1) {
					sharedState.anomalyDetector.setMinCorrelation(newThreshold);
					validNewThreshold = 1;
				} else {
					dio.write("please choose a value between 0 and 1\n");
				}
			}
		}
	}

	public class DetectCommand extends Command {

		public DetectCommand() {
			super("detect anomalies");
		}

		@Override
		public void execute() {
			TimeSeries tsTrain = new TimeSeries("anomalyTrain.csv");
			sharedState.anomalyDetector.learnNormal(tsTrain);
			TimeSeries tsTest = new TimeSeries("anomalyTest.csv");
			sharedState.reportList = sharedState.anomalyDetector.detect(tsTest);
			dio.write("anomaly detection complete.\n");
		}
	}

	public class DisplayResultsCommand extends Command {

		public DisplayResultsCommand() {
			super("display results");
		}

		@Override
		public void execute() {
			for (AnomalyReport report : sharedState.reportList) {
				dio.write(new DecimalFormat("#").format(report.timeStep));
				dio.write("\t");
				dio.write(report.description);
				dio.write("\n");
			}
			dio.write("Done.\n");
		}
	}

	public class UploadAnomaliesAndAnalyzeCommand extends Command {

		public UploadAnomaliesAndAnalyzeCommand() {
			super("upload anomalies and analyze results");
		}

		public void mergeAnomalyReports() {
			sharedState.fullReportList.clear();
			for (AnomalyReport report : sharedState.reportList) {
				// if full report list isnt empty && if the description of the last full report
				// matches the description of the next report
				// and the timestep of the report is right after the end of the last full report
				// then extand the last full report
				if (sharedState.fullReportList.size() != 0 && report.description
						.equals(sharedState.fullReportList
								.get(sharedState.fullReportList.size() - 1).description)
						&& report.timeStep == sharedState.fullReportList
								.get(sharedState.fullReportList.size() - 1).timeStepEnd + 1) {
					sharedState.fullReportList
							.get(sharedState.fullReportList.size() - 1).timeStepEnd = report.timeStep;
				} else {
					sharedState.fullReportList
							.add(new fullAnomalyReport(report.description, report.timeStep, report.timeStep));
				}
			}
		}

		@Override
		public void execute() {
			dio.write("Please upload your local anomalies file.\n");
			ArrayList<fullAnomalyReport> realAnomalies = new ArrayList<fullAnomalyReport>();
			String line = dio.readText(); // reading "" for some reason
			// used to calculate N (the number of timesteps in the original csv that didnt
			// have anomalies)
			float numOfTimeStepsWithAnomaly = 0;
			while (!(line = dio.readText()).equals("done")) {
				String[] realAnomaly = line.split(",");
				fullAnomalyReport newRealAnomalyReport = new fullAnomalyReport("real", Long.parseLong(realAnomaly[0]),
						Long.parseLong(realAnomaly[1]));
				numOfTimeStepsWithAnomaly += newRealAnomalyReport.timeStepEnd - newRealAnomalyReport.timeStepStart + 1;
				realAnomalies.add(newRealAnomalyReport);
			}
			dio.write("Upload complete.\n");
			// dio.write("Analyzing...\n");
			this.mergeAnomalyReports();
			float P = realAnomalies.size();
			float N = sharedState.numOfTimeStepsInTestFile - numOfTimeStepsWithAnomaly;
			float TP = 0;
			float FP = 0;
			for (fullAnomalyReport fullAnomalyReport : sharedState.fullReportList) {
				int isTP = 0;
				for (fullAnomalyReport trueAnomalyReport : realAnomalies) {
					// if start of anomaly is within a true anomaly range
					// or if end of anomaly is within a true anomaly range
					// or if true anomaly range is within the anomaly range
					if ((fullAnomalyReport.timeStepStart >= trueAnomalyReport.timeStepStart
							&& fullAnomalyReport.timeStepStart <= trueAnomalyReport.timeStepEnd)
							|| (fullAnomalyReport.timeStepEnd >= trueAnomalyReport.timeStepStart
									&& fullAnomalyReport.timeStepEnd <= trueAnomalyReport.timeStepEnd)
							|| (fullAnomalyReport.timeStepStart <= trueAnomalyReport.timeStepStart
									&& fullAnomalyReport.timeStepEnd >= trueAnomalyReport.timeStepEnd)) {
						isTP = 1;
						break;
					}
				}
				if (isTP == 1) {
					TP++;
				} else {
					FP++;
				}
			}
			DecimalFormat df = new DecimalFormat("0.0##");
			df.setRoundingMode(RoundingMode.DOWN);
			dio.write("True Positive Rate: " + df.format(TP / P) + "\n");
			dio.write("False Positive Rate: " + df.format(FP / N) + "\n");
		}
	}

	public class ExitCommand extends Command {

		public ExitCommand() {
			super("exit");
		}

		@Override
		public void execute() {
		}
	}

}
