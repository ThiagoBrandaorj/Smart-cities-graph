package src;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

/** Visão 3D: layout de forças no espaço + projeção em perspectiva própria (sem bibliotecas 3D). */
public class Painel3D extends JPanel {
    final Modelo m; final Consumer<String> aoEscolher;
    final Map<String, double[]> p = new LinkedHashMap<>();
    double yaw = .6, pitch = -.35, zoom = 1, fit = 1;
    boolean auto = true, moveu; Point inicio, ultimo;
    record Hit(String n, double x, double y, double r, double z) {}
    record Item(double z, Runnable desenhar) {}
    final List<Hit> hits = new ArrayList<>();

    Painel3D(Modelo m, Consumer<String> aoEscolher) {
        this.m = m; this.aoEscolher = aoEscolher;
        setPreferredSize(new Dimension(760, 520)); setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { inicio = ultimo = e.getPoint(); moveu = false; }
            @Override public void mouseDragged(MouseEvent e) {
                if (inicio == null) return;
                if (inicio.distance(e.getPoint()) > 3) moveu = true;
                if (moveu) { yaw += (e.getX() - ultimo.x) * .01; pitch = clamp(pitch + (e.getY() - ultimo.y) * .01, -1.5, 1.5); }
                ultimo = e.getPoint();
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (!moveu && inicio != null) hits.stream().filter(h -> Math.hypot(h.x() - e.getX(), h.y() - e.getY()) <= h.r())
                        .min(Comparator.comparingDouble(Hit::z)).ifPresent(h -> aoEscolher.accept(h.n()));
                inicio = null;
            }
            @Override public void mouseWheelMoved(MouseWheelEvent e) { zoom = clamp(zoom * Math.exp(-e.getPreciseWheelRotation() * .1), .4, 3); }
        };
        addMouseListener(ma); addMouseMotionListener(ma); addMouseWheelListener(ma);
        m.aoMudarEstrutura.add(this::distribuir); m.aoRepintar.add(this::repaint);
        new javax.swing.Timer(16, e -> { if (isShowing()) { if (auto && inicio == null) yaw += .004; repaint(); } }).start();
        distribuir();
    }
    static double clamp(double x, double a, double b) { return Math.max(a, Math.min(b, x)); }
    static double norma(double[] d) { return Math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2]); }
    void recentrar() { yaw = .6; pitch = -.35; zoom = 1; }

    /** Fruchterman-Reingold em 3D: repulsão entre todos os pares O(V²) + atração ao longo das arestas. */
    void distribuir() {
        List<String> nomes = new ArrayList<>(m.g.lista.keySet()); p.keySet().retainAll(nomes);
        Random rnd = new Random(7);
        for (String n : nomes) p.computeIfAbsent(n, v -> {
            var q = m.pos.get(v);
            return new double[]{q == null ? 0 : (q.x - 380) * .5, q == null ? 0 : (q.y - 260) * .5, (rnd.nextDouble() - .5) * 140};
        });
        double k = 95, t = 60; int N = nomes.size();
        for (int it = 0; it < 250; it++, t *= .985) {
            Map<String, double[]> f = new HashMap<>(); for (String n : nomes) f.put(n, new double[3]);
            for (int i = 0; i < N; i++) for (int j = i + 1; j < N; j++) {
                double[] a = p.get(nomes.get(i)), b = p.get(nomes.get(j)), d = {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
                double L = Math.max(norma(d), .01), fo = k * k / L / L;
                for (int c = 0; c < 3; c++) { f.get(nomes.get(i))[c] += d[c] * fo; f.get(nomes.get(j))[c] -= d[c] * fo; }
            }
            for (var e : m.g.arestas()) {
                double[] a = p.get(e.u()), b = p.get(e.v()), d = {a[0] - b[0], a[1] - b[1], a[2] - b[2]}; double fo = norma(d) / k;
                for (int c = 0; c < 3; c++) { f.get(e.u())[c] -= d[c] * fo; f.get(e.v())[c] += d[c] * fo; }
            }
            for (String n : nomes) {
                double[] q = p.get(n), fn = f.get(n);
                for (int c = 0; c < 3; c++) fn[c] -= q[c] * .05;
                double mag = Math.max(norma(fn), 1e-9), s = Math.min(mag, t) / mag;
                for (int c = 0; c < 3; c++) q[c] += fn[c] * s;
            }
        }
        double r = 0; for (double[] q : p.values()) r = Math.max(r, norma(q));
        fit = r > 0 ? clamp(190 / r, .3, 3) : 1.5;
    }

    /** Rotação (yaw/pitch) + perspectiva. Retorna {x, y, profundidade, fator de perspectiva}. */
    double[] proj(double[] q, int W, int H) {
        double cy = Math.cos(yaw), sy = Math.sin(yaw), cp = Math.cos(pitch), sp = Math.sin(pitch);
        double x = q[0] * cy + q[2] * sy, z0 = -q[0] * sy + q[2] * cy, y = q[1] * cp - z0 * sp, z = q[1] * sp + z0 * cp;
        double d = 520 / (520 + z), s = d * fit * zoom;
        return new double[]{W / 2.0 + x * s, H / 2.0 + y * s, z, d};
    }
    double raio(double[] q) { return 20 * q[3] * clamp(zoom, .7, 1.4); }
    static void alfa(Graphics2D g, double a) { g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) a)); }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0; int W = getWidth(), H = getHeight();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setPaint(new RadialGradientPaint(W / 2f, H / 2f, Math.max(W, H) / 1.5f, new float[]{0, 1}, new Color[]{new Color(0x183A33), new Color(0x08120F)}));
        g.fillRect(0, 0, W, H);
        Map<String, double[]> P = new HashMap<>(); p.forEach((n, q) -> P.put(n, proj(q, W, H)));
        hits.clear(); List<Item> itens = new ArrayList<>();
        for (var a : m.g.arestas()) { var s = P.get(a.u()); var t = P.get(a.v()); if (s != null && t != null) itens.add(new Item((s[2] + t[2]) / 2, () -> aresta(g, a, s, t))); }
        P.forEach((n, q) -> itens.add(new Item(q[2], () -> no(g, n, q))));
        itens.sort((a, b) -> Double.compare(b.z(), a.z()));   // do mais distante ao mais próximo
        itens.forEach(i -> i.desenhar().run());
        g.setFont(new Font("SansSerif", Font.PLAIN, 12)); g.setColor(new Color(255, 255, 255, 140));
        g.drawString("arraste: girar · roda: zoom · clique em um vértice: definir origem", 10, H - 10);
    }

    void aresta(Graphics2D g, Grafo.Aresta a, double[] s, double[] t) {
        boolean on = m.arestas.contains(m.g.chave(a.u(), a.v())); double dm = (s[3] + t[3]) / 2;
        double dx = t[0] - s[0], dy = t[1] - s[1], L = Math.max(Math.hypot(dx, dy), 1e-6), ux = dx / L, uy = dy / L;
        double o = m.g.direcionado && m.g.temAresta(a.v(), a.u()) ? 6 : 0, px = -uy * o, py = ux * o, ra = raio(s), rb = raio(t);
        double x1 = s[0] + ux * ra + px, y1 = s[1] + uy * ra + py, x2 = t[0] - ux * rb + px, y2 = t[1] - uy * rb + py;
        Color verde = new Color(0x35E0A1);
        if (on) { alfa(g, .3); g.setColor(verde); g.setStroke(new BasicStroke((float) (11 * dm), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(new Line2D.Double(x1, y1, x2, y2)); }
        alfa(g, clamp(dm - .3, .35, 1)); g.setColor(on ? verde : new Color(0x8FB0A6));
        g.setStroke(new BasicStroke((float) ((on ? 4 : 1.8) * dm))); g.draw(new Line2D.Double(x1, y1, x2, y2));
        if (m.g.direcionado) {
            double an = Math.atan2(uy, ux), h = 14 * dm; Path2D.Double seta = new Path2D.Double(); seta.moveTo(x2, y2);
            seta.lineTo(x2 - h * Math.cos(an - .4), y2 - h * Math.sin(an - .4)); seta.lineTo(x2 - h * Math.cos(an + .4), y2 - h * Math.sin(an + .4)); g.fill(seta);
        }
        if (m.g.ponderado) {
            String tx = Algoritmos.fmt(a.peso()); double mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
            g.setFont(new Font("SansSerif", Font.BOLD, (int) Math.max(10, 13 * dm))); int w = g.getFontMetrics().stringWidth(tx) + 8, hh = g.getFontMetrics().getHeight();
            g.setColor(new Color(0x0B1A16)); g.fill(new Rectangle2D.Double(mx - w / 2.0, my - hh / 2.0, w, hh));
            g.setColor(on ? new Color(0x7DFFC9) : new Color(0xE9FFF6)); Painel2D.centralizar(g, tx, mx, my);
        }
        alfa(g, 1);
    }
    void no(Graphics2D g, String n, double[] q) {
        String l = m.nos.get(n); boolean on = l != null; double r = raio(q);
        alfa(g, clamp(q[3] - .2, .55, 1));
        if (on) { g.setColor(new Color(53, 224, 161, 70)); g.fill(new Ellipse2D.Double(q[0] - r - 8, q[1] - r - 8, 2 * r + 16, 2 * r + 16)); }
        Color[] cores = on ? new Color[]{new Color(0xC9FFE9), new Color(0x1FBF87), new Color(0x0B5A42)} : new Color[]{new Color(0xD6E4FF), new Color(0x4D78C9), new Color(0x1C2F63)};
        g.setPaint(new RadialGradientPaint(new Point2D.Double(q[0] - r * .35, q[1] - r * .4), (float) (r * 1.4), new float[]{0, .55f, 1}, cores));
        g.fill(new Ellipse2D.Double(q[0] - r, q[1] - r, 2 * r, 2 * r));
        if (n.equals(m.origem)) { g.setColor(new Color(0xFFD24A)); g.setStroke(new BasicStroke(3f)); g.draw(new Ellipse2D.Double(q[0] - r - 4, q[1] - r - 4, 2 * r + 8, 2 * r + 8)); }
        g.setColor(Color.WHITE); g.setFont(new Font("SansSerif", Font.BOLD, (int) (r * .8))); Painel2D.centralizar(g, n, q[0], q[1]);
        if (on && !l.isEmpty()) { g.setColor(new Color(0x7DFFC9)); g.setFont(new Font("SansSerif", Font.BOLD, (int) (r * .65))); Painel2D.centralizar(g, l, q[0], q[1] - r - 10); }
        alfa(g, 1); hits.add(new Hit(n, q[0], q[1], r, q[2]));
    }
}
