package test;

import java.util.ArrayList;
import java.util.List;

public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {

	private List<CorrelatedFeatures> normalModel = new ArrayList<CorrelatedFeatures>();

	@Override
	public void learnNormal(TimeSeries ts) {
		float minCorrelation = (float) 0.9;
		for (int i = 0; i < ts.getNumOfColumns() - 1; i++) {
			float maxCorrelation = 0;
			int maxCorrelationIndex = -1;
			for (int j = i + 1; j < ts.getNumOfColumns(); j++) {
				float currCorrelation = StatLib.pearson(ts.getColumn(i), ts.getColumn(j));
				if (currCorrelation > maxCorrelation) {
					maxCorrelation = currCorrelation;
					maxCorrelationIndex = j;
				}
			}
			if (maxCorrelationIndex != -1 && maxCorrelation > minCorrelation) {
				// a better way would be to just have a linear_reg function that accepts 2
				// arrays of floats as arguments as it already parses the points array to such 2
				// float arrays. this creates double work here, as we have to create the points
				// array so the linear_reg function could make it 2 float arrays again
				// we keep it this way because thats how StatLib works and it should be closed
				// to changes
				Point[] points = new Point[ts.getNumOfRows()];
				for (int j = 0; j < ts.getNumOfRows(); j++) {
					points[j] = new Point(ts.getDataPoint(j, i), ts.getDataPoint(j, maxCorrelationIndex));
				}
				// finding the line equation using linear regression
				Line lin_reg = StatLib.linear_reg(points);

				// finding the farthest away point from the line equation
				float maxDistance = 0;
				for (Point point : points) {
					float currDistance = StatLib.dev(point, lin_reg);
					if (currDistance > maxDistance)
						maxDistance = currDistance;
				}
				maxDistance = maxDistance * (float) 1.1;
				this.normalModel.add(new CorrelatedFeatures(ts.getFeatureNameByIndex(i),
						ts.getFeatureNameByIndex(maxCorrelationIndex), maxCorrelation, lin_reg, maxDistance));
			}
		}
	}

	@Override
	public List<AnomalyReport> detect(TimeSeries ts) {
		List<AnomalyReport> reportList = new ArrayList<AnomalyReport>();
		for (CorrelatedFeatures correlatedFeatures : normalModel) {
			for (int i = 0; i < ts.getNumOfRows(); i++) {
				float distance = StatLib.dev(
						new Point(ts.getDataPoint(i, ts.getFeatureIndexByName(correlatedFeatures.feature1)),
								ts.getDataPoint(i, ts.getFeatureIndexByName(correlatedFeatures.feature2))),
						correlatedFeatures.lin_reg);
				if (distance > correlatedFeatures.threshold) {
					reportList
							.add(new AnomalyReport(correlatedFeatures.feature1 + "-" + correlatedFeatures.feature2,
									i + 1));
				}
			}
		}
		return reportList;
	}

	public List<CorrelatedFeatures> getNormalModel() {
		return this.normalModel;
	}
}
