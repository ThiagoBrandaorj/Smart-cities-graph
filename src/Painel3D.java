package src;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

// Visao 3D do grafo
public class Painel3D extends JPanel {

    Modelo m;
    double[][] pos = new double[0][3];

    double giroHorizontal = 0.6;
    double giroVertical = -0.35;
    double zoom = 1;
    double escala = 1;
    boolean girarSozinho = true;

    int mouseX, mouseY;
    boolean arrastando = false;

    public Painel3D(Modelo m) {
        this.m = m;
        setPreferredSize(new Dimension(760, 520));

        MouseAdapter mouse = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                arrastando = true;
            }

            public void mouseDragged(MouseEvent e) {
                giroHorizontal += (e.getX() - mouseX) * 0.01;
                giroVertical += (e.getY() - mouseY) * 0.01;
                giroVertical = Math.max(-1.5, Math.min(1.5, giroVertical));
                mouseX = e.getX();
                mouseY = e.getY();
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                arrastando = false;
            }

            public void mouseWheelMoved(MouseWheelEvent e) {
                zoom = zoom * Math.exp(-e.getPreciseWheelRotation() * 0.1);
                zoom = Math.max(0.4, Math.min(3, zoom));
                repaint();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(mouse);

        // giro automatico
        new Timer(30, e -> {
            if (isShowing() && girarSozinho && !arrastando) {
                giroHorizontal += 0.008;
                repaint();
            }
        }).start();

        calcularPosicoes();
    }

    // layout de forcas - O(V^2) por rodada
    void calcularPosicoes() {
        Grafo g = m.g;
        int n = g.numVertices();
        pos = new double[n][3];
        if (n == 0) return;

        // comeca da posicao 2D
        double cx = 0, cy = 0;
        for (int i = 0; i < n; i++) {
            cx += g.posicoes.get(i)[0];
            cy += g.posicoes.get(i)[1];
        }
        cx /= n;
        cy /= n;
        Random sorteio = new Random(7);
        for (int i = 0; i < n; i++) {
            pos[i][0] = (g.posicoes.get(i)[0] - cx) * 0.5;
            pos[i][1] = (g.posicoes.get(i)[1] - cy) * 0.5;
            pos[i][2] = (sorteio.nextDouble() - 0.5) * 140;
        }

        double distanciaIdeal = 95;
        double passoMaximo = 60;

        for (int rodada = 0; rodada < 250; rodada++) {
            double[][] forca = new double[n][3];

            // repulsao
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double[] d = diferenca(pos[i], pos[j]);
                    double dist = Math.max(tamanho(d), 0.01);
                    double f = distanciaIdeal * distanciaIdeal / (dist * dist);
                    for (int c = 0; c < 3; c++) {
                        forca[i][c] += d[c] * f;
                        forca[j][c] -= d[c] * f;
                    }
                }
            }

            // atracao
            for (int u = 0; u < n; u++) {
                for (Grafo.Aresta a : g.adj.get(u)) {
                    if (!g.mostrar(u, a)) continue;
                    int v = a.destino;
                    double[] d = diferenca(pos[u], pos[v]);
                    double f = tamanho(d) / distanciaIdeal;
                    for (int c = 0; c < 3; c++) {
                        forca[u][c] -= d[c] * f;
                        forca[v][c] += d[c] * f;
                    }
                }
            }

