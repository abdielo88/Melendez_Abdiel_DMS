//Password to login is secured123
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.DecimalFormat;

class GameInfo /*This class contains all information of each game including the name, the type of console,
    the category of the game, its price, and the amount of copies. This will also include the format for the game's price.*/
{
    int id;
    String name;
    String type;
    String category;
    double price;
    int quantity;

    private static final DecimalFormat priceFormat = new DecimalFormat("0.00");

    public GameInfo(int id, String name, String type, String category, double price, int quantity)
    {
        this.id = id;
        this.name = name;
        this.type = type;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
    }

    @Override
    public String toString()
    {
        return id + ". " + name + ", " + type + ", " + category + ", $" + priceFormat.format(price) + ", " + quantity + " copies";
    }
}

public class Game4LifeDMS /*This class contains the main method and the different methods that will allow the user
 to achieve the main objectives of the system: upload the file with the list of games, display those games, add more,
 remove them, or make an order for more games. This class will also include the login method for accessing the system.*/
{

    private static final String loginPassword = "secured123"; //Password used to log in into the system.
    private static final List<String> gameTypes = Arrays.asList("Playstation", "Xbox", "Nintendo Switch"); //Makes sure that the user's input are either one of these options.
    private static final List<String> gameCategories = Arrays.asList("Action/Adventure", "Sci-Fi", "Horror/Thriller",
            "Puzzles", "Family-Friendly", "Sports"); //Makes sure that the user's input are either one of these options.
    private static Map<Integer, GameInfo> gameInventory = new HashMap<>();
    private static int nextId = 1;
    private static String filePath;

    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);

        if (!login(scanner))
        {
            System.out.println("Maximum login attempts exceeded; exiting the system.");
            return;
        }
        if (!uploadFile(scanner))
        {
            System.out.println("File upload failed; exiting the system.");
            return;
        }

        boolean exit = false;
        while (!exit)
        {
            System.out.println("What would you like to do today?");
            System.out.println("Type '1' to display games on list");
            System.out.println("Type '2' to add a new game to the list");
            System.out.println("Type '3' to remove/sell a game from the list");
            System.out.println("Type '4' to update games from the list");
            System.out.println("Type '5' to order more games");
            System.out.println("Type '6' to close the system");

            int choice = 0;
            boolean validInput = false;

            while (!validInput) //This is used to make sure that the user only puts numbers between 1-5 for the menu options.
            {
                try
                {
                    choice = Integer.parseInt(scanner.nextLine());
                    if (choice >= 1 && choice <= 6)
                    {
                        validInput = true;
                    } else
                    {
                        System.out.print("Invalid input; please input a number between 1 and 6 to either: ");
                        System.out.println("\n'1' to display games on list");
                        System.out.println("'2' to add a new game to the list");
                        System.out.println("'3' to remove/sell a game from the list");
                        System.out.println("'4' to update a game from the list");
                        System.out.println("'5' to order more games");
                        System.out.println("'6' to close the system");
                    }
                } catch (NumberFormatException e)
                {
                    System.out.print("Invalid input; please input a number between 1 and 6 to either: ");
                    System.out.println("\n'1' to display games on list");
                    System.out.println("'2' to add a new game to the list");
                    System.out.println("'3' to remove/sell a game from the list");
                    System.out.println("'4' to update a game from the list");
                    System.out.println("'5' to order more games");
                    System.out.println("'6' to close the system");
                }
            }

            switch (choice)
            {
                case 1: displayGames(); break;
                case 2: addGame(scanner); saveInventoryToFile(); break;
                case 3: removeGame(scanner); saveInventoryToFile(); break;
                case 4: updateGame(scanner); saveInventoryToFile(); break;
                case 5: orderGames(scanner); break;
                case 6: exit = true; break;
                default: System.out.println("Invalid number; please try again.");
            }
        }
        scanner.close();
    }

    private static boolean login(Scanner scanner) //This will add a security layer for accessing the system.
    {
        int attempts = 0;
        while (attempts < 3)
        {
            System.out.print("Welcome to the Game4Life Database System! Please enter the password to login (3 attempts max): ");
            String inputPassword = scanner.nextLine(); //Password is secured123
            if (loginPassword.equals(inputPassword))
            {
                return true;
            }
            System.out.println("Incorrect password; please try again.");
            attempts++;
        }
        return false;
    }

    private static boolean uploadFile(Scanner scanner) /*This will allow the system to read the data on the text file.
    The format in the text file is formatted as the following: Unique game ID, game's name, game's type,
     game's category, price of the game, and amount og copies. */
    {
        while (true)
        {
            System.out.print("Enter the path to the text file: ");
            filePath = scanner.nextLine();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
            {
                String line;
                int maxId = 0;
                while ((line = br.readLine()) != null)
                {
                    String[] parts = line.split(", ");
                    int id = Integer.parseInt(parts[0].substring(0, parts[0].indexOf(".")));
                    String name = parts[0].substring(parts[0].indexOf(".") + 2);
                    String type = parts[1];
                    String category = parts[2];
                    double price = Double.parseDouble(parts[3].substring(1));
                    int quantity = Integer.parseInt(parts[4].split(" ")[0]);
                    gameInventory.put(id, new GameInfo(id, name, type, category, price, quantity));
                    maxId = Math.max(maxId, id);
                }
                nextId = maxId + 1;
                return true;
            } catch (IOException e)
            {
                System.out.println("File not found or incorrect format; please try again.");
            } catch (NumberFormatException e)
            {
                System.out.println("Error in data format; please type the correct format for the file.");
            }
        }
    }

    private static void saveInventoryToFile() //This will save the changes made to the list.
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath)))
        {
            for (GameInfo game : gameInventory.values())
            {
                bw.write(game.toString());
                bw.newLine();
            }
        } catch (IOException e)
        {
            System.out.println("Error saving to file: " + e.getMessage());
        }
    }

    private static void displayGames() /*This will allow the system to display the games available on the list.
    If there aren't any, it will let the user know. */
    {
        if (gameInventory.isEmpty())
        {
            System.out.println("The aren't any games in the list.");
            System.out.println("\n");
        } else
        {
            for (GameInfo game : gameInventory.values())
            {
                System.out.println(game);
            }
            System.out.println("\n");
        }
    }

    private static void addGame(Scanner scanner) /*The system will ask the user for the game's information in order for it
    to be added to the list. */
    {
        System.out.print("Enter the name of the game: ");
        String name = scanner.nextLine();
        String type = getValidInput(scanner, "For what type of console? (Playstation, Xbox, Nintendo Switch) ", gameTypes);
        String category = getValidInput(scanner, "What's the game category? (Action/Adventure, Sci-Fi, Horror/Thriller, " +
                "Puzzles, Family-Friendly, Sports) ", gameCategories);
        double price = getDoubleInput(scanner, "Enter the price of the game (in 0.00 format; no need to put the '$' symbol): ");
        int quantity = getIntInput(scanner, "How many copies of the game? ");
        scanner.nextLine();

        GameInfo newGame = new GameInfo(nextId++, name, type, category, price, quantity);
        gameInventory.put(newGame.id, newGame);
        System.out.println("The game has been added to the list successfully.");
        System.out.println("\n");
    }

    private static void removeGame(Scanner scanner) /*If a game is being removed or sold, the system will ask only
    for the name of the game and the amount. It will let the user know if the amount goes over the ones available
    in the list. */
    {
        String name = getValidGameName(scanner, "Enter the name of the game you want to remove/sell: ");
        int quantitySold = getIntInput(scanner, "Enter the amount of copies being removed/sold: ");

        boolean gameFound = false;

        for (GameInfo game : gameInventory.values())
        {
            if (game.name.equalsIgnoreCase(name))
            {
                gameFound = true;
                if (game.quantity < quantitySold)
                {
                    System.out.println("Error: Not enough copies in inventory; please try again.");
                } else
                {
                    game.quantity -= quantitySold;
                    System.out.println("The game has been removed/sold successfully.");
                    System.out.println("\n");
                    if (game.quantity == 0)
                    {
                        gameInventory.remove(game.id);
                    }
                    reassignIds(); //Read the comment of the last method, reassignIds().
                }
                break;
            }
        }

        if (!gameFound)
        {
            System.out.println("The game does not exist in the list.");
        }
    }

    private static void updateGame(Scanner scanner) /*This will allow the user to update any information
    of any game that is on the list. It will ask for the ID of the game that needs to be updated, and the user will
    be able to edit all of its information (if the information like the name of the game stays the same, they can
    just type it in again). */
    {
        int id;
        while (true)
        {
            System.out.print("Enter the ID of the game you want to update: ");
            id = getIntInput(scanner, "");

            if (gameInventory.containsKey(id))
            {break;}
            else
            {
                System.out.println("Error: Game with ID " + id + " not found; please try again.");
            }
        }

        GameInfo game = gameInventory.get(id);

        System.out.print("Enter the new name of the game, if any (current: " + game.name + "): ");
        String newName = scanner.nextLine();
        String newType = getValidInput(scanner, "For what type of console? " +
                "(Playstation, Xbox, Nintendo Switch) (current: " + game.type + "): ", gameTypes);
        String newCategory = getValidInput(scanner, "What's the game category? (Action/Adventure, Sci-Fi, " +
                "Horror/Thriller, Puzzles, Family-Friendly, Sports) (current: " + game.category + "): ", gameCategories);
        double newPrice = getDoubleInput(scanner, "Enter the new price of the game, if any " +
                "(in 0.00 format; no need to put the '$' symbol) (current: $" + game.price + "): ");
        int newQuantity = getIntInput(scanner, "Enter the new amount of " +
                "copies of the game, if any (current: " + game.quantity + "): ");

        game.name = newName;
        game.type = newType;
        game.category = newCategory;
        game.price = newPrice;
        game.quantity = newQuantity;

        System.out.println("Game information has been updated successfully.");
        System.out.println("\n");
    }

    private static void orderGames(Scanner scanner) /*The orders will require the game's information in order for it to
    be sent to the headquarters. The last section of this method will allow the user to create or update their txt file
    with all the orders they have made so far; all the orders will also include a date and time info attached to them.*/
    {
        DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        System.out.print("Enter the name of the game you want to order: ");
        String name = scanner.nextLine();
        String type = getValidInput(scanner, "What is the console for this game? (Playstation, Xbox, Nintendo Switch) ", gameTypes);
        String category = getValidInput(scanner, "What is the category of the game? (Action/Adventure, Sci-Fi, Horror/Thriller, " +
                "Puzzles, Family-Friendly, Sports) ", gameCategories);
        double price = getDoubleInput(scanner, "Enter the price of the game (in 0.00 format; no need to put the '$' symbol): ");
        int quantity = getIntInput(scanner, "How many copies are needed? ");
        System.out.print("Enter the name for the order file (without extension): ");
        String fileName = scanner.nextLine().trim() + ".txt";

        File file = new File(fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true)))
        {
            if (!file.exists())
            {
                file.createNewFile();
            }
            writer.write("Date Order: " + now.format(dateTime));
            writer.newLine();
            writer.write(name + ", " + type + ", " + category + ", $" + String.format("%.2f", price) + ", "
                    + quantity + " copies");
            writer.newLine();
            System.out.println("Order complete; expect the delivery between 1-2 weeks. The order has been recorded in " + fileName + "\n");
        } catch (IOException e)
        {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }

    private static String getValidInput(Scanner scanner, String prompt, List<String> validOptions) /*This will make
    sure that the user picks one of the options mentioned earlier. */
    {
        while (true)
        {
            System.out.print(prompt);
            String input = scanner.nextLine();
            if (validOptions.contains(input))
            {
                return input;
            }
            System.out.println("Invalid input; please type a correct option");
        }
    }

    private static double getDoubleInput(Scanner scanner, String prompt) /*This method guarantees that the user inputs
    the price in the correct format*/
    {
        double value = -1.0;
        boolean validInput = false;

        while (!validInput)
        {
            System.out.print(prompt);
            try
            {
                value = Double.parseDouble(scanner.nextLine());
                if (value >= 0.0)
                {
                    validInput = true;
                } else
                {
                    System.out.println("Invalid input; please type a valid number.");
                }
            } catch (NumberFormatException e)
            {
                System.out.println("Invalid input; please type a valid number.");
            }
        }

        return value;
    }

    private static int getIntInput(Scanner scanner, String prompt) /*This method guarantees that the user inputs
    the amount of copies to be added/removed/ordered in the correct format*/
    {
        int value = -1;
        boolean validInput = false;

        while (!validInput)
        {
            System.out.print(prompt);
            try
            {
                value = Integer.parseInt(scanner.nextLine());
                if (value >= 0)
                {
                    validInput = true;
                } else
                {
                    System.out.println("Invalid input; please type a valid number.");
                }
            } catch (NumberFormatException e)
            {
                System.out.println("Invalid input; please type a valid number.");
            }
        }

        return value;
    }

    private static String getValidGameName(Scanner scanner, String prompt) /*This method guarantees that the user inputs
    a name for a game */
    {
        while (true)
        {
            System.out.print(prompt);
            String input = scanner.nextLine();
            for (GameInfo game : gameInventory.values())
            {
                if (game.name.equalsIgnoreCase(input))
                {
                    return input;
                }
            }
            System.out.println("The game is not in the list; please try again.");
        }
    }

    private static void reassignIds() /*This will guarantee that when a game is removed/sold,
    the list will update the number ID, so it stays in sequence. For example, if I have 25 items and I remove
    item #23, instead of being (21,22,24,25), it will update to (21,22,23,24). */
    {
        int newId = 1;
        Map<Integer, GameInfo> newGameInventory = new LinkedHashMap<>();

        for (GameInfo game : new ArrayList<>(gameInventory.values()))
        {
            game.id = newId;
            newGameInventory.put(newId, game);
            newId++;
        }

        gameInventory = newGameInventory;
        nextId = newId;
    }
}
