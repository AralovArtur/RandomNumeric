import org.apache.commons.cli.*;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Creates a file with random numbers and then parses those numbers
 *
 * @author Artur Aralov
 */
public class RandomNumeric {
    private static int primeCounter;
    private static int armstrongCounter;
    private static BigInteger[] mostFrequentNumbers;

    /**
     * Creates a file with random numbers and parses those numbers
     *
     * @param args specifies file name and file size (optional)
     */
    public static void main(String args[]) {
        String fileName = args[0];
        int fileSize = getFileSize(args);

        writeFile(fileName, fileSize);

        long startReading = System.currentTimeMillis();
        List<BigInteger> randomNumbers = readFile(fileName);
        long endReading = System.currentTimeMillis();

        long startAnalyzing = System.currentTimeMillis();
        analyzeFile(randomNumbers);
        long endAnalyzing = System.currentTimeMillis();

        System.out.println("10 most frequently appeared numbers in bar chart form:");
        for (int i = 9, j = 0; i > -1; i--, j++) {
            char[] frequencyArray = new char[i + 1];
            Arrays.fill(frequencyArray, '*');
            String stringNumber = mostFrequentNumbers[j].toString();
            System.out.println(stringNumber + (stringNumber.length() == 1 ? "  ": " ") + new String(frequencyArray));
        }

        System.out.println("\n" + "The count of Prime numbers:" + "\n" + primeCounter);
        System.out.println("\n" + "The count of Armstrong numbers:" + "\n" + armstrongCounter);
        System.out.println("\n" + "Time taken to read the file:" + "\n" + ((endReading - startReading) + " milliseconds"));
        System.out.println("\n" + "Time taken to analyze the file:" + "\n" + ((endAnalyzing - startAnalyzing) + " milliseconds"));
    }

    /**
     * Gets file size
     *
     * @param args specifies file name and file size (optional)
     * @return selected file size
     */
    private static int getFileSize(String[] args) {
        Options options = new Options();

        Option fileSizeOption = new Option("s", "size", true, "File size specified in MB");
        fileSizeOption.setRequired(false);
        options.addOption(fileSizeOption);

        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = commandLineParser.parse(options, args);
        } catch (ParseException exception) {
            System.out.println(exception.getMessage());
            helpFormatter.printHelp("utility-name", options);
            System.exit(1);
        }

        String size = cmd.getOptionValue("size");
        return size != null? 1024 * 1024 * Integer.parseInt(size): 1024 * 1024 * 64;
    }

    /**
     * Writes random numbers to file
     *
     * @param fileName specifies file name
     * @param fileSize specifies file size
     */
    private static void writeFile(String fileName, int fileSize) {

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName))) {
            int byteCounter = 0;

            while (byteCounter < fileSize) {
                BigInteger bigInteger = getRandomBigInteger();
                String stringBigInteger = bigInteger.toString() + " ";
                bufferedWriter.write(stringBigInteger);
                byteCounter += stringBigInteger.getBytes("UTF-8").length;
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Gets random number (range from 1 to 2^64 - 1)
     *
     * @return random number
     */
    private static BigInteger getRandomBigInteger() {
        BigInteger bigInteger;
        BigInteger zero = new BigInteger("0");

        do {
            bigInteger = new BigInteger(ThreadLocalRandom.current().nextInt(65), ThreadLocalRandom.current());
        } while (bigInteger.equals(zero));

        return bigInteger;
    }

    /**
     * Reads the selected file and returns the numbers read
     *
     * @param fileName specifies file name
     * @return list of numbers
     */
    private static List<BigInteger> readFile(String fileName) {
        List<BigInteger> randomNumbers = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {

            bufferedReader.lines().parallel().forEach(line -> {
                String[] stringNumbers = line.split(" ");
                for (String stringNumber: stringNumbers) {
                    BigInteger bigInteger = new BigInteger(stringNumber);
                    randomNumbers.add(bigInteger);
                }
            });

        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return randomNumbers;
    }

    /**
     * Analyzes how many primes and Armstrong numbers are in a list
     *
     * @param randomNumbers specifies a list of random numbers
     */
    public static void analyzeFile(List<BigInteger> randomNumbers) {

        randomNumbers.parallelStream().forEach(number -> {
            if (number.isProbablePrime(1))
                primeCounter += 1;
            if (isArmstrong(number)) {
                armstrongCounter += 1;
            }
        });

        getMostFrequentNumbers(randomNumbers);
    }

    /**
     * Checks whether a number is a prime number
     *
     * @param number specifies a random number
     * @return true if a number is a prime number, false otherwise
     */
    private static boolean isArmstrong(BigInteger number) {
        BigInteger remainder;
        BigInteger result = new BigInteger("0");
        BigInteger ten = new BigInteger("10");

        int digitCounter = 0;

        for (BigInteger originalNumber = number; !originalNumber.equals(new BigInteger("0")); originalNumber = originalNumber.divide(ten)) {
            digitCounter++;
        }

        for (BigInteger originalNumber = number; !originalNumber.equals(new BigInteger("0")); originalNumber = originalNumber.divide(ten)) {
            remainder = originalNumber.remainder(ten);
            result = result.add(remainder.pow(digitCounter));
        }

        return result.equals(number);
    }

    /**
     * Gets 10 most frequently appeared numbers in a file
     *
     * @param randomNumbers specifies a list of random numbers
     */
    private static void getMostFrequentNumbers(List<BigInteger> randomNumbers) {
        Map<BigInteger, Long> numbersFrequency = randomNumbers.parallelStream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        //Sort a map and add it to sortedNumbersFrequency
        Map<BigInteger, Long> sortedNumbersFrequency = new LinkedHashMap<>();
        numbersFrequency.entrySet().parallelStream().sorted(Map.Entry.<BigInteger, Long> comparingByValue()
                .reversed()).forEachOrdered(entry -> sortedNumbersFrequency.put(entry.getKey(), entry.getValue()));

        Iterator<Map.Entry<BigInteger, Long>> iterator = sortedNumbersFrequency.entrySet().iterator();
        BigInteger[] mostFrequentNumbers = new BigInteger[10];

        for (int i = 0; i < 10; i++) {
            Map.Entry<BigInteger, Long> entry = iterator.next();
            mostFrequentNumbers[i] = entry.getKey();
        }

        RandomNumeric.mostFrequentNumbers = mostFrequentNumbers;
    }
}