            // move os vertices
            for (int i = 0; i < n; i++) {
                for (int c = 0; c < 3; c++) forca[i][c] -= pos[i][c] * 0.05;
                double t = Math.max(tamanho(forca[i]), 0.000001);
                double fator = Math.min(t, passoMaximo) / t;
                for (int c = 0; c < 3; c++) pos[i][c] += forca[i][c] * fator;
            }
            passoMaximo *= 0.985;
        }

        // escala
        double maior = 0;
        for (int i = 0; i < n; i++) maior = Math.max(maior, tamanho(pos[i]));
        escala = maior > 0 ? Math.max(0.3, Math.min(3, 190 / maior)) : 1.5;
    }

    static double[] diferenca(double[] a, double[] b) {
        return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    static double tamanho(double[] d) {
        return Math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2]);
    }

    // gira e projeta na tela
    double[] projetar(double[] p, int largura, int altura) {
        double x = p[0] * Math.cos(giroHorizontal) + p[2] * Math.sin(giroHorizontal);
        double z = -p[0] * Math.sin(giroHorizontal) + p[2] * Math.cos(giroHorizontal);
        double y = p[1] * Math.cos(giroVertical) - z * Math.sin(giroVertical);
        z = p[1] * Math.sin(giroVertical) + z * Math.cos(giroVertical);
        // perspectiva
        double perspectiva = 520 / (520 + z);
        double s = perspectiva * escala * zoom;
        return new double[]{largura / 2.0 + x * s, altura / 2.0 + y * s, z, perspectiva};
    }

    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int largura = getWidth();
        int altura = getHeight();

        g.setColor(new Color(16, 38, 33));
        g.fillRect(0, 0, largura, altura);

        Grafo grafo = m.g;
        int n = Math.min(grafo.numVertices(), pos.length);
        double[][] tela = new double[n][];
        for (int i = 0; i < n; i++) tela[i] = projetar(pos[i], largura, altura);

        // arestas
        for (int u = 0; u < n; u++) {
            for (Grafo.Aresta a : grafo.adj.get(u)) {
                if (!grafo.mostrar(u, a) || a.destino >= n) continue;
                desenharAresta(g, tela[u], tela[a.destino], a, m.destacada(u, a.destino));
            }
        }

        // vertices do mais longe para o mais perto
        int[] ordem = new int[n];
        for (int i = 0; i < n; i++) {
            int j = i;
            while (j > 0 && tela[ordem[j - 1]][2] < tela[i][2]) {
                ordem[j] = ordem[j - 1];
                j--;
            }
            ordem[j] = i;
        }
        for (int k = 0; k < n; k++) {
            int i = ordem[k];
            desenharVertice(g, i, tela[i]);
        }

        g.setColor(new Color(255, 255, 255, 150));
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("arraste para girar - roda do mouse para zoom", 10, altura - 10);
    }

    void desenharAresta(Graphics2D g, double[] a, double[] b, Grafo.Aresta aresta, boolean destaque) {
        double perspectiva = (a[3] + b[3]) / 2;
        g.setColor(destaque ? new Color(53, 224, 161) : new Color(143, 176, 166));
        g.setStroke(new BasicStroke((float) ((destaque ? 4 : 1.8) * perspectiva)));
        g.drawLine((int) a[0], (int) a[1], (int) b[0], (int) b[1]);

        if (m.g.direcionado) {
            // seta
            double ang = Math.atan2(b[1] - a[1], b[0] - a[0]);
            double r = 20 * b[3];
            double px = b[0] - Math.cos(ang) * r;
            double py = b[1] - Math.sin(ang) * r;
            double h = 14 * perspectiva;
            int[] xs = {(int) px, (int) (px - h * Math.cos(ang - 0.4)), (int) (px - h * Math.cos(ang + 0.4))};
            int[] ys = {(int) py, (int) (py - h * Math.sin(ang - 0.4)), (int) (py - h * Math.sin(ang + 0.4))};
            g.fillPolygon(xs, ys, 3);
        }

        if (m.g.ponderado) {
            String texto = Algoritmos.formatar(aresta.peso);
            g.setFont(new Font("SansSerif", Font.BOLD, (int) Math.max(10, 13 * perspectiva)));
            g.setColor(destaque ? new Color(125, 255, 201) : Color.WHITE);
            Painel2D.textoCentralizado(g, texto, (a[0] + b[0]) / 2, (a[1] + b[1]) / 2);
        }
    }

    void desenharVertice(Graphics2D g, int i, double[] p) {
        String rotulo = m.rotulos.get(i);
        boolean destaque = rotulo != null;
        int r = (int) (20 * p[3] * Math.max(0.7, Math.min(1.4, zoom)));
        int x = (int) p[0];
        int y = (int) p[1];

        g.setColor(destaque ? new Color(31, 191, 135) : new Color(77, 120, 201));
        g.fillOval(x - r, y - r, 2 * r, 2 * r);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(x - r, y - r, 2 * r, 2 * r);

        g.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, (int) (r * 0.8))));
        Painel2D.textoCentralizado(g, m.g.nomes.get(i), x, y);

        if (destaque && !rotulo.isEmpty()) {
            g.setColor(new Color(125, 255, 201));
            g.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, (int) (r * 0.65))));
            Painel2D.textoCentralizado(g, rotulo, x, y - r - 10);
        }
    }
}