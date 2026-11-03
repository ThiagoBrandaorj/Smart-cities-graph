package src;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

// Desenho e edicao do grafo em 2D
public class Painel2D extends JPanel {

    static final int RAIO = 22;
    static final Color VERDE = new Color(15, 157, 107);
    static final Color VERDE_ESCURO = new Color(10, 107, 73);

    Modelo m;
    String modo = "vertice";
    int selecionado = -1;
    int arrastando = -1;

    public Painel2D(Modelo m) {
        this.m = m;
        setBackground(new Color(250, 250, 247));
        setPreferredSize(new Dimension(760, 520));

        MouseAdapter mouse = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                clique(e);
            }

            public void mouseDragged(MouseEvent e) {
                if (arrastando != -1) {
                    m.g.posicoes.get(arrastando)[0] = e.getX();
                    m.g.posicoes.get(arrastando)[1] = e.getY();
                    m.aoRepintar.run();
                }
            }

            public void mouseReleased(MouseEvent e) {
                arrastando = -1;
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    // vertice clicado
    int verticeEm(double x, double y) {
        for (int i = 0; i < m.g.numVertices(); i++) {
            double[] p = m.g.posicoes.get(i);
            if (Math.hypot(p[0] - x, p[1] - y) <= RAIO) return i;
        }
        return -1;
    }

    void clique(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int v = verticeEm(x, y);

        // duplo clique renomeia
        if (e.getClickCount() == 2 && v != -1) {
            renomear(v);
            return;
        }

        if (modo.equals("vertice")) {
            if (v == -1 && e.getClickCount() == 1) {
                m.g.addVertice(m.proximoNome(), x, y);
                m.grafoMudou();
            }
        } else if (modo.equals("aresta")) {
            if (v == -1) {
                selecionado = -1;
            } else if (selecionado == -1) {
                selecionado = v;
            } else {
                if (v != selecionado && pedirPesoECriar(selecionado, v)) {
                    selecionado = -1;
                    m.grafoMudou();
                    return;
                }
                selecionado = -1;
            }
            repaint();
        } else if (modo.equals("mover")) {
            arrastando = v;
        } else if (modo.equals("apagar")) {
            if (v != -1) {
                m.g.removerVertice(v);
                m.grafoMudou();
                return;
            }
            // apagar aresta
            for (int u = 0; u < m.g.numVertices(); u++) {
                for (Grafo.Aresta a : m.g.adj.get(u)) {
                    if (!m.g.mostrar(u, a)) continue;
                    double[] p = pontas(u, a.destino);
                    if (Line2D.ptSegDist(p[0], p[1], p[2], p[3], x, y) < 8) {
                        m.g.removerAresta(u, a.destino);
                        m.grafoMudou();
                        return;
                    }
                }
            }
        }
    }

    boolean pedirPesoECriar(int u, int v) {
        double peso = 1;
        if (m.g.ponderado) {
            String texto = JOptionPane.showInputDialog(this,
                    "Peso da aresta " + m.g.nomes.get(u) + " -> " + m.g.nomes.get(v) + ":", "1");
            if (texto == null) return false;
            try {
                peso = Double.parseDouble(texto.trim().replace(',', '.'));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Peso inválido.");
                return false;
            }
        }
        m.g.addAresta(u, v, peso);
        return true;
    }

    void renomear(int v) {
        String novo = JOptionPane.showInputDialog(this, "Novo nome do vértice:", m.g.nomes.get(v));
        if (novo == null) return;
        novo = novo.trim();
        if (novo.isEmpty() || novo.equals(m.g.nomes.get(v))) return;
        if (m.g.indice(novo) != -1) {
            JOptionPane.showMessageDialog(this, "Já existe um vértice com esse nome.");
            return;
        }
        m.g.renomear(v, novo);
        selecionado = -1;
        m.grafoMudou();
    }

    // pontas da linha da aresta
    double[] pontas(int u, int v) {
        double[] a = m.g.posicoes.get(u);
        double[] b = m.g.posicoes.get(v);
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double tam = Math.max(Math.hypot(dx, dy), 0.001);
        dx = dx / tam;
        dy = dy / tam;
        double desvio = (m.g.direcionado && m.g.temAresta(v, u)) ? 7 : 0;
        double px = -dy * desvio;
        double py = dx * desvio;
        return new double[]{a[0] + dx * RAIO + px, a[1] + dy * RAIO + py,
                            b[0] - dx * RAIO + px, b[1] - dy * RAIO + py};
    }

    static void textoCentralizado(Graphics2D g, String t, double x, double y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(t, (float) (x - fm.stringWidth(t) / 2.0), (float) (y + (fm.getAscent() - fm.getDescent()) / 2.0));
    }

    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // arestas
        for (int u = 0; u < m.g.numVertices(); u++) {
            for (Grafo.Aresta a : m.g.adj.get(u)) {
                if (!m.g.mostrar(u, a)) continue;
                desenharAresta(g, u, a);
            }
        }

        // vertices
        g.setStroke(new BasicStroke(2f));
        for (int i = 0; i < m.g.numVertices(); i++) {
            double[] p = m.g.posicoes.get(i);
            String rotulo = m.rotulos.get(i);
            boolean destaque = rotulo != null;
            int x = (int) p[0] - RAIO;
            int y = (int) p[1] - RAIO;

            if (i == selecionado) g.setColor(new Color(255, 224, 138));
            else if (destaque) g.setColor(new Color(191, 233, 216));
            else g.setColor(Color.WHITE);
            g.fillOval(x, y, 2 * RAIO, 2 * RAIO);

            g.setColor(destaque ? VERDE : Color.DARK_GRAY);
            g.drawOval(x, y, 2 * RAIO, 2 * RAIO);

            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            textoCentralizado(g, m.g.nomes.get(i), p[0], p[1]);

            if (destaque && !rotulo.isEmpty()) {
                g.setColor(VERDE_ESCURO);
                g.setFont(new Font("SansSerif", Font.BOLD, 13));
                textoCentralizado(g, rotulo, p[0], p[1] - RAIO - 9);
            }
        }
    }

    void desenharAresta(Graphics2D g, int u, Grafo.Aresta a) {
        double[] p = pontas(u, a.destino);
        boolean destaque = m.destacada(u, a.destino);

        g.setColor(destaque ? VERDE : Color.GRAY);
        g.setStroke(new BasicStroke(destaque ? 4f : 1.8f));
        g.draw(new Line2D.Double(p[0], p[1], p[2], p[3]));

        // seta
        if (m.g.direcionado) {
            double ang = Math.atan2(p[3] - p[1], p[2] - p[0]);
            int[] xs = {(int) p[2], (int) (p[2] - 14 * Math.cos(ang - 0.4)), (int) (p[2] - 14 * Math.cos(ang + 0.4))};
            int[] ys = {(int) p[3], (int) (p[3] - 14 * Math.sin(ang - 0.4)), (int) (p[3] - 14 * Math.sin(ang + 0.4))};
            g.fillPolygon(xs, ys, 3);
        }

        // peso
        if (m.g.ponderado) {
            String texto = Algoritmos.formatar(a.peso);
            double mx = (p[0] + p[2]) / 2;
            double my = (p[1] + p[3]) / 2;
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            int largura = g.getFontMetrics().stringWidth(texto) + 8;
            g.setColor(getBackground());
            g.fillRect((int) (mx - largura / 2.0), (int) (my - 10), largura, 20);
            g.setColor(destaque ? VERDE_ESCURO : Color.DARK_GRAY);
            textoCentralizado(g, texto, mx, my);
        }
    }
}