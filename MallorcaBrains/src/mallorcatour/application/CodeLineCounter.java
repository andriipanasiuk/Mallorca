/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.application;

import java.io.BufferedReader;
import java.io.File;
import mallorcatour.util.Log;
import mallorcatour.util.ReaderUtils;

/**
 *
 * @author Andrew
 */
public class CodeLineCounter {

    public static int calculateCodeLines(String path) {
        int result = 0;
        File file = new File(path);
        if (file.isDirectory()) {
            File[] includedFiles = file.listFiles();
            for (File includedFile : includedFiles) {
                result += calculateCodeLines(includedFile.getAbsolutePath());
            }
        } else {
            if (file.getAbsolutePath().endsWith(".java")) {
                result += calculateCodeLinesInFile(file.getAbsolutePath());
            }
        }
        return result;
    }

    private static int calculateCodeLinesInFile(String path) {
        int result = 0;
        BufferedReader reader = ReaderUtils.initReader(path);
        while (ReaderUtils.readLine(reader) != null) {
            result++;
        }
        return result;
    }

    private static void deleteTempDirectory() {
        File dir = new File("temp");
        int count = dir.listFiles().length;
        for (File file : dir.listFiles()) {
            file.delete();
        }
        Log.d("There was deleted " + count + " files");
    }

    private static void calculateCodeLines() {
        Log.d("Lines of code: " + calculateCodeLines("C:\\Users\\Andrew\\Documents\\NetBeansProjects\\Utils"));
    }

    public static void main(String[] args) {
        deleteTempDirectory();
//        calculateCodeLines();
    }
}
