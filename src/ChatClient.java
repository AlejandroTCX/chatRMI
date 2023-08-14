import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatClient extends Remote {
    void receiveMessage(String message) throws RemoteException;

    void updateClientList(List<ChatClient> clients) throws RemoteException;

    String getClientNombre() throws RemoteException;
    void solveMatrix(int[][] matrix) throws RemoteException;

    int getConnectedClientsCount() throws RemoteException;
}