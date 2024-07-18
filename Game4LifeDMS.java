//Password to login is secured123

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;


/**
 * Game4LifeDMS contains the core of the program.
 * It has the methods for connecting to the database and accessing the home page,
 * which contains the 5 main methods for interacting with the data: displaying, adding, removing,
 * updating, and ordering games.
 */
public class Game4LifeDMS
{
    private static final List<String> gameTypes = Arrays.asList("Playstation", "Xbox", "Nintendo Switch");
    private static final List<String> gameCategories = Arrays.asList("Action/Adventure", "Sci-Fi", "Horror/Thriller", "Puzzles", "Family-Friendly", "Sports");
    private static Map<Integer, GameInfo> gameInventory = new HashMap<>();
    private static int nextId = 1;
    private static JFrame frame;
    private static JTextArea outputArea;
    public static String DATABASE_NAME;

    /**
     * This the main () that runs the program with the GUI components.
     *
     * @param args stores the incoming command line arguments for the program.
     */
    public static void main(String[] args) //This runs the program with the GUI components
    {
        SwingUtilities.invokeLater(Game4LifeDMS::loginGUI);
    }

    /**
     * This method adds a layer of security for the program access by asking for a password.
     * After inputting the correct password (secured123), the file chooser tool will open and connect to the database
     * file that it has been chosen.
     */
    private static void loginGUI()
    {
        int attempts = 0;
        while (attempts < 3)
        {
            String inputPassword = JOptionPane.showInputDialog(null, "Welcome to the Game4Life Database System!" +
                    "\nPlease enter the password to login (3 attempts max):", "Login", JOptionPane.QUESTION_MESSAGE);
            if ("secured123".equals(inputPassword))
            {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION)
                {
                    File selectedFile = fileChooser.getSelectedFile();
                    DATABASE_NAME = selectedFile.getPath();
                    DBHelper.connect();
                    loadDataFromDatabase();
                    createMainGUI();
                    outputArea.append("Welcome to the Game4Life DMS! Pick from the following choices:\n\n");
                    return;
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "File upload failed; exiting the system.");
                    System.exit(0);
                }
            }
            JOptionPane.showMessageDialog(null, "Incorrect password; please try again.");
            attempts++;
        }
        JOptionPane.showMessageDialog(null, "Maximum login attempts exceeded; the program will close now...");
        System.exit(0);
    }

    /**
     * Method used for loading the data from the database to the program.
     */
    private static void loadDataFromDatabase()
    {
        Game4Life dbHelper = new Game4Life();
        List<ArrayList<Object>> gamesList = dbHelper.select(null, null, null, null, null);

        for (ArrayList<Object> gameData : gamesList)
        {
            int id = (int) gameData.get(0);
            String name = (String) gameData.get(1);
            String type = (String) gameData.get(2);
            String category = (String) gameData.get(3);
            double price = (double) gameData.get(4);
            int quantity = (int) gameData.get(5);

            GameInfo game = new GameInfo(id, name, type, category, price, quantity);
            gameInventory.put(id, game);
        }

        nextId = gamesList.stream().mapToInt(gameData -> (int) gameData.getFirst()).max().orElse(0) + 1;
    }

    /**
     * This the GUI for the main menu.
     * It contains the 6 main functions for displaying games, adding or removing them, updating them, ordering
     * more games, or exiting the program.
     */
    private static void createMainGUI()
    {
        frame = new JFrame("Game4Life Database Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Click one of the following options: ", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 2, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton displayButton = new JButton("Display Games");
        displayButton.addActionListener(e -> displayGames());
        buttonPanel.add(displayButton);

        JButton addButton = new JButton("Add Game");
        addButton.addActionListener(e -> addGame());
        buttonPanel.add(addButton);

        JButton removeButton = new JButton("Remove Game");
        removeButton.addActionListener(e -> removeGame());
        buttonPanel.add(removeButton);

        JButton updateButton = new JButton("Update Game");
        updateButton.addActionListener(e -> updateGame());
        buttonPanel.add(updateButton);

        JButton orderButton = new JButton("Order Game");
        orderButton.addActionListener(e -> orderGames());
        buttonPanel.add(orderButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e ->
        {
            System.exit(0);
        });
        buttonPanel.add(exitButton);

        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Method used for displaying the games' information available at the database
     */
    private static void displayGames()
    {
        DBHelper dbHelper = new DBHelper();
        String query = "SELECT * FROM Game4Life;";

        try
        {
            ArrayList<ArrayList<Object>> result = dbHelper.executeQuery(query);

            if (result.isEmpty())
            {JOptionPane.showMessageDialog(frame, "No games in inventory.", "Display Games", JOptionPane.INFORMATION_MESSAGE);}
            else
            {
                StringBuilder gamesList = new StringBuilder();
                for (ArrayList<Object> row : result)
                {
                    int id = (int) row.get(0);
                    String name = (String) row.get(1);
                    String type = (String) row.get(2);
                    String category = (String) row.get(3);
                    double price = (double) row.get(4);
                    int quantity = (int) row.get(5);
                    gamesList.append(id).append(". ").append(name).append(", ").append(type).append(", ")
                            .append(category).append(", $").append(GameInfo.priceFormat.format(price))
                            .append(", ").append(quantity).append(" copies\n");
                }
                JOptionPane.showMessageDialog(frame, gamesList.toString(), "Display Games", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e)
        {JOptionPane.showMessageDialog(frame, "Error fetching games from database: " + e.getMessage());}

        finally
        {DBHelper.close();}
    }

    /**
     * Method used for adding more games to the inventory.
     *It will ask for the game's information such as name and price (check 'GameInfo' class to learn more
     * about the game's attributes).
     */
    private static void addGame()
    {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        JTextField nameField = new JTextField();
        JComboBox<String> typeComboBox = new JComboBox<>(gameTypes.toArray(new String[0]));
        JComboBox<String> categoryComboBox = new JComboBox<>(gameCategories.toArray(new String[0]));
        JTextField priceField = new JTextField();
        JTextField quantityField = new JTextField();

        panel.add(new JLabel("Game Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Game Type:"));
        panel.add(typeComboBox);
        panel.add(new JLabel("Game Category:"));
        panel.add(categoryComboBox);
        panel.add(new JLabel("Game Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Game Quantity:"));
        panel.add(quantityField);

        while (true)
        {
            int result = JOptionPane.showConfirmDialog(frame, panel, "Add New Game", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION)
            {return;}

            StringBuilder errorMessage = new StringBuilder();
            String name = nameField.getText().trim();
            String type = (String) typeComboBox.getSelectedItem();
            String category = (String) categoryComboBox.getSelectedItem();
            String priceText = priceField.getText().trim();
            String quantityText = quantityField.getText().trim();

            if (name.isEmpty())
            {errorMessage.append("Game name is required.\n");}

            if (type == null)
            {errorMessage.append("Game type is required.\n");}

            if (category == null)
            {errorMessage.append("Game category is required.\n");}

            if (priceText.isEmpty())
            {errorMessage.append("Game price is required.\n");}

            if (quantityText.isEmpty())
            {errorMessage.append("Game quantity is required.\n");}

            boolean validPrice = true;
            double price = 0;
            try
            {
                if (!priceText.isEmpty())
                {price = Double.parseDouble(priceText);}
            } catch (NumberFormatException e)
            {
                errorMessage.append("Invalid input for price.\n");
                validPrice = false;
            }

            boolean validQuantity = true;
            int quantity = 0;
            try
            {
                if (!quantityText.isEmpty())
                {quantity = Integer.parseInt(quantityText);}
            } catch (NumberFormatException e)
            {
                errorMessage.append("Invalid input for quantity.\n");
                validQuantity = false;
            }

            if (!errorMessage.isEmpty())
            {
                JOptionPane.showMessageDialog(frame, errorMessage.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            if (validPrice && validQuantity)
            {
                GameInfo newGame = new GameInfo(nextId++, name, type, category, price, quantity);
                gameInventory.put(newGame.getId(), newGame);

                try {
                    String insertSQL = "INSERT INTO Game4Life (gameID, gameName, gameType, gameCategory, gamePrice, gameQuantity) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement preparedStatement = DBHelper.prepareStatement(insertSQL);
                    preparedStatement.setInt(1, newGame.id);
                    preparedStatement.setString(2, newGame.name);
                    preparedStatement.setString(3, newGame.type);
                    preparedStatement.setString(4, newGame.category);
                    preparedStatement.setDouble(5, newGame.price);
                    preparedStatement.setInt(6, newGame.quantity);
                    preparedStatement.executeUpdate();
                    DBHelper.close();
                    JOptionPane.showMessageDialog(frame, "Game added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(frame, "Error saving game to database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Method used for removing games from the inventory.
     * It will ask for the name of the game and for the amount that it's being removed. It will also
     * reassign the game ID accordingly (check the 'reassignIDs' for more details).
     */
    private static void removeGame()
    {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));

        JTextField nameField = new JTextField();
        JTextField quantityField = new JTextField();

        panel.add(new JLabel("Game Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Quantity to Remove:"));
        panel.add(quantityField);

        while (true)
        {
            int result = JOptionPane.showConfirmDialog(frame, panel, "Remove/Sell Game", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION)
            {return;}

            String name = nameField.getText().trim();
            String quantityText = quantityField.getText().trim();
            StringBuilder errorMessage = new StringBuilder();

            if (name.isEmpty())
            {errorMessage.append("Game name is required.\n");}

            boolean validQuantity = true;
            int quantity = 0;
            try
            {
                if (!quantityText.isEmpty())
                {
                    quantity = Integer.parseInt(quantityText);
                    if (quantity <= 0)
                    {
                        errorMessage.append("Quantity must be greater than zero.\n");
                        validQuantity = false;
                    }
                } else
                {
                    errorMessage.append("Quantity is required.\n");
                    validQuantity = false;
                }
            } catch (NumberFormatException e)
            {
                errorMessage.append("Invalid input for quantity.\n");
                validQuantity = false;
            }

            if (!errorMessage.isEmpty())
            {
                JOptionPane.showMessageDialog(frame, errorMessage.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            if (validQuantity)
            {
                boolean found = false;
                for (GameInfo game : gameInventory.values())
                {
                    if (game.name.equalsIgnoreCase(name))
                    {
                        found = true;
                        if (game.quantity >= quantity)
                        {
                            game.quantity -= quantity;
                            if (game.quantity == 0) {
                                gameInventory.remove(game.id);
                                DBHelper.deleteGame(game.id);
                                reassignIds(); //Method found at the end of this code; responsible for updating the game IDs.
                            } else
                            {DBHelper.updateGameQuantity(game.id, game.quantity);}
                            JOptionPane.showMessageDialog(frame, "Game removed/sold successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        } else
                        {JOptionPane.showMessageDialog(frame, "Not enough quantity to remove/sell.", "Error", JOptionPane.ERROR_MESSAGE);}
                    }
                }
                if (!found)
                {JOptionPane.showMessageDialog(frame, "Game not found.", "Error", JOptionPane.ERROR_MESSAGE);}
            }
        }
    }

    /**
     * Method used for changing any of the information fields of a specific game.
     * It will ask for the game ID and allow the user to change its information, if needed.
     */
    private static void updateGame()
    {
        JFrame frame = new JFrame("Update Game Information");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));

        JLabel idLabel = new JLabel("Enter the ID of the game you want to update:");
        JTextField idField = new JTextField();
        idField.setPreferredSize(new Dimension(10, 20));

        panel.add(idLabel);
        panel.add(idField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            String idText = idField.getText();
            int id;

            try
            {
                id = Integer.parseInt(idText);

                if (!gameInventory.containsKey(id))
                {
                    JOptionPane.showMessageDialog(frame, "Error: Game with ID " + id + " not found; please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                GameInfo game = gameInventory.get(id);

                JFrame updateFrame = new JFrame("Update Game Information");
                updateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                updateFrame.setSize(1000, 700);
                updateFrame.setLayout(new GridLayout(7, 2));

                JLabel nameLabel = new JLabel("Enter the new name of the game (current: " + game.name + "):");
                JTextField nameField = new JTextField(game.name);

                JLabel typeLabel = new JLabel("For what type of console? (current: " + game.type + "):");
                JComboBox<String> typeBox = new JComboBox<>(new String[]{"Playstation", "Xbox", "Nintendo Switch"});
                typeBox.setSelectedItem(game.type);

                JLabel categoryLabel = new JLabel("What's the game category? (current: " + game.category + "):");
                JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Action/Adventure", "Sci-Fi", "Horror/Thriller", "Puzzles", "Family-Friendly", "Sports"});
                categoryBox.setSelectedItem(game.category);

                JLabel priceLabel = new JLabel("Enter the new price of the game (current: $" + game.price + "):");
                JTextField priceField = new JTextField(String.valueOf(game.price));

                JLabel quantityLabel = new JLabel("Enter the new amount of copies of the game (current: " + game.quantity + "):");
                JTextField quantityField = new JTextField(String.valueOf(game.quantity));

                JButton updateButton = new JButton("Update");

                updateButton.addActionListener(ev -> {
                    String newName = nameField.getText();
                    String newType = (String) typeBox.getSelectedItem();
                    String newCategory = (String) categoryBox.getSelectedItem();
                    double newPrice;
                    int newQuantity;

                    try
                    {newPrice = Double.parseDouble(priceField.getText());}
                    catch (NumberFormatException ex)
                    {
                        JOptionPane.showMessageDialog(updateFrame, "Incorrect input for price; please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try
                    {newQuantity = Integer.parseInt(quantityField.getText());}
                    catch (NumberFormatException ex)
                    {
                        JOptionPane.showMessageDialog(updateFrame, "Incorrect input for quantity; please enter a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    game.name = newName;
                    game.type = newType;
                    game.category = newCategory;
                    game.price = newPrice;
                    game.quantity = newQuantity;

                    boolean updateSuccess = DBHelper.updateGameDetails(game);

                    if (updateSuccess)
                    {JOptionPane.showMessageDialog(updateFrame, "Game information has been updated successfully.");}
                    else
                    {JOptionPane.showMessageDialog(updateFrame, "Failed to update game information.", "Error", JOptionPane.ERROR_MESSAGE);}

                    updateFrame.dispose();
                });

                updateFrame.add(nameLabel);
                updateFrame.add(nameField);
                updateFrame.add(typeLabel);
                updateFrame.add(typeBox);
                updateFrame.add(categoryLabel);
                updateFrame.add(categoryBox);
                updateFrame.add(priceLabel);
                updateFrame.add(priceField);
                updateFrame.add(quantityLabel);
                updateFrame.add(quantityField);
                updateFrame.add(new JLabel());
                updateFrame.add(updateButton);

                updateFrame.setLocationRelativeTo(null);
                updateFrame.setVisible(true);
            } catch (NumberFormatException ex)
            {JOptionPane.showMessageDialog(frame, "Error: Invalid ID format; please enter a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);}
        });

        frame.add(panel, BorderLayout.CENTER);
        frame.add(submitButton, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Method used for making game orders.
     * It will ask for the game's information and the name of the table where the order's information will be saved at.
     * If a table with that name doesn't exist, it will create a new on in the database.
     */
    private static void orderGames()
    {
        JFrame frame = new JFrame("Order Games");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLayout(new GridLayout(8, 2));

        DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(dateTime);

        JLabel nameLabel = new JLabel("Enter the name of the game:");
        JTextField nameField = new JTextField();

        JLabel typeLabel = new JLabel("What is the console for this game?");
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Playstation", "Xbox", "Nintendo Switch"});

        JLabel categoryLabel = new JLabel("What is the category of the game?");
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Action/Adventure", "Sci-Fi", "Horror/Thriller", "Puzzles", "Family-Friendly", "Sports"});

        JLabel priceLabel = new JLabel("Enter the price of the game (in 0.00 format):");
        JTextField priceField = new JTextField();

        JLabel quantityLabel = new JLabel("How many copies are needed?");
        JTextField quantityField = new JTextField();

        JLabel tableLabel = new JLabel("Enter the name of the table to save the order:");
        JTextField tableField = new JTextField();

        JButton nextButton = new JButton("Next");

        frame.add(nameLabel);
        frame.add(nameField);
        frame.add(typeLabel);
        frame.add(typeBox);
        frame.add(categoryLabel);
        frame.add(categoryBox);
        frame.add(priceLabel);
        frame.add(priceField);
        frame.add(quantityLabel);
        frame.add(quantityField);
        frame.add(tableLabel);
        frame.add(tableField);
        frame.add(new JLabel());
        frame.add(nextButton);

        nextButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            String category = (String) categoryBox.getSelectedItem();
            double price;
            int quantity;
            String tableName = tableField.getText().trim();

            if (name.isEmpty())
            {
                JOptionPane.showMessageDialog(frame, "Name cannot be empty; please enter the name of the game.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (type == null || type.isEmpty())
            {
                JOptionPane.showMessageDialog(frame, "Please select the type of console for the game.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (category == null || category.isEmpty())
            {
                JOptionPane.showMessageDialog(frame, "Please select the category of the game.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try
            {price = Double.parseDouble(priceField.getText().trim());}
            catch (NumberFormatException ex)
            {
                JOptionPane.showMessageDialog(frame, "Incorrect input for price; please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try
            {quantity = Integer.parseInt(quantityField.getText().trim());}
            catch (NumberFormatException ex)
            {
                JOptionPane.showMessageDialog(frame, "Incorrect input for quantity; please enter a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (tableName.isEmpty())
            {
                JOptionPane.showMessageDialog(frame, "Table name cannot be empty; please enter the table name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            DBHelper.connect();
            if (!DBHelper.tableExists(tableName))
            {DBHelper.createOrdersTable(tableName);}

            DBHelper.insertOrder(tableName, formattedDate, name, type, category, price, quantity);
            JOptionPane.showMessageDialog(frame, "Order complete; expect the delivery between 1-2 weeks. " +
                    "The order has been recorded in the " + tableName + " table.\n");
            DBHelper.close();
            frame.dispose();
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Method responsible for updating the game ID when the 'removeGame' method is used.
     * It will make sure that the inventory stays in ascending order.
     */
    private static void reassignIds()
    {
        int newId = 1;
        Map<Integer, GameInfo> newGameInventory = new LinkedHashMap<>();

        for (GameInfo game : new ArrayList<>(gameInventory.values()))
        {
            int oldId = game.id;
            game.id = newId;
            newGameInventory.put(newId, game);
            DBHelper.updateGameId(oldId, newId);
            newId++;
        }

        gameInventory = newGameInventory;
        nextId = newId;
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////

import java.text.DecimalFormat;

/**
 * GameInfo contains the general information of a game.
 * The 6 attributes of a game are: the game ID, the name,
 * type of console where it's played, the category (or genre), price, and amount of copies.
 * It also includes the format that is used when using the 'displayGames' method in the main code.
 */
public class GameInfo
{
    int id;
    String name;
    String type;
    String category;
    double price;
    int quantity;

    public static final DecimalFormat priceFormat = new DecimalFormat("0.00");

    public GameInfo(int id, String name, String type, String category, double price, int quantity)
    {
        this.id = id;
        this.name = name;
        this.type = type;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
    }

    public int getId()
    {return id;}

    public String getName()
    {return name;}

    public String getType()
    {return type;}

    public String getCategory()
    {return category;}

    public double getPrice()
    {return price;}

    public int getQuantity()
    {return quantity;}

    @Override
    public String toString()
    {return id + ". " + name + ", " + type + ", " + category + ", $" + priceFormat.format(price) + ", " + quantity + " copies";}

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import static java.sql.DriverManager.getConnection;

/**
 * DBHelper contains the methods used in the main code that help with connecting
 * and querying the database.
 */
public class DBHelper
{
	private static Connection connection;
	private static Statement statement;
	private static ResultSet resultSet;

	public DBHelper()
	{
		connection = null;
		statement = null;
		resultSet = null;
	}

	public static void connect()
	{
		try
		{Class.forName("org.sqlite.JDBC");}
		catch (ClassNotFoundException e)
		{e.printStackTrace();}

		try
		{
			connection = getConnection("jdbc:sqlite:" + Game4LifeDMS.DATABASE_NAME);
			statement = connection.createStatement();
		} catch (SQLException e)
		{e.printStackTrace();}

	}

	public static void close()
	{
		try
		{
			connection.close();
			statement.close();
			if (resultSet != null)
				resultSet.close();
		} catch (SQLException e)
		{e.printStackTrace();}
	}

	public static PreparedStatement prepareStatement(String sql) throws SQLException
	{
		connect();
		return connection.prepareStatement(sql);
	}

	private Object[][] arrayListTo2DArray(ArrayList<ArrayList<Object>> list)
	{
		Object[][] array = new Object[list.size()][];
		for (int i = 0; i < list.size(); i++)
		{
			ArrayList<Object> row = list.get(i);
			array[i] = row.toArray(new Object[row.size()]);
		}
		return array;
	}

	public void execute(String sql)
	{
		try
		{
			connect();
			statement.execute(sql);
		} catch (SQLException e)
		{e.printStackTrace();}

		finally
		{close();}
	}


	public DefaultTableModel executeQueryToTable(String sql)
	{
		ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();
		ArrayList<Object> columns = new ArrayList<Object>();
		connect();
		try
		{
			resultSet = statement.executeQuery(sql);
			int columnCount = resultSet.getMetaData().getColumnCount();
			for (int i = 1; i <= columnCount; i++)
			columns.add(resultSet.getMetaData().getColumnName(i));
			while (resultSet.next())
			{
				ArrayList<Object> subresult = new ArrayList<Object>();
				for (int i = 1; i <= columnCount; i++)
				subresult.add(resultSet.getObject(i));
				result.add(subresult);
			}
		} catch (SQLException e)
		{e.printStackTrace();}
		close();

		return new DefaultTableModel(arrayListTo2DArray(result), columns.toArray());
	}

	public ArrayList<ArrayList<Object>> executeQuery(String sql)
	{
		ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();
		connect();
		try
		{
			resultSet = statement.executeQuery(sql);
			int columnCount = resultSet.getMetaData().getColumnCount();
			while (resultSet.next())
			{
				ArrayList<Object> subresult = new ArrayList<Object>();
				for (int i = 1; i <= columnCount; i++)
				{subresult.add(resultSet.getObject(i));}
				result.add(subresult);
			}
		} catch (SQLException e)
		{e.printStackTrace();}
		close();

		return result;
	}

	public static void updateGameQuantity(int id, int quantity)
	{
		String sql = "UPDATE Game4Life SET gameQuantity = ? WHERE gameID = ?";
		try (PreparedStatement pstmt = prepareStatement(sql))
		{
			pstmt.setInt(1, quantity);
			pstmt.setInt(2, id);
			pstmt.executeUpdate();
		} catch (SQLException e)
		{System.out.println(e.getMessage());}
	}

	public static void updateGameId(int oldId, int newId)
	{
		String sql = "UPDATE Game4Life SET gameID = ? WHERE gameID = ?";
		try (PreparedStatement pstmt = prepareStatement(sql))
		{
			pstmt.setInt(1, newId);
			pstmt.setInt(2, oldId);
			pstmt.executeUpdate();
		} catch (SQLException e)
		{System.out.println(e.getMessage());}
	}

	public static void deleteGame(int id)
	{
		String sql = "DELETE FROM Game4Life WHERE gameID = ?";
		try (PreparedStatement pstmt = prepareStatement(sql))
		{
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
		} catch (SQLException e)
		{System.out.println(e.getMessage());}
	}

	public static boolean updateGameDetails(GameInfo game)
	{
		String sql = "UPDATE Game4Life SET gameName = ?, gameType = ?, gameCategory = ?, gamePrice = ?, gameQuantity = ? WHERE gameID = ?";
		try (PreparedStatement pstmt = prepareStatement(sql))
		{
			pstmt.setString(1, game.getName());
			pstmt.setString(2, game.getType());
			pstmt.setString(3, game.getCategory());
			pstmt.setDouble(4, game.getPrice());
			pstmt.setInt(5, game.getQuantity());
			pstmt.setInt(6, game.getId());
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e)
		{
			System.out.println(e.getMessage());
			return false;
		}
	}

	public static boolean tableExists(String tableName)
	{
		connect();
		try
		{
			DatabaseMetaData meta = connection.getMetaData();
			ResultSet res = meta.getTables(null, null, tableName, new String[] {"TABLE"});
			boolean exists = res.next();
			res.close();
			return exists;
		} catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		} finally
		{close();}
	}

	public static void createOrdersTable(String tableName)
	{
		connect();
		String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
				"orderID INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"orderDate TEXT, " +
				"gameName TEXT, " +
				"gameType TEXT, " +
				"gameCategory TEXT, " +
				"gamePrice REAL, " +
				"gameQuantity INTEGER)";
		try (Statement stmt = connection.createStatement())
		{stmt.execute(sql);}
		catch (SQLException e)
		{e.printStackTrace();}
		finally
		{close();}
	}

	public static void insertOrder(String tableName, String date, String name, String type, String category, double price, int quantity)
	{
		String sql = "INSERT INTO " + tableName + "(orderDate, gameName, gameType, gameCategory, gamePrice, gameQuantity) VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = prepareStatement(sql)) {
			pstmt.setString(1, date);
			pstmt.setString(2, name);
			pstmt.setString(3, type);
			pstmt.setString(4, category);
			pstmt.setDouble(5, price);
			pstmt.setInt(6, quantity);
			pstmt.executeUpdate();
		} catch (SQLException e)
		{System.out.println(e.getMessage());}
	}
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

/**
 * This class contains the schema for the table 'Game4Life'.
 */
public class Game4Life extends DBHelper
{
	private final String TABLE_NAME = "Game4Life";
	public static final String gameID = "gameID";
	public static final String gameName = "gameName";
	public static final String gameType = "gameType";
	public static final String gameCategory = "gameCategory";
	public static final String gamePrice = "gamePrice";
	public static final String  gameQuantity = "gameQuantity";

	private String prepareSQL(String fields, String whatField, String whatValue, String sortField, String sort)
	{
		String query = "SELECT ";
		query += fields == null ? " * FROM " + TABLE_NAME : fields + " FROM " + TABLE_NAME;
		query += whatField != null && whatValue != null ? " WHERE " + whatField + " = \"" + whatValue + "\"" : "";
		query += sort != null && sortField != null ? " order by " + sortField + " " + sort : "";
		return query;
	}

	public void insert(Integer gameID, String gameName, String gameType, String gameCategory, Double gamePrice, Integer gameQuantity)
	{
		gameName = gameName != null ? "\"" + gameName + "\"" : null;
		gameType = gameType != null ? "\"" + gameType + "\"" : null;
		gameCategory = gameCategory != null ? "\"" + gameCategory + "\"" : null;
		
		Object[] values_ar = {gameID, gameName, gameType, gameCategory, gamePrice, gameQuantity};
		String[] fields_ar = {Game4Life.gameID, Game4Life.gameName, Game4Life.gameType, Game4Life.gameCategory, Game4Life.gamePrice, Game4Life.gameQuantity};
		String values = "", fields = "";
		for (int i = 0; i < values_ar.length; i++)
		{
			if (values_ar[i] != null)
			{
				values += values_ar[i] + ", ";
				fields += fields_ar[i] + ", ";
			}
		}
		if (!values.isEmpty())
		{
			values = values.substring(0, values.length() - 2);
			fields = fields.substring(0, fields.length() - 2);
			super.execute("INSERT INTO " + TABLE_NAME + "(" + fields + ") values(" + values + ");");
		}
	}

	public void delete(String whatField, String whatValue)
	{super.execute("DELETE from " + TABLE_NAME + " where " + whatField + " = " + whatValue + ";");}

	public void update(String whatField, String whatValue, String whereField, String whereValue)
	{super.execute("UPDATE " + TABLE_NAME + " set " + whatField + " = \"" + whatValue + "\" where " + whereField + " = \"" + whereValue + "\";");}

	public ArrayList<ArrayList<Object>> select(String fields, String whatField, String whatValue, String sortField, String sort)
	{return super.executeQuery(prepareSQL(fields, whatField, whatValue, sortField, sort));}

	public ArrayList<ArrayList<Object>> getExecuteResult(String query)
	{return super.executeQuery(query);}

	public void execute(String query)
	{super.execute(query);}

	public DefaultTableModel selectToTable(String fields, String whatField, String whatValue, String sortField, String sort)
	{return super.executeQueryToTable(prepareSQL(fields, whatField, whatValue, sortField, sort));}
}
