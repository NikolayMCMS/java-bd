import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import java.io.File;
import org.w3c.dom.Element;
import java.sql.*;

public class Main extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField usernamelogField; // Добавлено для поля логина во время авторизации
    private JPasswordField passwordlogField; // Добавлено для поля пароля во время авторизации

    public Main() {
        setTitle("Авторизация и Регистрация");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        cardLayout = new CardLayout();
        cardPanel = new JPanel();
        cardPanel.setLayout(cardLayout);

        // Инициализация подключения к базе данных
        initializeDatabase();

        // Добавление панелей для авторизации и регистрации
        JPanel loginPanel = loadPageFromXML("xml/Login.xml");
        JPanel registrationPanel = loadPageFromXML("xml/Registration.xml");

        cardPanel.add(loginPanel, "Login");
        cardPanel.add(registrationPanel, "Registration");

        getContentPane().add(cardPanel);
        setVisible(true);
    }

    // Подключение к базе данных
    private Connection connection;

    // Метод для инициализации подключения к базе данных
    private void initializeDatabase() {
        try {
            // Загрузка драйвера JDBC
            Class.forName("org.postgresql.Driver");

            // Установка соединения с базой данных
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "root");

            // Вывод сообщения об успешном подключении
            System.out.println("Подключение к базе данных успешно.");
        } catch (ClassNotFoundException | SQLException e) {
            // Обработка исключений
            e.printStackTrace();
        }
    }

    private void login() {
        String usernamelog = usernamelogField.getText();
        String passwordlog = new String(passwordlogField.getPassword());

        // Проверка наличия значений в полях
        if (usernamelog.isEmpty() || passwordlog.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ошибка при авторизации. Пожалуйста, заполните все поля.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Выполните запрос к базе данных, чтобы проверить учетные данные пользователя
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
            statement.setString(1, usernamelog);
            statement.setString(2, passwordlog);
            ResultSet resultSet = statement.executeQuery();

            // Если результат запроса не пустой, значит пользователь с такими учетными данными существует
            if (resultSet.next()) {
                // Вход выполнен успешно, выполните действия, необходимые для входа пользователя
                JOptionPane.showMessageDialog(this, "Вход выполнен успешно!");
                RedirectToUserWindow();
                // Закрыть окно регистрации
                dispose();
                // Здесь может быть переключение на другую панель или выполнение других действий для входа пользователя
            } else {
                // Если результат запроса пустой, учетные данные неверны
                JOptionPane.showMessageDialog(this, "Ошибка при входе. Пожалуйста, проверьте ваш логин и пароль.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при выполнении запроса: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    private void RedirectToUserWindow() {
        // Создать экземпляр класса, который будет представлять окно пользователя
        UserWindow userWindow = new UserWindow();
        // Отобразить окно пользователя
        userWindow.setVisible(true);
    }


    // Метод для обработки регистрации
    private void register() {
        // Проверка наличия всех требуемых полей
        if (usernameField == null || emailField == null || passwordField == null) {
            JOptionPane.showMessageDialog(this, "Ошибка при регистрации пользователя. Пожалуйста, заполните все поля для регистрации.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Получение значений из текстовых полей
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        // Проверка наличия значений в полях
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ошибка при регистрации пользователя. Пожалуйста, заполните все поля для регистрации.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Выполнение SQL-запроса для регистрации
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (username, email, password) VALUES (?, ?, ?)");
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Пользователь успешно зарегистрирован.");
                cardLayout.show(cardPanel, "Login"); // Переключение на панель входа после успешной регистрации
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при регистрации пользователя: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Метод загрузки содержимого страницы из XML-файла
    private JPanel loadPageFromXML(String filePath) {
        try {
            // Загружаем и разбираем XML файл
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(filePath));

            // Создаем панель, на которую будем добавлять компоненты
            JPanel pagePanel = new JPanel(new BorderLayout());

            // Получаем список всех элементов <input> в документе
            NodeList inputNodes = doc.getElementsByTagName("input");
            for (int i = 0; i < inputNodes.getLength(); i++) {
                Node inputNode = inputNodes.item(i);
                if (inputNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element inputElement = (Element) inputNode;
                    int x = Integer.parseInt(inputElement.getAttribute("x"));
                    int y = Integer.parseInt(inputElement.getAttribute("y"));
                    int width = Integer.parseInt(inputElement.getAttribute("width"));
                    int height = Integer.parseInt(inputElement.getAttribute("height"));
                    String name = inputElement.getAttribute("name");

                    // Создаем соответствующие текстовые поля
                    JTextField textField = new JTextField();
                    textField.setBounds(x, y, width, height);
                    if (name.equals("username")) {
                        usernameField = textField;
                    } else if (name.equals("email")) {
                        emailField = textField;
                    } else if (name.equals("password")) {
                        passwordField = new JPasswordField();
                        passwordField.setBounds(x, y, width, height);
                        pagePanel.add(passwordField);
                        continue; // Переходим к следующей итерации цикла
                    } else if (name.equals("usernamelog")) { // Добавлено для нового id поля логина
                        usernamelogField = textField;
                    } else if (name.equals("passwordlog")) { // Добавлено для нового id поля пароля
                        passwordlogField = new JPasswordField();
                        passwordlogField.setBounds(x, y, width, height);
                        pagePanel.add(passwordlogField);
                        continue; // Переходим к следующей итерации цикла
                    }
                    pagePanel.add(textField);
                }
            }
            // Получаем список всех элементов <label> в документе
            NodeList labelNodes = doc.getElementsByTagName("label");
            for (int i = 0; i < labelNodes.getLength(); i++) {
                Node labelNode = labelNodes.item(i);
                if (labelNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element labelElement = (Element) labelNode;
                    String labelText = labelElement.getTextContent();
                    int x = Integer.parseInt(labelElement.getAttribute("x"));
                    int y = Integer.parseInt(labelElement.getAttribute("y"));
                    int width = Integer.parseInt(labelElement.getAttribute("width"));
                    int height = Integer.parseInt(labelElement.getAttribute("height"));
                    int fontSize = Integer.parseInt(labelElement.getAttribute("fontsize"));
                    JLabel label = new JLabel(labelText);
                    label.setBounds(x, y, width, height);
                    label.setFont(new Font("Arial", Font.PLAIN, fontSize));
                    pagePanel.add(label);
                }
            }

            // Создаем панель для кнопок с null-менеджером компоновки
            JPanel buttonPanel = new JPanel(null);
            pagePanel.add(buttonPanel, BorderLayout.CENTER);

            // Получаем список всех элементов <button> в документе
            NodeList buttonNodes = doc.getElementsByTagName("button");
            for (int i = 0; i < buttonNodes.getLength(); i++) {
                Node buttonNode = buttonNodes.item(i);
                if (buttonNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element buttonElement = (Element) buttonNode;
                    int width = Integer.parseInt(buttonElement.getAttribute("width"));
                    int height = Integer.parseInt(buttonElement.getAttribute("height"));
                    int x = Integer.parseInt(buttonElement.getAttribute("x"));
                    int y = Integer.parseInt(buttonElement.getAttribute("y"));

                    String buttonText = buttonElement.getTextContent();
                    JButton button = new JButton(buttonText);
                    button.setBounds(x, y, width, height);
                    buttonPanel.add(button);

                    // Добавляем уникальный идентификатор кнопки в качестве свойства
                    String buttonId = buttonElement.getAttribute("id");
                    button.putClientProperty("id", buttonId);

                    button.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            // Получаем идентификатор кнопки
                            String buttonId = (String) button.getClientProperty("id");
                            if ("loginButton".equals(buttonId)) {
                                cardLayout.show(cardPanel, "Login");
                            } else if ("registerButton".equals(buttonId)) {
                                cardLayout.show(cardPanel, "Registration"); // Переключение на панель регистрации
                            } else if ("register".equals(buttonId)) {
                                register(); // Вызываем метод регистрации пользователя
                            } else if ("login".equals(buttonId)) {
                                login(); // Вызываем метод авторизации пользователя
                            }
                        }
                    });
                }
            }

            // Возвращаем панель с добавленными компонентами
            return pagePanel;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main();
            }
        });
    }
}

