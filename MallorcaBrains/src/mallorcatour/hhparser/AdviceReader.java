/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hhparser;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import mallorcatour.core.game.advice.Advice;
import mallorcatour.util.ReaderUtils;

/**
 *
 * @author Andrew
 */
public class AdviceReader {

    public static List<Advice> readAdvices(String filename) {
        List<Advice> result = new ArrayList<Advice>();
        BufferedReader reader = ReaderUtils.initReader(filename);
        String buffer = ReaderUtils.readLine(reader);
        while (buffer != null) {
            if (!buffer.isEmpty()) {
                result.add(Advice.valueOf(buffer));
            }
            buffer = ReaderUtils.readLine(reader);
        }
        return result;
    }
}
