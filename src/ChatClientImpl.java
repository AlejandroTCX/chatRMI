import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ChatClientImpl extends UnicastRemoteObject implements ChatClient {
    private String clientNombre;

    public ChatClientImpl(String nombreCliente) throws RemoteException {
        super();
        clientNombre = nombreCliente;
    }

    @Override
    public void receiveMessage(String message) throws RemoteException {
        System.out.println("Mensaje recibido: " + message);
        // Puedes agregar aquí la lógica para mostrar el mensaje en la interfaz gráfica
    }

    @Override
    public void updateClientList(List<ChatClient> clients) throws RemoteException {

    }

    @Override
    public String getClientNombre() throws RemoteException {
        return clientNombre;
    }
    @Override
    public void solveMatrix(int[][] matrix) throws RemoteException {
        // Resolver la matriz y mostrar el resultado en la consola
        int[][] result = MatrixSolver.solve(matrix);
        System.out.println("Matriz resuelta: ");
        printMatrix(result);
    }

    @Override
    public int getConnectedClientsCount() throws RemoteException {
        return 0;
    }

    private void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int num : row) {
                System.out.print(num + " ");
            }
            System.out.println();
        }
    }

}