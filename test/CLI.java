package test;

import java.util.ArrayList;

import test.Commands.Command;
import test.Commands.DefaultIO;

public class CLI {

	ArrayList<Command> commands;
	DefaultIO dio;
	Commands c;

	public CLI(DefaultIO dio) {
		this.dio = dio;
		c = new Commands(dio);
		commands = new ArrayList<Command>();
		commands.add(c.new UploadFileCommand());
		commands.add(c.new AlgoSettingsCommand());
		commands.add(c.new DetectCommand());
		commands.add(c.new DisplayResultsCommand());
		commands.add(c.new UploadAnomaliesAndAnalyzeCommand());
		commands.add(c.new ExitCommand());
	}

	private void printWelcomeText() {
		this.dio.write("Welcome to the Anomaly Detection Server.\n");
		this.dio.write("Please choose an option:\n");
		for (int i = 0; i < commands.size(); i++) {
			this.dio.write(i + 1 + ". " + commands.get(i).description + "\n");
		}
	}

	public void start() {
		int exit = 0;
		while (exit == 0) {
			this.printWelcomeText();
			float commandNum = this.dio.readVal();
			// can check if commandNum is valid
			if (commandNum == commands.size()) {
				exit = 1;
			} else {
				commands.get((int) commandNum - 1).execute();
			}
		}

	}
}
