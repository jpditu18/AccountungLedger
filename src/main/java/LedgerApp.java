
import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class LedgerApp {
    private static final String fileName = "transaction.csv";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        showHomeScreen();
    }

    private static void showHomeScreen() {
        while (true) {
            System.out.println("\nWelcome! What would you like to do today?");
            System.out.println("\n=== Home Screen ===");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) View Ledger");
            System.out.println("X) Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "D":
                    recordTransaction(true);
                    break;
                case "P":
                    recordTransaction(false);
                    break;
                case "L":
                    showLedgerScreen();
                    break;
                case "X":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid input. Try again.");
            }
        }
    }

    private static void recordTransaction(boolean isDeposit) {
        System.out.print("Date (YYYY-MM-DD): ");
        String date = scanner.nextLine().trim();

        System.out.print("Time (HH:MM:SS): ");
        String time = scanner.nextLine().trim();

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Vendor: ");
        String vendor = scanner.nextLine().trim();

        System.out.print("Amount: ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        if (!isDeposit) amount = -amount;

        Transaction transaction = new Transaction(date, time, description, vendor, amount);
        writeTransactionToFile(transaction);
        System.out.println("Transaction recorded.");
    }

    private static void showLedgerScreen() {
        List<Transaction> allTransactions = loadTransactions();
        allTransactions.sort((t1, t2) -> (t2.getDate() + t2.getTime()).compareTo(t1.getDate() + t1.getTime()));

        while (true) {
            System.out.println("\n=== Ledger Menu ===");
            System.out.println("A) All Transactions");
            System.out.println("D) Deposits Only");
            System.out.println("P) Payments Only");
            System.out.println("R) Reports");
            System.out.println("H) Home");
            System.out.print("Choose an option: ");
            String input = scanner.nextLine().trim().toUpperCase();

            switch (input) {
                case "A":
                    printTransactions(allTransactions);
                    break;
                case "D":
                    printTransactions(filterTransactions(allTransactions, true));
                    break;
                case "P":
                    printTransactions(filterTransactions(allTransactions, false));
                    break;
                case "R":
                    showReportsMenu(allTransactions);
                    break;
                case "H":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static List<Transaction> filterTransactions(List<Transaction> transactions, boolean deposits) {
        return transactions.stream()
                .filter(t -> deposits ? t.getAmount() > 0 : t.getAmount() < 0)
                .collect(Collectors.toList());
    }

    private static void showReportsMenu(List<Transaction> transactions) {
        while (true) {
            System.out.println("\n=== Reports Menu ===");
            System.out.println("1) Month to Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year to Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("0) Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            List<Transaction> filtered;
            switch (choice) {
                case "1":
                    filtered = filterByMonth(transactions, true);
                    printTransactions(filtered);
                    break;
                case "2":
                    filtered = filterByMonth(transactions, false);
                    printTransactions(filtered);
                    break;
                case "3":
                    filtered = filterByYear(transactions, true);
                    printTransactions(filtered);
                    break;
                case "4":
                    filtered = filterByYear(transactions, false);
                    printTransactions(filtered);
                    break;
                case "5":
                    System.out.print("Enter vendor keyword: ");
                    String keyword = scanner.nextLine().trim().toLowerCase();
                    filtered = transactions.stream()
                            .filter(t -> t.getVendor().toLowerCase().contains(keyword))
                            .collect(Collectors.toList());
                    printTransactions(filtered);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid selection.");
            }
        }
    }

    private static List<Transaction> filterByMonth(List<Transaction> list, boolean currentMonth) {
        YearMonth targetMonth = currentMonth ? YearMonth.now() : YearMonth.now().minusMonths(1);
        return list.stream()
                .filter(t -> YearMonth.from(LocalDate.parse(t.getDate())).equals(targetMonth))
                .collect(Collectors.toList());
    }

    private static List<Transaction> filterByYear(List<Transaction> list, boolean currentYear) {
        int year = currentYear ? LocalDate.now().getYear() : LocalDate.now().getYear() - 1;
        return list.stream()
                .filter(t -> LocalDate.parse(t.getDate()).getYear() == year)
                .collect(Collectors.toList());
    }

    private static void printTransactions(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            for (Transaction t : transactions) {
                System.out.println(t);
            }
        }
    }

    private static void writeTransactionToFile(Transaction transaction) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(transaction.toCSV());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving transaction: " + e.getMessage());
        }
    }

    private static List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\|");
                if (fields.length == 5) {
                    Transaction t = new Transaction(fields[0], fields[1], fields[2], fields[3], Double.parseDouble(fields[4]));
                    transactions.add(t);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading transactions: " + e.getMessage());
        }
        return transactions;
    }
}


