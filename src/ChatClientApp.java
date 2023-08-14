import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

public class ChatClientApp extends UnicastRemoteObject implements ChatClient {
    private ChatService chatService;
    private List<ChatClient> connectedClients;
    private JTextArea chatTextArea;
    private JTextField messageTextField;
    private JList<String> clientList;
    private String clientNombre;

    protected ChatClientApp(String nombreCliente) throws RemoteException {
        super();
        clientNombre = nombreCliente;
        createGUI();
        connectToChatService();
    }

    private void createGUI() {
        JFrame frame = new JFrame("Chat RMI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel chatPanel = new JPanel(new BorderLayout());

        chatTextArea = new JTextArea(10, 40);
        chatTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatTextArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messageTextField = new JTextField(30);
        JButton sendButton = new JButton("Enviar");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        messagePanel.add(messageTextField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        JPanel clientPanel = new JPanel(new BorderLayout());
        DefaultListModel<String> clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane clientScrollPane = new JScrollPane(clientList);
        clientPanel.add(clientScrollPane, BorderLayout.CENTER);

        JButton updateButton = new JButton("Actualizar");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateClientList();
            }
        });
        clientPanel.add(updateButton, BorderLayout.SOUTH);

        JButton publicButton = new JButton("Enviar a todos");
        publicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendPublicMessage();
            }
        });
        messagePanel.add(publicButton, BorderLayout.WEST);

        JButton matrixButton = new JButton("Resolver matriz");
        matrixButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solveMatrix();
            }
        });
        messagePanel.add(matrixButton, BorderLayout.NORTH);

        frame.getContentPane().add(chatPanel, BorderLayout.CENTER);
        frame.getContentPane().add(messagePanel, BorderLayout.SOUTH);
        frame.getContentPane().add(clientPanel, BorderLayout.EAST);

        frame.pack();
        frame.setVisible(true);
    }

    private void connectToChatService() {
        try {
            // Modifica la dirección IP para apuntar al servidor RMI
            String serverIP = "192.168.1.102";
            Registry registry = LocateRegistry.getRegistry(serverIP, 6001);
            chatService = (ChatService) registry.lookup("ChatService");
            chatService.registerClient(this);
            connectedClients = chatService.getConnectedClients();
            updateClientList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateClientList() {
        DefaultListModel<String> clientListModel = new DefaultListModel<>();
        for (ChatClient client : connectedClients) {
            if (client != this) {
                try {
                    String clientNombre = client.getClientNombre();
                    clientListModel.addElement(clientNombre);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        clientList.setModel(clientListModel);
    }

    private void sendMessage() {
        try {
            String message = messageTextField.getText();
            ChatClient selectedClient = getSelectedClient();
            if (selectedClient != null) {
                selectedClient.receiveMessage("DM de " + clientNombre + ": " + message);
            } else {
                chatService.broadcastMessage("Mensaje de " + clientNombre + ": " + message);
            }
            messageTextField.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPublicMessage() {
        try {
            String message = messageTextField.getText();
            chatService.broadcastMessage("Mensaje para todos " + clientNombre + ": " + message);
            messageTextField.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ChatClient getSelectedClient() {
        int selectedIndex = clientList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < connectedClients.size()) {
            return connectedClients.get(selectedIndex);
        }
        return null;
    }

    @Override
    public void receiveMessage(String message) throws RemoteException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chatTextArea.append("" + message + "\n");
            }
        });
    }

    @Override
    public void updateClientList(List<ChatClient> clients) throws RemoteException {
        connectedClients = clients;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateClientList();
            }
        });
    }

    private void solveMatrix() {
        String sizeStr = JOptionPane.showInputDialog("Ingrese el tamaño de la matriz cuadrada:");
        int size = Integer.parseInt(sizeStr);

        String numThreadsStr = JOptionPane.showInputDialog("Ingrese el número de hilos a utilizar:");
        int numThreads = Integer.parseInt(numThreadsStr);

        int[][] matrixA = generateRandomMatrix(size);
        int[][] matrixB = generateRandomMatrix(size);

        long startTimeSeq = System.currentTimeMillis();
        int[][] resultSeq = multiplyMatrixSequential(matrixA, matrixB);
        long endTimeSeq = System.currentTimeMillis();
        double timeSecSeq = (endTimeSeq - startTimeSeq) / 1000.0;

        long startTimeConcurrent = System.currentTimeMillis();
        int[][] resultConcurrent = multiplyMatrixConcurrent(matrixA, matrixB, numThreads);
        long endTimeConcurrent = System.currentTimeMillis();
        double timeSecConcurrent = (endTimeConcurrent - startTimeConcurrent) / 1000.0;

        long startTimeParallel = System.currentTimeMillis();
        int[][] resultParallel = multiplyMatrixParallel(matrixA, matrixB, numThreads);
        long endTimeParallel = System.currentTimeMillis();
        double timeSecParallel = (endTimeParallel - startTimeParallel) / 1000.0;

        StringBuilder resultStr = new StringBuilder();
        resultStr.append("Resultado de la multiplicación de matrices:\n\n");
        resultStr.append("Secuencial:\n");
        resultStr.append("Tiempo: " + timeSecSeq + " segundos\n");
        resultStr.append("Matriz Resultante:\n");
        resultStr.append(matrixToString(resultSeq));
        resultStr.append("\n");

        resultStr.append("Concurrente:\n");
        resultStr.append("Tiempo: " + timeSecConcurrent + " segundos\n");
        resultStr.append("Matriz Resultante:\n");
        resultStr.append(matrixToString(resultConcurrent));
        resultStr.append("\n");

        resultStr.append("Paralelo:\n");
        resultStr.append("Tiempo: " + timeSecParallel + " segundos\n");
        resultStr.append("Matriz Resultante:\n");
        resultStr.append(matrixToString(resultParallel));

        JOptionPane.showMessageDialog(null, resultStr.toString(), "Resultado de la multiplicación de matrices", JOptionPane.INFORMATION_MESSAGE);
    }

    private int[][] generateRandomMatrix(int size) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = (int) (Math.random() * 10);
            }
        }
        return matrix;
    }

    private int[][] multiplyMatrixSequential(int[][] matrixA, int[][] matrixB) {
        int size = matrixA.length;
        int[][] result = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    result[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }
        return result;
    }

    private int[][] multiplyMatrixConcurrent(int[][] matrixA, int[][] matrixB, int numThreads) {
        int size = matrixA.length;
        int[][] result = new int[size][size];
        List<Thread> threads = new ArrayList<>();

        int rowsPerThread = size / numThreads;
        int remainingRows = size % numThreads;

        int startRow = 0;
        int endRow = rowsPerThread - 1;

        for (int i = 0; i < numThreads; i++) {
            if (i == numThreads - 1) {
                endRow += remainingRows;
            }

            int finalStartRow = startRow;
            int finalEndRow = endRow;

            Thread thread = new Thread(() -> {
                for (int row = finalStartRow; row <= finalEndRow; row++) {
                    for (int col = 0; col < size; col++) {
                        for (int k = 0; k < size; k++) {
                            result[row][col] += matrixA[row][k] * matrixB[k][col];
                        }
                    }
                }
            });

            thread.start();
            threads.add(thread);

            startRow = endRow + 1;
            endRow = startRow + rowsPerThread - 1;
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private int[][] multiplyMatrixParallel(int[][] matrixA, int[][] matrixB, int numThreads) {
        int size = matrixA.length;
        int[][] result = new int[size][size];
        List<Thread> threads = new ArrayList<>();

        int rowsPerThread = size / numThreads;
        int remainingRows = size % numThreads;

        int startRow = 0;
        int endRow = rowsPerThread - 1;

        for (int i = 0; i < numThreads; i++) {
            if (i == numThreads - 1) {
                endRow += remainingRows;
            }

            int finalStartRow = startRow;
            int finalEndRow = endRow;

            Thread thread = new Thread(() -> {
                for (int row = finalStartRow; row <= finalEndRow; row++) {
                    for (int col = 0; col < size; col++) {
                        for (int k = 0; k < size; k++) {
                            result[row][col] += matrixA[row][k] * matrixB[k][col];
                        }
                    }
                }
            });

            thread.start();
            threads.add(thread);

            startRow = endRow + 1;
            endRow = startRow + rowsPerThread - 1;
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private String matrixToString(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                sb.append(matrix[i][j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String getClientNombre() {
        return clientNombre;
    }

    @Override
    public void solveMatrix(int[][] matrix) throws RemoteException {

    }

    @Override
    public int getConnectedClientsCount() throws RemoteException {
        return 0;
    }

    public static void main(String[] args) {
        String nombreCliente = JOptionPane.showInputDialog("Ingrese su nombre de usuario:");
        try {
            ChatClientApp clientApp = new ChatClientApp(nombreCliente);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}