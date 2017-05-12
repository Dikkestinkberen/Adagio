package org.twobits.adagio.simpletools;

import java.util.Random;

/**
 * Created by jeroen on 12-5-17.
 */
public class SimpleToolManager {

    public int rollDice(String content) throws IllegalArgumentException {
        String[] parts = content.split("d");

        if (parts.length != 2){
            throw new IllegalArgumentException("Invalid parameters. Please use 'xdy' where x and y are integers.");
        }

        int total = 0;
        for (int i = 0; i < Integer.parseInt(parts[0]); i++) {
            total += Math.random() * Integer.parseInt(parts[1]) + 1;
        }

        return total;
    }

}
