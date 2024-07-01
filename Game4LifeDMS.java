//Password to login is secured123
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.text.DecimalFormat;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

class GameInfo
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

public class Game4LifeDMS
{
    private static final String loginPassword = "secured123";
    private static final List<String> gameTypes = Arrays.asList("Playstation", "Xbox", "Nintendo Switch");
    private static final List<String> gameCategories = Arrays.asList("Action/Adventure", "Sci-Fi", "Horror/Thriller", "Puzzles", "Family-Friendly", "Sports");
    private static Map<Integer, GameInfo> gameInventory = new HashMap<>();
    private static int nextId = 1;
    private static JFrame frame;
    private static JTextArea outputArea;
    private static String filePath;

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(Game4LifeDMS::loginGUI);
    }

    private static void loginGUI()
    {
        int attempts = 0;
        while (attempts < 3)
        {
            String inputPassword = JOptionPane.showInputDialog(null, "Welcome to the Game4Life Database System!" +
                    "\nPlease enter the password to login (3 attempts max):", "Login", JOptionPane.QUESTION_MESSAGE);
            if ("secured123".equals(inputPassword))
            {
                uploadFile();
                createMainGUI();
                outputArea.append("Welcome to the Game4Life DMS! Pick from the following choices:\n\n");
                return;
            }
            JOptionPane.showMessageDialog(null, "Incorrect password; please try again.");
            attempts++;
        }
        JOptionPane.showMessageDialog(null, "Maximum login attempts exceeded; the program will close now...");
        System.exit(0);
    }

    private static void uploadFile()
    {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            filePath = selectedFile.getPath();
            loadGamesFromFile(selectedFile);
        } else
        {
            JOptionPane.showMessageDialog(null, "File upload failed; exiting the system.");
            System.exit(0);
        }
    }

    private static void loadGamesFromFile(File file)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(file)))
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
                double price = Double.parseDouble(parts[3].substring(1)); // Removing the '$' sign
                int quantity = Integer.parseInt(parts[4].split(" ")[0]); // Removing the 'copies' part
                GameInfo game = new GameInfo(id, name, type, category, price, quantity);
                gameInventory.put(id, game);
                maxId = Math.max(maxId, id);
            }
            nextId = maxId + 1;
        } catch (IOException e)
        {
            JOptionPane.showMessageDialog(null, "File not found or incorrect format; please try again.");
            System.exit(0);
        } catch (NumberFormatException e)
        {
            JOptionPane.showMessageDialog(null, "Error in data format; please type the correct format for the file.");
            System.exit(0);
        }
    }

    private static void saveInventoryToFile()
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
            JOptionPane.showMessageDialog(frame, "Error saving to file: " + e.getMessage());
        }
    }

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
            saveInventoryToFile();
            System.exit(0);
        });
        buttonPanel.add(exitButton);

        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void displayGames()
    {
        if (gameInventory.isEmpty())
        {
            JOptionPane.showMessageDialog(frame, "No games in inventory.", "Display Games", JOptionPane.INFORMATION_MESSAGE);
        } else
        {
            StringBuilder gamesList = new StringBuilder();
            for (GameInfo game : gameInventory.values())
            {
                gamesList.append(game).append("\n");
            }
            JOptionPane.showMessageDialog(frame, gamesList.toString(), "Display Games", JOptionPane.INFORMATION_MESSAGE);
        }
    }

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
                {
                    price = Double.parseDouble(priceText);
                }
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
                {
                    quantity = Integer.parseInt(quantityText);
                }
            } catch (NumberFormatException e)
            {
                errorMessage.append("Invalid input for quantity.\n");
                validQuantity = false;
            }

            if (errorMessage.length() > 0)
            {
                JOptionPane.showMessageDialog(frame, errorMessage.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            if (validPrice && validQuantity)
            {
                GameInfo newGame = new GameInfo(nextId++, name, type, category, price, quantity);
                gameInventory.put(newGame.id, newGame);
                saveInventoryToFile();
                JOptionPane.showMessageDialog(frame, "Game added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                break;
            }
        }
    }


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

            if (errorMessage.length() > 0)
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
                            if (game.quantity == 0)
                            {
                                gameInventory.remove(game.id);
                                reassignIds();
                            }
                            saveInventoryToFile();
                            JOptionPane.showMessageDialog(frame, "Game removed/sold successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        } else
                        {
                            JOptionPane.showMessageDialog(frame, "Not enough quantity to remove/sell.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                if (!found)
                {
                    JOptionPane.showMessageDialog(frame, "Game not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }


    private static void updateGame()
    {
        JFrame frame = new JFrame("Update Game Information");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));

        JLabel idLabel = new JLabel("Enter the ID of the game you want to update:");
        JTextField idField = new JTextField();
        idField.setPreferredSize(new Dimension(10, 20));

        panel.add(idLabel);
        panel.add(idField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e ->
        {
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

                updateButton.addActionListener(ev ->
                {
                    String newName = nameField.getText();
                    String newType = (String) typeBox.getSelectedItem();
                    String newCategory = (String) categoryBox.getSelectedItem();
                    double newPrice;
                    int newQuantity;

                    try
                    {
                        newPrice = Double.parseDouble(priceField.getText());
                    } catch (NumberFormatException ex)
                    {
                        JOptionPane.showMessageDialog(updateFrame, "Incorrect input for price; please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        newQuantity = Integer.parseInt(quantityField.getText());
                    } catch (NumberFormatException ex)
                    {
                        JOptionPane.showMessageDialog(updateFrame, "Incorrect input for quantity; please enter a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    game.name = newName;
                    game.type = newType;
                    game.category = newCategory;
                    game.price = newPrice;
                    game.quantity = newQuantity;

                    JOptionPane.showMessageDialog(updateFrame, "Game information has been updated successfully.");
                    updateFrame.dispose();
                    saveInventoryToFile();
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
            {
                JOptionPane.showMessageDialog(frame, "Error: Invalid ID format; please enter a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.add(panel, BorderLayout.CENTER);
        frame.add(submitButton, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }



    private static void orderGames()
    {
        JFrame frame = new JFrame("Order Games");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 300);
        frame.setLayout(new GridLayout(7, 2));

        DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

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
        frame.add(new JLabel());
        frame.add(nextButton);

        nextButton.addActionListener(e ->
        {
            String name = nameField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            String category = (String) categoryBox.getSelectedItem();
            double price;
            int quantity;

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
            {
                price = Double.parseDouble(priceField.getText().trim());
            } catch (NumberFormatException ex)
            {
                JOptionPane.showMessageDialog(frame, "Incorrect input for price; please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try
            {
                quantity = Integer.parseInt(quantityField.getText().trim());
            } catch (NumberFormatException ex)
            {
                JOptionPane.showMessageDialog(frame, "Incorrect input for quantity; please enter a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFrame fileFrame = new JFrame("Order File");
            fileFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            fileFrame.setSize(300, 150);
            fileFrame.setLayout(new BorderLayout());

            JPanel filePanel = new JPanel();
            filePanel.setLayout(new GridLayout(2, 1));

            JLabel fileLabel = new JLabel("Enter the name for the order file (without extension):");
            JTextField fileField = new JTextField();

            filePanel.add(fileLabel);
            filePanel.add(fileField);

            JButton saveButton = new JButton("Save Order");
            saveButton.addActionListener(ev ->
            {
                String fileName = fileField.getText().trim() + ".txt";
                File file = new File(fileName);

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true)))
                {
                    if (!file.exists())
                    {file.createNewFile();}

                    writer.write("Date Order: " + now.format(dateTime));
                    writer.newLine();
                    writer.write(name + ", " + type + ", " + category + ", $" + String.format("%.2f", price) + ", " + quantity + " copies");
                    writer.newLine();
                    JOptionPane.showMessageDialog(fileFrame, "Order complete; expect the delivery between 1-2 weeks. The order has been recorded in " + fileName + "\n");
                    fileFrame.dispose();
                    frame.dispose();
                } catch (IOException ex)
                {
                    JOptionPane.showMessageDialog(fileFrame, "An error occurred while writing to the file.", "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            });

            fileFrame.add(filePanel, BorderLayout.CENTER);
            fileFrame.add(saveButton, BorderLayout.SOUTH);

            fileFrame.setLocationRelativeTo(frame);
            fileFrame.setVisible(true);
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    private static void reassignIds()
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
