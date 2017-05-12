package org.twobits.adagio.simpletools;

/**
 * Created by jeroen on 12-5-17.
 */
public class SimpleToolManager {

    public int rollDice(String content) throws IllegalArgumentException {
        String[] dice = content.split("\\+");

        if (dice.length < 1){
            throw new IllegalArgumentException("Invalid parameters. Example: 'xdy' where a,b,x and y are integers.");
        }

        int total = 0;
        for (int j = 0; j < dice.length; j++) {
            String[] parts = dice[j].split("d");

            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid parameters. Example: 'xdy+adb' where a,b,x and y are integers.");
            }

            for (int i = 0; i < Integer.parseInt(parts[0]); i++) {
                total += Math.random() * Integer.parseInt(parts[1]) + 1;
            }
        }

        return total;
    }

}
