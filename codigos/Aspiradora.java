import java.util.*;

public class Aspiradora {

    private int bolsa;
    private final int capacidadMaximaBolsa = 100;
    private Bateria bateriaObj;
    private int posX, posY;
    private VistaConsola vista;

    public Aspiradora(Bateria bateria, VistaConsola vista) {
        this.bateriaObj = bateria;
        this.vista = vista;
        this.bolsa = 0;
        this.posX = 0;
        this.posY = 0;
    }

    public void actuar(Superficie superficie) {
        if (bateriaObj.getCarga() <= 1 || bolsa >= capacidadMaximaBolsa) {
            if (bolsa >= capacidadMaximaBolsa) {
                vista.mostrarBolsaLlena();
            } else {
                vista.mostrarBateriaBaja();
            }
            buscarYRecargar(superficie);
            return;
        }

        moverInteligente(superficie);
        Zona zonaActual = superficie.obtenerZona(posX, posY);
        limpiar(zonaActual);
    }

    private void moverInteligente(Superficie superficie) {
        int maxNivel = -1;
        int objetivoX = posX;
        int objetivoY = posY;

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 25; x++) {
                Zona zona = superficie.obtenerZona(x, y);
                if (zona != null && !zona.tieneMueble() && zona.getNivelSuciedad() > maxNivel
                        && !(zona instanceof ZonaRecarga)) {
                    maxNivel = zona.getNivelSuciedad();
                    objetivoX = x;
                    objetivoY = y;
                }
            }
        }

        if (maxNivel == 0)
            return;

        List<int[]> ruta = buscarRuta(superficie, objetivoX, objetivoY);
        if (ruta != null && !ruta.isEmpty()) {
            int[] siguientePaso = ruta.get(0);
            posX = siguientePaso[0];
            posY = siguientePaso[1];
        } else {
            vista.mostrarZonaSuciaInaccesible();
        }
    }

    private List<int[]> buscarRuta(Superficie superficie, int destinoX, int destinoY) {
        boolean[][] visitado = new boolean[10][25];
        int[][][] padre = new int[10][25][2];
        Queue<int[]> cola = new LinkedList<>();
        cola.add(new int[] { posX, posY });
        visitado[posY][posX] = true;

        int[][] direcciones = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

        while (!cola.isEmpty()) {
            int[] actual = cola.poll();
            int x = actual[0];
            int y = actual[1];

            if (x == destinoX && y == destinoY)
                break;

            for (int[] d : direcciones) {
                int nx = x + d[0];
                int ny = y + d[1];
                Zona z = superficie.obtenerZona(nx, ny);

                if (z != null && !visitado[ny][nx] && !z.tieneMueble()) {
                    visitado[ny][nx] = true;
                    padre[ny][nx][0] = x;
                    padre[ny][nx][1] = y;
                    cola.add(new int[] { nx, ny });
                }
            }
        }

        if (!visitado[destinoY][destinoX])
            return null;

        LinkedList<int[]> camino = new LinkedList<>();
        int cx = destinoX;
        int cy = destinoY;

        while (cx != posX || cy != posY) {
            camino.addFirst(new int[] { cx, cy });
            int px = padre[cy][cx][0];
            int py = padre[cy][cx][1];
            cx = px;
            cy = py;
        }

        return camino;
    }

    private void buscarYRecargar(Superficie superficie) {
        int destinoX = -1, destinoY = -1;
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 25; x++) {
                Zona zona = superficie.obtenerZona(x, y);
                if (zona instanceof ZonaRecarga) {
                    destinoX = x;
                    destinoY = y;
                    break;
                }
            }
            if (destinoX != -1)
                break;
        }

        if (destinoX == -1) {
            vista.mostrarZonaRecargaNoEncontrada();
            return;
        }

        List<int[]> ruta = buscarRuta(superficie, destinoX, destinoY);
        if (ruta != null && !ruta.isEmpty()) {
            int[] siguientePaso = ruta.get(0);
            posX = siguientePaso[0];
            posY = siguientePaso[1];

            Zona zonaActual = superficie.obtenerZona(posX, posY);
            if (zonaActual instanceof ZonaRecarga) {
                ((ZonaRecarga) zonaActual).recargar(this);
                if (bolsa == capacidadMaximaBolsa) {
                    vaciarBolsa();
                }
            }
        } else {
            vista.mostrarZonaRecargaInaccesible();
        }
    }

    public void vaciarBolsa() {
        vista.mostrarVaciadoBolsa();
        bolsa = 0;
    }

    public void limpiar(Zona x) {
        if (x == null) {
            return;
        }

        if (x.getNivelSuciedad() > 0) {
            if (bateriaObj.getCarga() > 0) {
                x.limpiar();
                bolsa++;
                consumirBateria();

                if (bolsa == capacidadMaximaBolsa) {
                    vista.mostrarBolsaLlenaDuranteLimpieza();
                }
            } else {
                vista.mostrarBateriaAgotada();
            }
        } else {
            vista.mostrarZonaLimpia();
        }
    }

    public void consumirBateria() {
        bateriaObj.consumir();
    }

    public void recargarBateria() {
        bateriaObj.recargar();
    }

    public int obtenerBolsa() {
        return bolsa;
    }

    public int obtenerCargaBateria() {
        return bateriaObj.getCarga();
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }
}
