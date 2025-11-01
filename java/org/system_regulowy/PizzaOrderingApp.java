package org.system_regulowy;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class PizzaOrderingApp {
    private JFrame frame;
    private JPanel pizzasListPanel;
    private JLabel totalLabel;
    private JTextArea orderSummary;
    private JTextArea consoleOutput;
    private Order order;
    private ByteArrayOutputStream consoleBuffer;

    public PizzaOrderingApp() {
        order = new Order();

        frame = new JFrame("Zamawianie Pizzy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 850);

        redirectSystemOut();

        JPanel selectionPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Wybór pizzy"));

        String[] pizzaTypes = {"Margherita", "Pepperoni", "Hawajska", "Wege"};
        double[] basePrices = {25.0, 30.0, 28.0, 27.0};
        JComboBox<String> pizzaComboBox = new JComboBox<>(pizzaTypes);

        String[] sizes = {"32 cm", "40 cm", "50 cm"};
        JComboBox<String> sizeComboBox = new JComboBox<>(sizes);

        JButton addButton = new JButton("Dodaj do zamówienia");

        selectionPanel.add(new JLabel("Rodzaj pizzy:"));
        selectionPanel.add(pizzaComboBox);
        selectionPanel.add(new JLabel("Rozmiar:"));
        selectionPanel.add(sizeComboBox);
        selectionPanel.add(new JLabel());
        selectionPanel.add(addButton);

        pizzasListPanel = new JPanel();
        pizzasListPanel.setLayout(new BoxLayout(pizzasListPanel, BoxLayout.Y_AXIS));
        pizzasListPanel.setBorder(BorderFactory.createTitledBorder("Zamówione pizze"));

        orderSummary = new JTextArea(7, 50);
        orderSummary.setEditable(false);
        orderSummary.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane summaryScrollPane = new JScrollPane(orderSummary);
        summaryScrollPane.setBorder(BorderFactory.createTitledBorder("Podsumowanie zamówienia"));

        totalLabel = new JLabel("Do zapłaty: 0.00 zł");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton calculateButton = new JButton("Przelicz");
        JButton orderButton = new JButton("Zamów");
        orderButton.setEnabled(false);

        JButton clearButton = new JButton("Wyczyść");

        addButton.addActionListener(e -> {
            int pizzaIndex = pizzaComboBox.getSelectedIndex();
            String name = pizzaTypes[pizzaIndex];
            double basePrice = basePrices[pizzaIndex];

            int sizeIndex = sizeComboBox.getSelectedIndex();
            int sizeCm;
            double multiplier;
            switch (sizeIndex) {
                case 1 -> {
                    sizeCm = 40;
                    multiplier = 1.3;
                }
                case 2 -> {
                    sizeCm = 50;
                    multiplier = 1.6;
                }
                default -> {
                    sizeCm = 32;
                    multiplier = 1.0;
                }
            }
            double finalPrice = basePrice * multiplier;
            String pizzaName = name + " (" + sizeCm + " cm)";
            order.addPizza(new Pizza(pizzaName, finalPrice));
            updatePizzasListPanel();
            updateOrderSummaryTextNoPromotion();
            orderButton.setEnabled(false);
        });

        calculateButton.addActionListener(e -> {
            if (order.getPizzas().isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Proszę dodać przynajmniej jedną pizzę do zamówienia.",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            order.getNotes().clear();
            order.calculateInitialTotal();

            order.getPizzas().forEach(p -> p.setUsedInPromotion(false));

            DroolsEngine.applyRules(order);

            updateOrderSummaryTextWithPromotion();
            totalLabel.setText("Do zapłaty: " + String.format("%.2f", order.getTotal()) + " zł");

            orderButton.setEnabled(true);
        });

        orderButton.addActionListener(e -> {
            if (order.getPizzas().isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Nie można złożyć pustego zamówienia.",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(frame,
                    "Dziękujemy za zamówienie!\nKwota do zapłaty: " +
                            String.format("%.2f", order.getTotal()) + " zł",
                    "Zamówienie złożone",
                    JOptionPane.INFORMATION_MESSAGE);

            order = new Order();
            updatePizzasListPanel();
            updateOrderSummaryTextNoPromotion();
            orderButton.setEnabled(false);
        });

        clearButton.addActionListener(e -> {
            if (consoleBuffer != null) {
                consoleBuffer.reset();
            }
            consoleOutput.setText("");

            order = new Order();
            updatePizzasListPanel();
            updateOrderSummaryTextNoPromotion();
            orderButton.setEnabled(false);
        });

        JPanel bottomButtons = new JPanel();
        bottomButtons.add(calculateButton);
        bottomButtons.add(orderButton);
        bottomButtons.add(clearButton);

        consoleOutput = new JTextArea();
        consoleOutput.setEditable(false);
        consoleOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane consoleScrollPane = new JScrollPane(consoleOutput);
        consoleScrollPane.setBorder(BorderFactory.createTitledBorder("Konsola systemu reguł"));
        consoleScrollPane.setPreferredSize(new Dimension(600, 200));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(selectionPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(pizzasListPanel), BorderLayout.CENTER);
        mainPanel.add(summaryScrollPane, BorderLayout.SOUTH);

        JPanel fullBottom = new JPanel(new BorderLayout());
        fullBottom.add(bottomButtons, BorderLayout.NORTH);
        fullBottom.add(consoleScrollPane, BorderLayout.CENTER);

        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        frame.getContentPane().add(fullBottom, BorderLayout.SOUTH);

        Timer consoleUpdateTimer = new Timer(1000, e -> consoleOutput.setText(getConsoleOutput()));
        consoleUpdateTimer.start();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void updatePizzasListPanel() {
        pizzasListPanel.removeAll();

        if (order.getPizzas().isEmpty()) {
            JLabel emptyLabel = new JLabel("Koszyk jest pusty.");
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            pizzasListPanel.add(emptyLabel);
        } else {
            for (int i = 0; i < order.getPizzas().size(); i++) {
                Pizza p = order.getPizzas().get(i);

                JPanel pizzaPanel = new JPanel(new BorderLayout(5, 5));
                pizzaPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
                pizzaPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                pizzaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel pizzaLabel = new JLabel(p.getName() + " - " + String.format("%.2f", p.getPrice()) + " zł");
                pizzaLabel.setPreferredSize(new Dimension(400, 30));
                pizzaLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                JButton deleteButton = new JButton("X");
                deleteButton.setMargin(new Insets(2, 8, 2, 8));
                deleteButton.setPreferredSize(new Dimension(50, 30));
                int index = i;
                deleteButton.addActionListener(e -> {
                    order.getPizzas().remove(index);
                    updatePizzasListPanel();
                    updateOrderSummaryTextNoPromotion();
                });

                pizzaPanel.add(pizzaLabel, BorderLayout.CENTER);
                pizzaPanel.add(deleteButton, BorderLayout.EAST);

                pizzasListPanel.add(pizzaPanel);
                pizzasListPanel.add(Box.createRigidArea(new Dimension(0,5)));
            }
        }

        pizzasListPanel.revalidate();
        pizzasListPanel.repaint();
    }

    private void updateOrderSummaryTextNoPromotion() {
        if (order.getPizzas().isEmpty()) {
            orderSummary.setText("Koszyk jest pusty.");
            totalLabel.setText("Do zapłaty: 0.00 zł");
        } else {
            order.calculateInitialTotal();
            StringBuilder sb = new StringBuilder();
            sb.append("Suma (przed promocjami): ").append(String.format("%.2f", order.getTotal())).append(" zł");
            orderSummary.setText(sb.toString());
            totalLabel.setText("Do zapłaty: " + String.format("%.2f", order.getTotal()) + " zł");
        }
    }

    private void updateOrderSummaryTextWithPromotion() {
        StringBuilder sb = new StringBuilder("Podsumowanie zamówienia:\n");

        sb.append("Suma (przed promocjami): ").append(String.format("%.2f", order.getInitialTotal())).append(" zł\n");
        sb.append("Suma (po promocjach): ").append(String.format("%.2f", order.getTotal())).append(" zł");

        if (!order.getNotes().isEmpty()) {
            sb.append("\n\nZastosowane promocje:\n").append(String.join("\n", order.getNotes()));
        }

        orderSummary.setText(sb.toString());
    }

    private void redirectSystemOut() {
        consoleBuffer = new ByteArrayOutputStream();
        PrintStream guiStream = new PrintStream(consoleBuffer, true);
        PrintStream originalOut = System.out;
        TeeOutputStream tee = new TeeOutputStream(originalOut, guiStream);
        System.setOut(new PrintStream(tee, true));
    }

    private String getConsoleOutput() {
        return consoleBuffer != null ? consoleBuffer.toString() : "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PizzaOrderingApp::new);
    }
}