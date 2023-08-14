public class MatrixSolver {
    public static int[][] solve(int[][] matrix) {
        int n = matrix.length;
        int[][] augmentedMatrix = augmentMatrix(matrix);

        for (int i = 0; i < n; i++) {
            if (augmentedMatrix[i][i] == 0) {
                // Si el pivote es cero, intercambiar filas
                if (!swapRows(augmentedMatrix, i)) {
                    throw new IllegalArgumentException("La matriz no se puede resolver");
                }
            }

            // Hacer ceros debajo del pivote
            for (int j = i + 1; j < n; j++) {
                int factor = augmentedMatrix[j][i] / augmentedMatrix[i][i];
                for (int k = i; k < n + 1; k++) {
                    augmentedMatrix[j][k] -= factor * augmentedMatrix[i][k];
                }
            }
        }

        // Resolver la matriz triangular superior
        int[][] result = new int[n][1];
        for (int i = n - 1; i >= 0; i--) {
            int sum = 0;
            for (int j = i + 1; j < n; j++) {
                sum += augmentedMatrix[i][j] * result[j][0];
            }
            result[i][0] = (augmentedMatrix[i][n] - sum) / augmentedMatrix[i][i];
        }

        return result;
    }

    private static int[][] augmentMatrix(int[][] matrix) {
        int n = matrix.length;
        int[][] augmentedMatrix = new int[n][n + 1];
        for (int i = 0; i < n; i++) {
            System.arraycopy(matrix[i], 0, augmentedMatrix[i], 0, n);
            augmentedMatrix[i][n] = 0; // Columna adicional para almacenar el resultado
        }
        return augmentedMatrix;
    }

    private static boolean swapRows(int[][] matrix, int row) {
        int n = matrix.length;
        for (int i = row + 1; i < n; i++) {
            if (matrix[i][row] != 0) {
                int[] temp = matrix[i];
                matrix[i] = matrix[row];
                matrix[row] = temp;
                return true;
            }
        }
        return false;
    }
}
