import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class UserWindow extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public UserWindow() {
        setTitle("Окно пользователя");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        getContentPane().add(panel);

        model = new DefaultTableModel();
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton editButton = new JButton("Редактировать");
        JButton createButton = new JButton("Создать");
        JButton deleteButton = new JButton("Удалить");
        buttonPanel.add(editButton);
        buttonPanel.add(createButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        editButton.addActionListener(e -> editRecord());
        createButton.addActionListener(e -> createRecord());
        deleteButton.addActionListener(e -> deleteRecord());

        loadDataFromDatabase();

        setVisible(true);
    }

    private void loadDataFromDatabase() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "root");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users");

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            model.setColumnCount(0);
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }

            model.setRowCount(0);

            while (resultSet.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    rowData[i] = resultSet.getObject(i + 1);
                }
                model.addRow(rowData);
            }

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке данных из базы данных: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            Object idObj = table.getValueAt(selectedRow, 0);
            Object nameObj = table.getValueAt(selectedRow, 1);
            Object emailObj = table.getValueAt(selectedRow, 2);
            Object passwordObj = table.getValueAt(selectedRow, 3);

            if (idObj instanceof Integer && nameObj instanceof String && emailObj instanceof String && passwordObj instanceof String) {
                int id = (int) idObj;
                String username = (String) nameObj;
                String email = (String) emailObj;
                String password = (String) passwordObj;

                try {
                    Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "root");
                    String sql = "UPDATE users SET username=?, email=?, password=? WHERE id=?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, username);
                    pstmt.setString(2, email);
                    pstmt.setString(3, password);
                    pstmt.setInt(4, id);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(UserWindow.this, "Запись успешно обновлена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
                    pstmt.close();
                    conn.close();

                    model.setValueAt(username, selectedRow, 1);
                    model.setValueAt(email, selectedRow, 2);
                    model.setValueAt(password, selectedRow, 3);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(UserWindow.this, "Ошибка при обновлении записи в базе данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(UserWindow.this, "Некорректные данные для редактирования.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(UserWindow.this, "Выберите запись для редактирования.", "Предупреждение", JOptionPane.WARNING_MESSAGE);
        }
    }



    private void createRecord() {
        JTextField field1 = new JTextField();
        JTextField field2 = new JTextField();
        JTextField field3 = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Логин"));
        panel.add(field1);
        panel.add(new JLabel("почта"));
        panel.add(field2);
        panel.add(new JLabel("пароль"));
        panel.add(field3);

        int result = JOptionPane.showConfirmDialog(null, panel, "Введите данные для новой записи",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String value1 = field1.getText();
            String value2 = field2.getText();
            String value3 = field3.getText();

            try {
                Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "root");
                PreparedStatement statement = connection.prepareStatement("INSERT INTO users (username, email, password) VALUES (?, ?, ?)");
                statement.setString(1, value1);
                statement.setString(2, value2);
                statement.setString(3, value3);
                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Новая запись успешно добавлена.");
                    loadDataFromDatabase();
                }
                connection.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении новой записи в базу данных: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int confirmDialogResult = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить выбранную запись?", "Удаление записи", JOptionPane.YES_NO_OPTION);
            if (confirmDialogResult == JOptionPane.YES_OPTION) {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "root");
                    Statement statement = connection.createStatement();
                    int idToDelete = (int) table.getValueAt(selectedRow, 0);
                    statement.executeUpdate("DELETE FROM users WHERE id = " + idToDelete);
                    connection.close();
                    loadDataFromDatabase();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Ошибка при удалении записи из базы данных: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите запись для удаления.", "Предупреждение", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UserWindow::new);
    }
}
