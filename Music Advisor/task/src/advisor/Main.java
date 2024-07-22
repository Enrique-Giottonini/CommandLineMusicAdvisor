package advisor;

import advisor.controller.SessionController;
import advisor.view.CommandLineView;

import java.util.HashMap;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        SessionController application = new SessionController(new Scanner(System.in), new CommandLineView());
        if (args.length != 0) application.setCustomParamsFromArgs(args);
        application.run();
    }
}
