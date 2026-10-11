import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SimuladorBanquero {

    static class Banquero {
        final int numProcesos;
        final int numRecursos;
        final String[] nombreRecurso;
        int[] disponible;
        int[][] asignado;
        final int[][] maximo;

        Banquero(String[] nombreRecurso, int[] disponible, int[][] asignado, int[][] maximo) {
            this.nombreRecurso = nombreRecurso;
            this.numRecursos = nombreRecurso.length;
            this.numProcesos = asignado.length;
            this.disponible = disponible.clone();
            this.asignado = copiaProfunda(asignado);
            this.maximo = copiaProfunda(maximo);
        }

        int necesidad(int proceso, int recurso) {
            return maximo[proceso][recurso] - asignado[proceso][recurso];
        }

        int[] vectorNecesidad(int proceso) {
            int[] v = new int[numRecursos];
            for (int r = 0; r < numRecursos; r++) {
                v[r] = necesidad(proceso, r);
            }
            return v;
        }

        List<Integer> probarSeguridad(int[] disp, int[][] asig, boolean explicar) {
            int[] trabajo = disp.clone();
            boolean[] fin = new boolean[numProcesos];
            List<Integer> orden = new ArrayList<>();

            if (explicar) {
                System.out.println("  Trabajo inicial = " + texto(trabajo));
            }

            boolean huboProgreso;
            do {
                huboProgreso = false;
                for (int p = 0; p < numProcesos; p++) {
                    if (fin[p]) {
                        continue;
                    }
                    int[] nec = new int[numRecursos];
                    for (int r = 0; r < numRecursos; r++) {
                        nec[r] = maximo[p][r] - asig[p][r];
                    }
                    if (cabe(nec, trabajo)) {
                        for (int r = 0; r < numRecursos; r++) {
                            trabajo[r] += asig[p][r];
                        }
                        fin[p] = true;
                        orden.add(p);
                        huboProgreso = true;
                        if (explicar) {
                            System.out.println("  P" + p + " termina (necesita " + texto(nec)
                                    + ") y devuelve sus recursos -> Trabajo = " + texto(trabajo));
                        }
                    }
                }
            } while (huboProgreso);

            if (explicar) {
                for (int p = 0; p < numProcesos; p++) {
                    if (!fin[p]) {
                        System.out.println("  P" + p + " queda bloqueado (necesita "
                                + texto(vectorNecesidad(p)) + ", Trabajo = " + texto(trabajo) + ")");
                    }
                }
            }
            return orden;
        }

        boolean esSeguro() {
            return probarSeguridad(disponible, asignado, false).size() == numProcesos;
        }

        void procesarSolicitud(int p, int[] pedido) {
            System.out.println("\nP" + p + " pide " + texto(pedido));

            if (!cabe(pedido, vectorNecesidad(p))) {
                System.out.println("-> ERROR: pide más de lo que declaró como máximo. Rechazada.");
                return;
            }
            if (!cabe(pedido, disponible)) {
                System.out.println("-> P" + p + " debe esperar: no alcanzan los recursos libres "
                        + texto(disponible) + ".");
                return;
            }

            int[] dispSim = disponible.clone();
            int[][] asigSim = copiaProfunda(asignado);
            for (int r = 0; r < numRecursos; r++) {
                dispSim[r] -= pedido[r];
                asigSim[p][r] += pedido[r];
            }

            System.out.println("Simulando la asignación y revisando seguridad:");
            List<Integer> orden = probarSeguridad(dispSim, asigSim, true);

            if (orden.size() == numProcesos) {
                disponible = dispSim;
                asignado = asigSim;
                System.out.println("-> CONCEDIDA. Orden seguro: " + formatoOrden(orden));
            } else {
                System.out.println("-> DENEGADA: el estado resultante sería inseguro. P" + p
                        + " espera y todo queda como estaba.");
            }
        }

        void liberar(int p) {
            for (int r = 0; r < numRecursos; r++) {
                disponible[r] += asignado[p][r];
                maximo[p][r] -= asignado[p][r];
                asignado[p][r] = 0;
            }
            System.out.println("P" + p + " devolvió sus recursos. Disponible = " + texto(disponible));
        }

        void imprimir() {
            System.out.println();
            System.out.print("Recursos: ");
            for (String n : nombreRecurso) {
                System.out.print(n + "  ");
            }
            System.out.println();
            System.out.println("+---------+--------------+--------------+--------------+");
            System.out.printf("| %-7s | %-12s | %-12s | %-12s |%n", "Proceso", "Asignado", "Máximo", "Necesidad");
            System.out.println("+---------+--------------+--------------+--------------+");
            for (int p = 0; p < numProcesos; p++) {
                System.out.printf("| %-7s | %-12s | %-12s | %-12s |%n", "P" + p,
                        texto(asignado[p]), texto(maximo[p]), texto(vectorNecesidad(p)));
            }
            System.out.println("+---------+--------------+--------------+--------------+");
            System.out.println("Disponible: " + texto(disponible));
        }
    }

    static boolean cabe(int[] a, int[] b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] > b[i]) {
                return false;
            }
        }
        return true;
    }

    static int[][] copiaProfunda(int[][] m) {
        int[][] c = new int[m.length][];
        for (int i = 0; i < m.length; i++) {
            c[i] = m[i].clone();
        }
        return c;
    }

    static String texto(int[] v) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < v.length; i++) {
            sb.append(i == 0 ? "" : ", ").append(v[i]);
        }
        return sb.append(")").toString();
    }

    static String formatoOrden(List<Integer> orden) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < orden.size(); i++) {
            sb.append(i == 0 ? "" : " , ").append("P").append(orden.get(i));
        }
        return "<" + sb + ">";
    }
