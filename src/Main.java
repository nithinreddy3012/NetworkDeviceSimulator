
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.regex.Pattern;

public class Main {

    static JFrame frame;
    static DefaultTableModel model;
    static JTable table;
    static JLabel totalLabel, onlineLabel, offlineLabel;
    static JTextField searchField;
    static TableRowSorter<DefaultTableModel> sorter;

    static final String FILE_PATH =
        System.getProperty("user.home") + File.separator
        + "netmonitor-devices.txt";

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

    String[] columns = {
        "Device Name", "Device Type", "IP Address", "Status"
    };

    model = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    loadDevices();
    createDashboard();
});
    }

    // ================= DASHBOARD =================

    static void createDashboard() {

        frame = new JFrame("NETMONITOR - Network Device Manager");
        frame.setSize(1050, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(new Color(240, 244, 250));

        // HEADER

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 45, 80));
        header.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("NETMONITOR");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Network Device Management System");
        subtitle.setForeground(Color.WHITE);

        JPanel heading = new JPanel(new GridLayout(2, 1));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(subtitle);

        header.add(heading, BorderLayout.WEST);

        main.add(header, BorderLayout.NORTH);

        // STATISTICS

        JPanel stats = new JPanel(new GridLayout(1, 3, 15, 15));
        stats.setOpaque(false);

        totalLabel = createStatLabel("Total Devices: 0");
        onlineLabel = createStatLabel("Online Devices: 0");
        offlineLabel = createStatLabel("Offline Devices: 0");

        stats.add(totalLabel);
        stats.add(onlineLabel);
        stats.add(offlineLabel);

        // TABLE

        
        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 14)
        );

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);

        // SEARCH

        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search Device:");
        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(
            new javax.swing.event.DocumentListener() {

                public void insertUpdate(
                    javax.swing.event.DocumentEvent e) {
                    search();
                }

                public void removeUpdate(
                    javax.swing.event.DocumentEvent e) {
                    search();
                }

                public void changedUpdate(
                    javax.swing.event.DocumentEvent e) {
                    search();
                }
            }
        );

        // TABLE AREA

        JPanel tablePanel = new JPanel(new BorderLayout(10, 12));
        tablePanel.setOpaque(false);

       JPanel topPanel = new JPanel(new BorderLayout(10, 10));
topPanel.setOpaque(false);

topPanel.add(stats, BorderLayout.NORTH);
topPanel.add(searchPanel, BorderLayout.SOUTH);

