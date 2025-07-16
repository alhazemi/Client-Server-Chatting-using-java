package com.chat.view;

import com.chat.controller.ClientController;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import java.io.File;

public class ClientUI extends JFrame {

    private JTextPane chatArea;
    private JTextArea messageArea;
    private JButton sendButton;
    private JButton emojiButton;
    private JButton recordButton;
    private JButton imageButton;
    private JTextField usernameField;
    private JTextField receiverField;

    private JDialog emojiDialog;
    private ClientController controller;

    private final String[] emojis = {
        "😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆", "😉", "😊",
        "😋", "😎", "😍", "😘", "🥰", "😗", "😙", "😚", "🙂", "🤗",
        "🤩", "🤔", "🤨", "😐", "😑", "😶", "🙄", "😏", "😣", "😥",
        "😮", "🤐", "😯", "😪", "😫", "😴", "😌", "😛", "😜", "😝"
    };

    public ClientUI() {
        setTitle("Client Chat");
        setSize(520, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top Panel for Username and Receiver
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        topPanel.add(new JLabel("Your Username:"));
        usernameField = new JTextField();
        topPanel.add(usernameField);

        topPanel.add(new JLabel("Receiver Username:"));
        receiverField = new JTextField();
        topPanel.add(receiverField);

        add(topPanel, BorderLayout.NORTH);

        // Chat Area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setContentType("text/html");
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // Input Panel (Message + Buttons)
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        messageArea = new JTextArea(3, 30);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane msgScroll = new JScrollPane(messageArea);
        inputPanel.add(msgScroll, BorderLayout.CENTER);

        // Button size settings
        Dimension btnSize = new Dimension(60, 35);

        // Emoji button
        emojiButton = new JButton("😊");
        emojiButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        emojiButton.setBackground(Color.YELLOW);
        emojiButton.setFocusPainted(false);
        emojiButton.setPreferredSize(btnSize);
        emojiButton.addActionListener(e -> {
            String emoji = showEmojiPanel();
            if (emoji != null) {
                messageArea.append(emoji);
            }
        });

        // Image button
        imageButton = new JButton("📷");
        imageButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        imageButton.setBackground(Color.PINK);
        imageButton.setFocusPainted(false);
        imageButton.setPreferredSize(btnSize);
        imageButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int option = chooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                try {
                    File selectedFile = chooser.getSelectedFile();
                    byte[] imageBytes = java.nio.file.Files.readAllBytes(selectedFile.toPath());
                    controller.sendImage(imageBytes, receiverField.getText());
                } catch (Exception ex) {
                    appendMessage("Error reading image file: " + ex.getMessage());
                }
            }
        });

        // Record button
        recordButton = new JButton("🎙️");
        recordButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        recordButton.setBackground(Color.GREEN.darker());
        recordButton.setForeground(Color.WHITE);
        recordButton.setFocusPainted(false);
        recordButton.setPreferredSize(btnSize);
        recordButton.addActionListener(e -> controller.recordAndSendAudio(receiverField.getText()));

        // Send button
        sendButton = new JButton("➤");
        sendButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        sendButton.setBackground(new Color(0, 122, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setPreferredSize(btnSize);
        sendButton.addActionListener(e -> {
            String msg = messageArea.getText().trim();
            if (!msg.isEmpty()) {
                controller.sendMessage(msg, receiverField.getText());
                messageArea.setText("");
            }
        });

        // Arrange buttons in a row
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        buttonPanel.add(emojiButton);
        buttonPanel.add(imageButton);
        buttonPanel.add(recordButton);
        buttonPanel.add(sendButton);

        inputPanel.add(buttonPanel, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        controller = new ClientController(this);

        // Add action listener to username field to connect to server
        usernameField.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                controller.connectToServer(username);
                usernameField.setEditable(false); // Disable editing after connecting
            }
        });

        setVisible(true);
    }

    // Emoji selection window
    private String showEmojiPanel() {
        JDialog emojiDialog = new JDialog(this, "Select Emoji", true);
        emojiDialog.setUndecorated(true);
        emojiDialog.setSize(300, 300);
        emojiDialog.setLocationRelativeTo(emojiButton);

        JPanel panel = new JPanel(new GridLayout(5, 8, 4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final String[] selectedEmoji = {null};

        for (String emoji : emojis) {
            JButton btn = new JButton(emoji);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            btn.setMargin(new Insets(2, 2, 2, 2));
            btn.addActionListener(e -> {
                selectedEmoji[0] = emoji;
                emojiDialog.dispose();
            });
            panel.add(btn);
        }

        emojiDialog.add(new JScrollPane(panel));
        emojiDialog.setVisible(true);
        return selectedEmoji[0];
    }

    // Display text message in chat area
    public void appendMessage(String msg) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            doc.insertString(doc.getLength(), msg + "\n\n", null);
            chatArea.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Display image in chat area
    public void appendImage(byte[] imageBytes, String sender) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (img != null) {
                Image scaledImg = img.getScaledInstance(200, -1, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImg);

                StyledDocument doc = chatArea.getStyledDocument();
                doc.insertString(doc.getLength(), sender + ": ", null);
                Style style = chatArea.addStyle("ImageStyle", null);
                StyleConstants.setIcon(style, icon);

                doc.insertString(doc.getLength(), "ignored text", style);
                doc.insertString(doc.getLength(), "\n\n", null);
                chatArea.setCaretPosition(doc.getLength());
            } else {
                appendMessage("[Error displaying image]");
            }
        } catch (Exception e) {
            appendMessage("[Error displaying image: " + e.getMessage() + "]");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientUI().setVisible(true));
    }
}


