package src.main.java;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;

public class OCRTest {
    private final static Logger log = Logger.getLogger(OCRTest.class.getName());
    private final static String LANG_OPTION = "-l";
    private final static String LANGUAGE = "eng";
    private final static String PSM_OPTION = "-psm";
    private final static String PAGE_SEG_MODE = "3"; // Fully automatic page segmentation, but no OSD (default).

    static void usage() {
        System.err.println("usage: java -jar OCRTest.jar <image file name>");
        System.err.println("");
        System.exit(-1);
    }

    public static void main(String[] args) {
        if (args.length == 0 || args.length < 1) {
            usage();
        }

        String imgFile = args[0];

        try {
            // Invokes Tesseract executable via command line.
            List<String> cmd = new ArrayList<String>();
            cmd.add("tesseract");
            cmd.add(imgFile);
            cmd.add("output");
            cmd.add(LANG_OPTION);
            cmd.add(LANGUAGE);
            cmd.add(PSM_OPTION);
            cmd.add(PAGE_SEG_MODE);

            ProcessBuilder pb = new ProcessBuilder();
            pb.command(cmd);
            log.info("cmd: " + cmd);

            // Process command line output.
            Process process = pb.start();
            IOThreadHandler outputHandler = new IOThreadHandler(process.getInputStream());
            outputHandler.start();
            process.waitFor();
            log.info(outputHandler.getOutput());

            // Process output file.
            String s;
            BufferedReader br = new BufferedReader(new FileReader("output.txt"));
			while ((s = br.readLine()) != null) {
				log.info(s);
            }
            br.close();
        }
        catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private static class IOThreadHandler extends Thread {
		private InputStream inputStream;
		private StringBuilder output = new StringBuilder();

		IOThreadHandler(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		public void run() {
			Scanner br = null;
			try {
				br = new Scanner(new InputStreamReader(inputStream));
				String line = null;
				while (br.hasNextLine()) {
					line = br.nextLine();
					output.append(line + System.getProperty("line.separator"));
				}
			}
            finally {
				br.close();
			}
		}

		public StringBuilder getOutput() {
			return output;
		}
	}
}