tablePanel.add(topPanel, BorderLayout.NORTH);
tablePanel.add(scrollPane, BorderLayout.CENTER);

        // BUTTONS

        JPanel buttons = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 12, 10)
        );
        buttons.setBackground(new Color(240, 244, 250));

        JButton addButton = new JButton("Add Device");
        JButton editButton = new JButton("Edit Device");
        JButton removeButton = new JButton("Remove Device");
        JButton statusButton = new JButton("Change Status");
        JButton refreshButton = new JButton("Refresh");

        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(removeButton);
        buttons.add(statusButton);
        buttons.add(refreshButton);

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);

        center.add(tablePanel, BorderLayout.CENTER);
        center.add(buttons, BorderLayout.SOUTH);

        main.add(center, BorderLayout.CENTER);

        // BUTTON ACTIONS

        addButton.addActionListener(e -> addDevice());

        editButton.addActionListener(e -> editDevice());

        removeButton.addActionListener(e -> removeDevice());

        statusButton.addActionListener(e -> changeStatus());

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            updateStatistics();
            JOptionPane.showMessageDialog(
                frame, "Dashboard refreshed!"
            );
        });

        frame.add(main);

        updateStatistics();

        frame.setVisible(true);
    }

    // ================= STATISTICS =================

    static JLabel createStatLabel(String text) {

        JLabel label = new JLabel(text, SwingConstants.CENTER);

        label.setFont(new Font("Arial", Font.BOLD, 17));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setBorder(
            BorderFactory.createEmptyBorder(20, 5, 20, 5)
        );

        return label;
    }

    static void updateStatistics() {

        int total = model.getRowCount();
        int online = 0;
        int offline = 0;

        for (int i = 0; i < total; i++) {

            String status = model.getValueAt(i, 3).toString();

            if (status.equals("Online")) {
                online++;
            } else {
                offline++;
            }
        }

        totalLabel.setText("Total Devices: " + total);
        onlineLabel.setText("Online Devices: " + online);
        offlineLabel.setText("Offline Devices: " + offline);
    }

    // ================= ADD DEVICE =================

    static void addDevice() {

        JTextField name = new JTextField();
        JTextField ip = new JTextField();

        JComboBox<String> type = new JComboBox<>(
            new String[]{"Router", "Switch", "Computer", "Server"}
        );

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        panel.add(new JLabel("Device Name:"));
        panel.add(name);

        panel.add(new JLabel("Device Type:"));
        panel.add(type);

        panel.add(new JLabel("IP Address:"));
        panel.add(ip);

        int result = JOptionPane.showConfirmDialog(
            frame, panel, "Add New Device",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        String deviceName = name.getText().trim();
        String deviceIP = ip.getText().trim();

        if (deviceName.isEmpty() || deviceIP.isEmpty()) {
            showError("Please enter all device details.");
            return;
        }

        if (!validIP(deviceIP)) {
            showError("Enter a valid IPv4 address.");
            return;
        }

        if (duplicateIP(deviceIP, -1)) {
            showError("This IP address is already registered.");
            return;
        }

        if (deviceName.contains("|") || deviceName.contains("\n")) {
            showError("Device name contains invalid characters.");
            return;
        }

        model.addRow(new Object[]{
            deviceName, type.getSelectedItem(),
            deviceIP, "Online"
        });

        saveDevices();
        updateStatistics();
    }

    // ================= EDIT DEVICE =================

    static void editDevice() {

        int row = selectedRow();

        if (row == -1) return;

        JTextField name = new JTextField(
            model.getValueAt(row, 0).toString()
        );

        JComboBox<String> type = new JComboBox<>(
            new String[]{"Router", "Switch", "Computer", "Server"}
        );

        type.setSelectedItem(model.getValueAt(row, 1));

        JTextField ip = new JTextField(
            model.getValueAt(row, 2).toString()
        );

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        panel.add(new JLabel("Device Name:"));
        panel.add(name);

        panel.add(new JLabel("Device Type:"));
        panel.add(type);

        panel.add(new JLabel("IP Address:"));
        panel.add(ip);

        int result = JOptionPane.showConfirmDialog(
            frame, panel, "Edit Device",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        String newName = name.getText().trim();
        String newIP = ip.getText().trim();

        if (newName.isEmpty() || newIP.isEmpty()
                || newName.contains("|") || newName.contains("\n")) {
            showError("Enter valid device details.");
            return;
        }

        if (!validIP(newIP)) {
            showError("Enter a valid IPv4 address.");
            return;
        }

        if (duplicateIP(newIP, row)) {
            showError("This IP address is already registered.");
            return;
        }

        model.setValueAt(newName, row, 0);
        model.setValueAt(type.getSelectedItem(), row, 1);
        model.setValueAt(newIP, row, 2);

        saveDevices();
        updateStatistics();
    }

    // ================= REMOVE DEVICE =================

    static void removeDevice() {

        int row = selectedRow();

        if (row == -1) return;

        int confirm = JOptionPane.showConfirmDialog(
            frame,
            "Are you sure you want to remove this device?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {

            model.removeRow(row);

            saveDevices();
            updateStatistics();
        }
    }

    // ================= CHANGE STATUS =================

    static void changeStatus() {

        int row = selectedRow();

        if (row == -1) return;

        String current = model.getValueAt(row, 3).toString();

        String next = current.equals("Online")
            ? "Offline" : "Online";

        model.setValueAt(next, row, 3);

        saveDevices();
        updateStatistics();
    }

    // ================= SEARCH =================

    static void search() {

        String text = searchField.getText().trim();

        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(
                RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(text)
                )
            );
        }
    }

    // ================= VALIDATION =================

    static boolean validIP(String ip) {

        String[] parts = ip.split("\\.", -1);

        if (parts.length != 4) return false;

        try {
            for (String part : parts) {

                if (part.isEmpty()) return false;

                if (part.length() > 3) return false;

                int number = Integer.parseInt(part);

                if (number < 0 || number > 255) {
                    return false;
                }
            }

            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    static boolean duplicateIP(String ip, int excludedRow) {

        for (int i = 0; i < model.getRowCount(); i++) {

            if (i != excludedRow &&
                model.getValueAt(i, 2).toString().equals(ip)) {
                return true;
            }
        }

        return false;
    }

    static int selectedRow() {

        int selected = table.getSelectedRow();

        if (selected == -1) {
            showError("Please select a device from the table.");
            return -1;
        }

        return table.convertRowIndexToModel(selected);
    }

    static void showError(String message) {

        JOptionPane.showMessageDialog(
            frame, message, "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // ================= SAVE DATA =================

    static void saveDevices() {

        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(FILE_PATH))) {

            for (int i = 0; i < model.getRowCount(); i++) {

                writer.write(
                    model.getValueAt(i, 0) + "|" +
                    model.getValueAt(i, 1) + "|" +
                    model.getValueAt(i, 2) + "|" +
                    model.getValueAt(i, 3)
                );

                writer.newLine();
            }

        } catch (IOException e) {
            showError("Unable to save device data.");
        }
    }

    // ================= LOAD DATA =================

    static void loadDevices() {

        Path path = Paths.get(FILE_PATH);

        if (!Files.exists(path)) return;

        try (BufferedReader reader = Files.newBufferedReader(path)) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split("\\|", -1);

                if (data.length == 4
                        && !data[0].isEmpty()
                        && validIP(data[2])
                        && (data[3].equals("Online")
                            || data[3].equals("Offline"))) {

                    model.addRow(data);
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null, "Unable to load saved device data."
            );
        }
    }
}