-
    static final Scanner in = new Scanner(System.in);

    static int pedirNumero(String texto, int min, int max) {
        while (true) {
            System.out.print(texto);
            try {
                int v = Integer.parseInt(in.nextLine().trim());
                if (v >= min && v <= max) {
                    return v;
                }
            } catch (NumberFormatException ignorar) {
            }
            System.out.println("  Valor inválido, debe estar entre " + min + " y " + max + ".");
        }
    }

    static int[] pedirVector(String texto, int largo) {
        while (true) {
            System.out.print(texto);
            String[] t = in.nextLine().trim().split("\\s+");
            if (t.length == largo) {
                try {
                    int[] v = new int[largo];
                    boolean ok = true;
                    for (int i = 0; i < largo; i++) {
                        v[i] = Integer.parseInt(t[i]);
                        ok &= v[i] >= 0;
                    }
                    if (ok) {
                        return v;
                    }
                } catch (NumberFormatException ignorar) {

                }
            }
            System.out.println("  Escribe " + largo + " enteros >= 0 separados por espacio.");
        }
    }

    static String[] nombresPorDefecto(int cantidad) {
        String[] n = new String[cantidad];
        for (int i = 0; i < cantidad; i++) {
            n[i] = "R" + i;
        }
        return n;
    }

    static Banquero capturarDatos() {
        int np = pedirNumero("Cantidad de procesos (1-10): ", 1, 10);
        int nr = pedirNumero("Cantidad de tipos de recurso (1-10): ", 1, 10);
        int[] disp = pedirVector("Disponible inicial: ", nr);
        int[][] asig = new int[np][];
        int[][] max = new int[np][];
        for (int p = 0; p < np; p++) {
            asig[p] = pedirVector("P" + p + " asignado: ", nr);
            while (true) {
                max[p] = pedirVector("P" + p + " máximo:   ", nr);
                if (cabe(asig[p], max[p])) {
                    break;
                }
                System.out.println("  El máximo no puede ser menor que lo asignado.");
            }
        }
        return new Banquero(nombresPorDefecto(nr), disp, asig, max);
    }

    static Banquero ejemploSeguro() {
        return new Banquero(
                new String[] {"Cintas", "Plotters", "Impresoras", "CD-ROM"},
                new int[] {1, 0, 2, 0},
                new int[][] {{3, 0, 1, 1}, {0, 1, 0, 0}, {1, 1, 1, 0}, {1, 1, 0, 1}, {0, 0, 0, 0}},
                new int[][] {{4, 1, 1, 1}, {0, 2, 1, 2}, {4, 2, 1, 0}, {1, 1, 1, 1}, {2, 1, 1, 0}});
    }

    static Banquero ejemploInterbloqueo() {
        return new Banquero(
                new String[] {"X", "Y", "Z"},
                new int[] {0, 0, 0},
                new int[][] {{1, 0, 1}, {0, 1, 1}, {1, 1, 0}},
                new int[][] {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}});
    }

    public static void main(String[] args) {
        Banquero sistema = null;
        boolean salir = false;

        while (!salir) {
            System.out.println("\n===== ALGORITMO DEL BANQUERO =====");
            System.out.println(" [1] Ejemplo con estado seguro");
            System.out.println(" [2] Ejemplo con interbloqueo");
            System.out.println(" [3] Capturar mis propios datos");
            System.out.println(" [4] Ver estado del sistema");
            System.out.println(" [5] Revisar si el estado es seguro");
            System.out.println(" [6] Hacer una solicitud de recursos");
            System.out.println(" [7] Un proceso termina y libera recursos");
            System.out.println(" [0] Salir");
            int op = pedirNumero("Opción: ", 0, 7);

            if (op >= 4 && op <= 7 && sistema == null) {
                System.out.println("Primero carga datos con la opción 1, 2 o 3.");
                continue;
            }

            switch (op) {
                case 1:
                    sistema = ejemploSeguro();
                    sistema.imprimir();
                    break;
                case 2:
                    sistema = ejemploInterbloqueo();
                    sistema.imprimir();
                    break;
                case 3:
                    sistema = capturarDatos();
                    sistema.imprimir();
                    break;
                case 4:
                    sistema.imprimir();
                    break;
                case 5:
                    System.out.println("\nPrueba de seguridad:");
                    List<Integer> orden = sistema.probarSeguridad(sistema.disponible, sistema.asignado, true);
                    if (orden.size() == sistema.numProcesos) {
                        System.out.println("=> ESTADO SEGURO. Orden posible: " + formatoOrden(orden));
                    } else {
                        System.out.println("=> ESTADO INSEGURO: los procesos que no terminan están en riesgo de interbloqueo.");
                    }
                    break;
                case 6:
                    int p = pedirNumero("Proceso (0-" + (sistema.numProcesos - 1) + "): ", 0, sistema.numProcesos - 1);
                    int[] pedido = pedirVector("Cantidad solicitada de cada recurso: ", sistema.numRecursos);
                    sistema.procesarSolicitud(p, pedido);
                    break;
                case 7:
                    int q = pedirNumero("Proceso que termina (0-" + (sistema.numProcesos - 1) + "): ", 0, sistema.numProcesos - 1);
                    sistema.liberar(q);
                    break;
                default:
                    salir = true;
                    break;
            }
        }
        System.out.println("Fin del programa.");
    }
}
