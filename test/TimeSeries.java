package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TimeSeries {

	private List<String[]> rowList = new ArrayList<String[]>();

	public TimeSeries(String csvFileName) {
		try {
			String line = "";
			BufferedReader br = new BufferedReader(new FileReader(csvFileName));

			while ((line = br.readLine()) != null) {
				rowList.add(line.split(","));
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// timeStep starts from 1, as 0 will be the name of the data point
	public float getDataPoint(int timeStep, int columnIndex) {
		if (timeStep >= 0 && timeStep < this.rowList.size()) {
			if (this.rowList.get(timeStep).length > columnIndex && columnIndex >= 0) {
				return Float.parseFloat(this.rowList.get(timeStep + 1)[columnIndex]);
			}
		}
		return (float) -1;
	}

	public float[] getColumn(int index) {
		float[] column = new float[rowList.size()];
		if (this.rowList.get(0).length > index && index >= 0) {
			for (int i = 1; i < rowList.size(); i++) {
				column[i - 1] = Float.parseFloat(rowList.get(i)[index]);
			}
		}
		return column;
	}

	public int getNumOfRows() {
		return this.rowList.size() - 1;
	}

	public int getNumOfColumns() {
		return this.rowList.get(0).length;
	}

	public String getFeatureNameByIndex(int index) {
		return this.rowList.get(0)[index];
	}

	public int getFeatureIndexByName(String name) {
		for (int i = 0; i < rowList.get(0).length; i++) {
			if (rowList.get(0)[i].equals(name)) {
				return i;
			}
		}
		return -1;
	}
}
